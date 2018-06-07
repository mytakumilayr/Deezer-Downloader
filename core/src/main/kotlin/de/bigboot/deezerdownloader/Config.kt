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

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.ObjectMapper
import de.bigboot.deezerdownloader.i18n.CoreStrings
import kotlinExtensions.java.BundleLine
import kotlinExtensions.java.clamp
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy

@JsonInclude(JsonInclude.Include.NON_NULL)
class Config private constructor(
        @JacksonInject("filename")
        @JsonIgnore
        val filename: String = ""
) {
    annotation class Hidden

    enum class ImageSize {
        Small, Medium, Big, XL, Custom;
    }

    enum class ExistingFileBehaviour(val text: BundleLine) {
        Skip(CoreStrings.settings__existing_file_skip),
        Overwrite(CoreStrings.settings__existing_file_overwrite),
        Ask(CoreStrings.settings__existing_file_ask)
    }

    enum class CoverType(val text: BundleLine? = null) {
        Other(CoreStrings.settings__cover_other),
        Icon(),
        OtherIcon(),
        Front(CoreStrings.settings__cover_front),
        Back(CoreStrings.settings__cover_back),
        Leaflet(),
        Media(),
        LeadArtist(),
        Artist(),
        Conductor(),
        Band(),
        Composer(),
        Lyricist(),
        RecordingLocation(),
        DuringRecording(),
        DuringPerformance(),
        VideoCapture(),
        ABrightColoredFish(),
        Illustration(),
        BandLogo(),
        PublisherLogo()
    }

    enum class ImageFormat(val extension: String) {
        JPEG("jpg"),
        PNG("png")
    }

    data class QueueEntry(val id: String, val state: Download.State) {
        constructor(): this("", Download.State.Error)
    }

    var useFancy: Boolean = true
        set(value) { field = value; save() }

    var trackTemplate: String = "%track%"
        set(value) { field = value; save() }

    var albumTemplate: String = "%albumArtist%/%album% (%year%)/%trackNumber%. %track%"
        set(value) { field = value; save() }

    var artistTemplate: String = "%artist% (Top20)/%toplistNumber%. %track%"
        set(value) { field = value; save() }

    var playlistTemplate: String = "%playlist%/%playlistNumber%. %track%"
        set(value) { field = value; save() }

    var concurrentDownloads: Int = 1
        set(value) { field = value.clamp(1, 8); save() }

    var outputFolder: File = DataHandler.Instance.downloadPath.absoluteFile
        set(value) { field = value; save() }

    var existingFileBehaviour: ExistingFileBehaviour = ExistingFileBehaviour.Overwrite
        set(value) { field = value; save() }

    var maxDownloadQuality: StreamQuality = StreamQuality.High
        set(value) { field = value; save() }

    var imageSize: ImageSize = ImageSize.Big
        set(value) { field = value; save() }

    var imageSizeCustom: Int = 800
        set(value) { field = value; save() }

    var imageFormat: ImageFormat = ImageFormat.JPEG
        set(value) { field = value; save() }

    var coverType: CoverType = CoverType.Front
        set(value) { field = value; save() }

    var width: Int = 780
        set(value) { field = value; save() }

    var height: Int = 600
        set(value) { field = value; save() }

    var embedGenres: Boolean = false
        set(value) { field = value; save() }

    var genreSeparator: String = " "
        set(value) { field = value; save() }

    var useTmpFiles: Boolean = false
        set(value) { field = value; save() }

    var userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36"
        set(value) { field = value; save() }

    var proxyType: Proxy.Type = Proxy.Type.DIRECT
        set(value) { field = value; save() }

    var proxy: String = "localhost:8080"
        set(value) { field = value; save() }

    var dateFormat: String = "yyyy-MM-dd"
        set(value) { field = value; save() }

    var checkForUpdate: Boolean = true
        set(value) { field = value; save() }

    var disclaimerAccepted: Boolean = false
        set(value) { field = value; save() }

    @get:Hidden
    var searchBarSize: Double = 200.0
        set(value) { field = value; save() }

    @get:Hidden
    var downloadQueue: List<QueueEntry> = ArrayList()
        set(value) { field = value; save() }

    @get:Hidden
    var paused: Boolean = false
        set(value) { field = value; save() }


    @get:JsonIgnore
    @get:Hidden
    val proxyHost get() = proxy.split(":").firstOrNull() ?: "localhost"

    @get:JsonIgnore
    @get:Hidden
    val proxyPort: Int get()
        = try { proxy.split(":").lastOrNull()?.toInt() ?: 8080 } catch (_: kotlin.NumberFormatException) {8080}

    @get:JsonIgnore
    @get:Hidden
    val proxyAddress: InetSocketAddress
        get() = InetSocketAddress(proxyHost, proxyPort)

    @JsonIgnore
    @get:Hidden
    var portable: Boolean = false

    var showAdvancedSettings: Boolean = false
        set(value) { field = value; save() }

    companion object {
        fun create(filename: String): Config = when {
            File(filename).exists() && File(filename).length() > 0L -> ObjectMapper().apply {
                setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            }.readerFor(Config::class.java)
                    .with(InjectableValues.Std()
                            .addValue("filename", filename))
                    .readValue(File(filename))
            else -> Config(filename).apply { save() }
        }
    }

    fun serialize(): String
            = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)

    fun deserialize(data: String) {
        ObjectMapper().readerForUpdating(this).readValue<Config>(data)
    }
}

internal fun Config.save() {
    val file = File(this.filename).canonicalFile
    file.parentFile.mkdirs()
    ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, this)
}