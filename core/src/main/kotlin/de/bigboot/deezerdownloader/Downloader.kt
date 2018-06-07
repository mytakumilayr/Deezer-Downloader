/*
 * Copyright 2017 BigBoot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bigboot.deezerdownloader
import com.zeloon.deezer.domain.Track
import kotlinExtensions.OS
import kotlinExtensions.deezer.Deezer
import kotlinExtensions.deezer.collect
import kotlinExtensions.deezer.collectAll
import kotlinExtensions.java.PriorityExecutor
import kotlinExtensions.java.sanitizeFileName
import kotlinExtensions.okhttp.DefaultHttpClient
import kotlinExtensions.sfl4j.debug
import kotlinExtensions.sfl4j.error
import kotlinExtensions.sfl4j.info
import kotlinExtensions.sfl4j.trace
import nl.komponents.kovenant.task
import okhttp3.Request
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.audio.mp3.MP3FileReader
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagOptionSingleton
import org.jaudiotagger.tag.id3.ID3v23Frame
import org.jaudiotagger.tag.id3.ID3v23Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodyUSLT
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList


class Downloader {
    private val config: Config get() = DownloaderApplication.config
    private val workers = PriorityExecutor(config.concurrentDownloads, comparator = PriorityExecutor.Companion.LOW_BEFORE_HIGH)
    private val inputMutex = Semaphore(1)
    private val initDownloadMutex = Semaphore(1)
    private val pauseMutex = Semaphore(1)
    var paused = false
        set(value) {
            if (field != value) {
                if (value) {
                    pauseMutex.acquire()
                } else {
                    pauseMutex.release()
                }
            }
            field = value
        }


    fun queueDownload(download: Download): Download {
        download.handle = workers.submit(PRIORITY_PREPROCESS) {
            download.filenameTemplate = when(download.entry) {
                is Entry.Track -> DownloaderApplication.config.trackTemplate
                is Entry.Album -> DownloaderApplication.config.albumTemplate
                is Entry.Playlist -> DownloaderApplication.config.playlistTemplate
                is Entry.Artist -> DownloaderApplication.config.artistTemplate
            }

            download(download)
        }
        trace { "Adding preprocess task for download ${download.id}, currently has [${workers.activeCount}/${workers.activeCount+workers.queue.size}] tasks" }

        return download
    }

    init {
        if (OS.isAndroid) {
            TagOptionSingleton.getInstance().isAndroid = true
        }
    }

    fun cancelDownload(download: Download) {
        download.cancel()
        download.handle?.cancel(true)

        download.children.forEach {
            cancelDownload(it)
        }
    }

    fun askForExistingFileBehaviour(file: File): Config.ExistingFileBehaviour
        = DialogHandler.Instance.askForExistingFileBehaviour(file)

    private fun download(download: Download) {
        try {
            val data = generateData(download)
            val name = fillNameTemplate(download.filenameTemplate, data)

            val entry = download.entry
            when (entry) {
                is Entry.Album -> downloadAsTracklist(download, Deezer.get(entry.album.id).tracks.collectAll())
                is Entry.Playlist -> downloadAsTracklist(download, Deezer.getTracks(entry.playlist.id).collectAll(), true)
                is Entry.Artist -> downloadAsTracklist(download, Deezer.getTop(entry.artist.id).collect(20))

                is Entry.Track -> {
                    if (download.preprocessed) {
                        trace { "Starting Download(${download.id}) $name, currently running ${workers.activeCount} of ${DownloaderApplication.config.concurrentDownloads}" }
                        waitForUnpause()

                        val filename = File(download.outputFile?.parentFile, download.outputFile?.nameWithoutExtension).absolutePath

                        val extension = when (config.useTmpFiles) {
                            true -> "tmp"
                            false -> "mp3"
                        }
                        val file = File("$filename.$extension")

                        val trackId = entry.track.id.value.toString()
                        val api = DeezerPrivateApi().trackID(trackId).maxQuality(DownloaderApplication.config.maxDownloadQuality).get()
                        val lyrics = api.getLyrics()
                        try {
                            val downloadMutex = Semaphore(1)
                            // trace { "$name waiting for init mutex" }
                            initDownloadMutex.acquire()
                            val init = AtomicBoolean(false)
                            downloadMutex.acquire()
                            val req = api.getDownloadRequest()
                            file.parentFile.mkdirs()
                            DownloadHandler.Instance.createDownloader().download(req.trackID, req.url, file, object: PlatformDownloader.Listener {
                                override fun error(error: Throwable) {
                                    download.state = Download.State.Error
                                    downloadMutex.release()
                                    error(error) { "Download of $name failed" }
                                    if (!init.getAndSet(true))
                                        initDownloadMutex.release()
                                }

                                override fun init() {
                                    if (!init.getAndSet(true))
                                        initDownloadMutex.release()
                                    //trace { "$name released init mutex"}
                                }

                                override fun progress(progress: Double) {
                                    download.progress = progress
                                    if (download.state != Download.State.Error)
                                        download.state = Download.State.Running
                                }

                                override fun finished() {
                                    download.progress = 1.0
                                    downloadMutex.release()
                                }

                            })
                            // trace { "$name waiting to finish" }
                            downloadMutex.acquire()
                            downloadMutex.release()
                            // trace { "$name finish received" }
                        } catch (ex: Exception) {
                            initDownloadMutex.release()
                            download.state = Download.State.Error
                            error(ex) { "Download of $name failed" }
                        }
                        if (download.state != Download.State.Error) {
                            download.state = Download.State.Tagging
                            tagFile(file, entry.track, lyrics)
                            download.state = Download.State.Finished
                        }
                        trace { "Download of $name finished, [${workers.activeCount}/${workers.activeCount+workers.queue.size}] tasks left" }
                    } else {
                        waitForUnpause()

                        trace { "Preprocessing Download(${download.id}) $name, currently running ${workers.activeCount} of ${DownloaderApplication.config.concurrentDownloads}" }


                        download.state = Download.State.Preprocessing

                        val filename = name
                        val mp3file = File(DownloaderApplication.config.outputFolder, "$filename.mp3")
                        download.outputFile = mp3file
                        if (mp3file.exists()) {
                            inputMutex.acquire()

                            var behaviour = DownloaderApplication.config.existingFileBehaviour

                            while (behaviour == Config.ExistingFileBehaviour.Ask) {
                                behaviour = askForExistingFileBehaviour(mp3file)
                            }

                            inputMutex.release()

                            when (behaviour) {
                                Config.ExistingFileBehaviour.Skip -> {
                                    download.state = Download.State.Skipped
                                    download.progress = 1.0
                                    return
                                }
                                Config.ExistingFileBehaviour.Overwrite -> {
                                    mp3file.delete()
                                }
                                else -> throw AssertionError("Invalid ExistingFileBehaviour: ${behaviour.name}")
                            }
                        }
                        download.preprocessed = true
                        download.state = Download.State.Preprocessed
                        mp3file.parentFile.mkdirs()
                        debug { "Queued the download $name" }
                        download.handle = workers.submit(download.id) {
                            debug { "Actually called the download task of $name" }
                            download(download)
                        }

                        trace { "Preprocessing of $name finished, [${workers.activeCount}/${workers.activeCount+workers.queue.size}] tasks left" }
                    }
                }
                else -> {
                    error { "Unknown Download Type: ${entry::class.java}" }
                    throw NotImplementedError()
                }
            }
        } catch (ex: Exception) {
            download.state = Download.State.Error
            error (ex) { "Error while downloading: " }
        }
    }

    private fun waitForUnpause() {
        if (pauseMutex.availablePermits() == 0)
            info { "Waiting for unpause" }
        pauseMutex.acquire()
        pauseMutex.release()
    }

    private fun downloadAsTracklist(download: Download, list: List<Track>, generateM3U: Boolean = false) {

        val updateListener = { _: Download ->
            download.progress = download.children.sumByDouble { it.progress } / download.children.size
            download.state = download.children.fold(Download.State.Finished, { current, next ->
                when {
                    current.priority > next.state.priority -> current
                    else -> next.state
                }
            })
        }

        val listData = generateData(download)

        val writer: Writer = when (generateM3U) {
            true -> File(DownloaderApplication.config.outputFolder, "${(download.entry as Entry.Playlist).playlist.title}.m3u").writer()
            false -> object: Writer() {
                override fun write(cbuf: CharArray, off: Int, len: Int) {}
                override fun flush() {}
                override fun close() {}
            }
        }

        val m3uentries = ArrayList<String>(list.size)

        list.mapIndexed { i, entry -> Pair(Entry.Track(entry), i + 1) }.reversed().forEach {
            val (track, i) = it
            val paddedNumber = i.toString().padStart(list.size.toString().length, '0')
            val data = when (download.entry) {
                is Entry.Playlist -> listData.plus(mapOf("playlistNumber" to paddedNumber))
                is Entry.Artist -> listData.plus(mapOf("toplistNumber" to paddedNumber))
                else -> listData
            }
            val trackDownload = Download(track, download.id).apply {
                filenameTemplate = fillNameTemplate(download.filenameTemplate, data)
                if (generateM3U) {
                    m3uentries.add("""|#EXTINF:${track.track.duration},${track.track.artist.name} - ${track.track.title}
                                      |${fillNameTemplate(filenameTemplate, generateData(this))}.mp3
                                      |""".trimMargin())
                }
                onUpdate(updateListener)
                containingListSize = list.size
            }
            workers.submit(PRIORITY_PREPROCESS) {
                download(trackDownload)
            }
            download.children.add(trackDownload)
        }

        writer.buffered().use { writer ->
            writer.write("#EXTM3U\n\n")

            m3uentries.reversed().forEach { writer.write("$it\n") }
        }

        download.preprocessed = true
    }

    private fun tagFile(file: File, track: Track, lyrics: DeezerPrivate.Lyrics) {
        task {
            val trackDetails  = Deezer.get(track.id)
            val albumDetails  = Deezer.get(trackDetails.album.id)
            var coverUrl = if (config.imageSize == Config.ImageSize.Custom) {
                val coverId = COVER_ID_REGEX.find(albumDetails.cover_big)
                        ?: throw RuntimeException("Unable to get coverId from link: ${albumDetails.cover_xl}")

                val url = COVER_URL_TEMPLATE.format(coverId.groupValues[1], config.imageSizeCustom)

                if (DefaultHttpClient.newCall(Request.Builder().url(url).head().build())
                        .execute().isSuccessful) url else null
            } else {
                null
            }

            if (coverUrl == null) {
                coverUrl = when (config.imageSize) {
                    Config.ImageSize.Small -> albumDetails.cover_small
                    Config.ImageSize.Medium -> albumDetails.cover_medium
                    Config.ImageSize.Big -> albumDetails.cover_big
                    Config.ImageSize.XL -> albumDetails.cover_xl
                    Config.ImageSize.Custom -> albumDetails.cover_xl
                }
            }
            coverUrl = coverUrl!!.replace(".jpg", ".${config.imageFormat.extension}")
            val coverData = DefaultHttpClient.newCall(
                    Request.Builder().url(coverUrl).build()
            ).execute().body().bytes()

            val size = when (config.imageSize) {
                Config.ImageSize.Small -> 56
                Config.ImageSize.Medium -> 250
                Config.ImageSize.Big -> 500
                Config.ImageSize.XL -> 1000
                Config.ImageSize.Custom -> config.imageSizeCustom
            }

            val coverImage = ImageHandler.Image(coverData, ImageHandler.ImageInfo("image/${config.imageFormat.extension}", size, size))

            val artwork = ArtworkFactory.getNew().apply {
                binaryData = coverImage.binaryData
                mimeType = coverImage.info.mimeType
                this.pictureType = when (config.coverType) {
                    Config.CoverType.Other -> 0
                    Config.CoverType.Icon -> 1
                    Config.CoverType.OtherIcon -> 2
                    Config.CoverType.Front -> 3
                    Config.CoverType.Back -> 4
                    Config.CoverType.Leaflet -> 5
                    Config.CoverType.Media -> 6
                    Config.CoverType.LeadArtist -> 7
                    Config.CoverType.Artist -> 8
                    Config.CoverType.Conductor -> 9
                    Config.CoverType.Band -> 10
                    Config.CoverType.Composer -> 11
                    Config.CoverType.Lyricist -> 12
                    Config.CoverType.RecordingLocation -> 13
                    Config.CoverType.DuringRecording -> 14
                    Config.CoverType.DuringPerformance -> 15
                    Config.CoverType.VideoCapture -> 16
                    Config.CoverType.ABrightColoredFish -> 17
                    Config.CoverType.Illustration -> 18
                    Config.CoverType.BandLogo -> 19
                    Config.CoverType.PublisherLogo -> 20
                }
            }

            val reader = MP3FileReader()
            val mp3file = reader.read(file) as MP3File
            val id3v2Tag = ID3v23Tag()
            mp3file.tag = id3v2Tag

            id3v2Tag.setField(FieldKey.TRACK, trackDetails.track_position.toString())
            id3v2Tag.setField(FieldKey.TRACK_TOTAL, albumDetails.nb_tracks.toString())
            id3v2Tag.setField(FieldKey.DISC_NO, trackDetails.disk_number.toString())
            id3v2Tag.setField(FieldKey.ARTIST, trackDetails.contributors
                    .filter { it.role == "Main" }
                    .map { it.name }
                    .distinct()
                    .joinToString("/")
            )
            id3v2Tag.setField(FieldKey.ARTISTS, trackDetails.contributors
                    .groupBy { it.name }
                    .map { (name, details) -> Pair(name, details.map { it.role }.distinct()) }
                    .map { (name, roles) -> "$name (${roles.joinToString(", ")})"}
                    .joinToString("/")

                    /* Will result in something like this: "Artist1 (Main, Composer, OtherRole...)/Artist2 (Featured)/..." */
            )
            id3v2Tag.setField(FieldKey.TITLE, trackDetails.title)
            id3v2Tag.setField(FieldKey.ALBUM, albumDetails.title)
            id3v2Tag.setField(FieldKey.RECORD_LABEL, albumDetails.label)
            id3v2Tag.setField(FieldKey.ALBUM_ARTIST, albumDetails.contributors
                    .filter { it.role == "Main" }
                    .map { it.name }
                    .distinct()
                    .joinToString("/")
            )
            id3v2Tag.setField(FieldKey.YEAR, SimpleDateFormat("yyyy-MM-dd", Locale.US).format(trackDetails.release_date.time))
            if (lyrics.lyrics != null) {
                val frameBody = FrameBodyUSLT(1.toByte(), "eng", "lyrics", lyrics.lyrics)
                val frame = ID3v23Frame("USLT").apply {
                    body = frameBody
                }
                id3v2Tag.setFrame(frame)
            }
            if (trackDetails.bpm > 0f) id3v2Tag.setField(FieldKey.BPM, trackDetails.bpm.toInt().toString())
            if (config.embedGenres) id3v2Tag.setField(FieldKey.GENRE, albumDetails.genres.data.map { it.name }.joinToString(config.genreSeparator))
            id3v2Tag.setField(FieldKey.ISRC, trackDetails.isrc)
            id3v2Tag.setField(artwork)
            mp3file.commit()
            if (config.useTmpFiles) {
                file.renameTo(File(file.parentFile, file.nameWithoutExtension + ".mp3"))
            }
        } fail {
            error(it) { "Error tagging file: ${file.canonicalPath}" }
        }
    }

    private fun fillNameTemplate(template: String, data: Map<String, String>): String
            = data.entries.fold(template, { current, entry -> current.replace("%${entry.key}%", entry.value.sanitizeFileName())})
            .replace('/', File.separatorChar).replace('\\', File.separatorChar)



    private fun generateData(download: Download): Map<String, String> {
        val entry = download.entry
        return when(entry) {
            is Entry.Track -> {
                val trackDetails  = Deezer.get(entry.track.id)
                val albumDetails  = Deezer.get(trackDetails.album.id)
                val artistDetails = Deezer.get(trackDetails.artist.id)
                val albumArtistDetails = Deezer.get(albumDetails.artist.id)
                mapOf(
                        "track" to trackDetails.title,
                        "album" to albumDetails.title,
                        "artist" to artistDetails.name,
                        "trackNumber" to trackDetails.track_position.toString().padStart(download.containingListSize.toString().length, '0'),
                        "diskNumber" to trackDetails.disk_number.toString(),
                        "year" to trackDetails.release_date.get(Calendar.YEAR).toString(),
                        "albumYear" to albumDetails.title,
                        "albumArtist" to albumArtistDetails.name,
                        "bpm" to trackDetails.bpm.toInt().toString(),
                        "releaseDate" to SimpleDateFormat(DownloaderApplication.config.dateFormat).format(trackDetails.release_date.time)
                )
            }
            is Entry.Playlist -> mapOf("playlist" to entry.playlist.title)
            else -> mapOf()
        }
    }

    companion object {
        val PRIORITY_PREPROCESS = 0

        val COVER_ID_REGEX = Regex("""/images/cover/(\w+)/\d+x\d+-[\d-]+\.(?:jpg|png)""")
        val COVER_URL_TEMPLATE = "http://e-cdn-images.deezer.com/images/cover/%1\$s/%2\$dx%2\$d.jpg"
    }
}


