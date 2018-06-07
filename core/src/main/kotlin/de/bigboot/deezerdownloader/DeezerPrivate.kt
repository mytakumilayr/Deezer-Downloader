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
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinExtensions.java.substringBetween
import kotlinExtensions.okhttp.DefaultHttpClient
import kotlinExtensions.sfl4j.error
import kotlinExtensions.sfl4j.info
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.Exception

sealed class DeezerPrivateException : RuntimeException {
    constructor(msg: String):super(msg)
    constructor(msg: String, cause: Throwable):super(msg, cause)

    class LinkGenerateException: Exception("Error generating download url")
    class TrackUnavailableException: Exception("Track not available on Deezer")
    class DecryptionException(cause: Throwable): Exception("Error decrypting content", cause)
    class DownloadException(trackID: String, cause: Throwable): Exception("Error while Downloading the track with id $trackID", cause)
}

abstract class DeezerPrivate private constructor() {

    data class DownloadRequest(val url: String, val trackID: String)

    class A {
        private var trackID: String = ""
        private var maxQuality: StreamQuality = StreamQuality.High

        fun trackID(trackID: String): A {
            this.trackID = trackID
            return this
        }

        fun maxQuality(maxQuality: StreamQuality): A {
            this.maxQuality = maxQuality
            return this
        }

        fun get(): B = B( trackID, maxQuality )
    }

    class B(private var trackID: String,
            private val maxQuality: StreamQuality) {

        private data class ApiToken(val token: String, val session: String)

        private fun createApiToken(): ApiToken {
            val request = Request.Builder()
                    .url("http://www.deezer.com/")
                    .build()

            val response = DefaultHttpClient.newCall(request).execute()
            val resultString = response.body().string()

            val regexResult = Regex("""checkForm\s*=\s*["|'](.*)["|']""").find(resultString)
            val apiToken = regexResult?.groups?.get(1)?.value ?: ""
            val sessionID = response.header("Set-Cookie").substringBefore(';')

            if (apiToken.isBlank()) throw RuntimeException("Error getting api-token")
            if (sessionID.isBlank()) throw RuntimeException("Error getting sessionId")

            return ApiToken(apiToken, sessionID)
        }

        private val apiToken: ApiToken by lazy { createApiToken() }

        fun getDownloadRequest(): DownloadRequest {
            if (trackID.isEmpty())
                throw IllegalStateException("TrackID not set")

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "[{\"method\":\"song.getListData\",\"params\":{\"sng_ids\":[$trackID]}}]")
            val request = Request.Builder()
                    .url("http://www.deezer.com/ajax/gw-light.php?api_version=1.0&api_token=${apiToken.token}&input=3")
                    .post(body)
                    .addHeader("Cookie", apiToken.session)
                    .build()
            val response = DefaultHttpClient.newCall(request).execute()
            val responseString = response.body().string()


            val track = ObjectMapper().readTree(responseString)?.get(0)?.get("results")?.get("data")?.get(0)


            if (track != null) {
                if (track.has("FALLBACK") && track["FALLBACK"].has("SNG_ID")) {
                    val fallback = track["FALLBACK"]["SNG_ID"].asText()
                    info { "Track with id $trackID has Fallback, getting $fallback instead" }
                    this.trackID = fallback
                    return B(fallback, maxQuality).getDownloadRequest()
                }

                val puid = track.get("MD5_ORIGIN").textValue()
                val mediaVersion = track.get("MEDIA_VERSION").asInt()

                for (quality in StreamQuality.values().reversed()) {
                    if (quality.bitrate > maxQuality.bitrate) {
                        continue
                    }
                    val size = track.get("FILESIZE_MP3_${quality.bitrate}").asInt()
                    if (size > 0) {
                        return DownloadRequest(DeezerDecrypter.getDownloadURL(puid, quality.formatID, trackID, mediaVersion), trackID)
                    }
                }
            }

            throw DeezerPrivateException.LinkGenerateException()
        }

        fun getLyrics(): Lyrics {
            if (trackID.isEmpty())
                throw IllegalStateException("TrackID not set")

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "[{\"method\":\"song.getLyrics\",\"params\":{\"SNG_ID\":\"$trackID\"}}]")
            val request = Request.Builder()
                    .url("http://www.deezer.com/ajax/gw-light.php?api_version=1.0&api_token=${apiToken.token}&input=3")
                    .post(body)
                    .addHeader("Cookie", apiToken.session)
                    .build()
            val response = DefaultHttpClient.newCall(request).execute()
            val responseString = response.body().string()

            val results = ObjectMapper().readTree(responseString)?.get(0)?.get("results")
            val lyrics = results?.get("LYRICS_TEXT")?.asText()
            val syncedLyrics = results?.get("LYRICS_SYNC_JSON")?.map {
                val timestamp = it.get("milliseconds")?.asInt()
                val line = it.get("line")?.asText(null)
                when {
                    timestamp != null && line != null -> Lyrics.SyncedLine(timestamp, line)
                    else -> null
                }
            }?.filterNotNull()
            val lyricists = results?.get("LYRICS_WRITERS")?.asText()?.split(", ")?.toList()
            return Lyrics(lyrics, syncedLyrics, lyricists)
        }
    }

    data class Lyrics(val lyrics: String? = null, val syncedLyrics: List<SyncedLine>? = null, val lyricists: List<String>? = null) {
        data class SyncedLine(val timestamp: Int, val text: String)
    }
}

fun DeezerPrivateApi(): DeezerPrivate.A = DeezerPrivate.A()