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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.module.SimpleModule
import com.zeloon.deezer.domain.Object
import de.bigboot.deezerdownloader.DownloaderApplication
import de.bigboot.deezerdownloader.Version
import kotlinExtensions.okhttp.DefaultHttpClient
import okhttp3.Request

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultNoArgConstructor

@DefaultNoArgConstructor
internal class ReleaseDescription (
        val jar: String,
        val exe: String,
        @Deprecated("There are no separate debug versions anymore")
        val djar: String,
        @Deprecated("There are no separate debug versions anymore")
        val dexe: String,
        val changes: Array<String>
)

internal class ReleaseDescriptionDeserializer: JsonDeserializer<ReleaseDescription>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ReleaseDescription {
        val string = p.readValueAs(String::class.java)
        return (ObjectMapper().readValue(string, ReleaseDescription::class.java))
    }

}

@DefaultNoArgConstructor
internal data class Release (
        val tag_name: String,
        val description: ReleaseDescription
)

@DefaultNoArgConstructor
@JsonIgnoreProperties("commit")
internal data class Tag (
        val name: String,
        val message: String,
        val release: Release
)

data class UpdateResult(val version: Version, val link: String, val changelog: String = "") {
    companion object {
        val NO_UPDATE = UpdateResult(Version("0.0.0"), "")
    }
}

internal object objectMapper: ObjectMapper() {
    init {
        val module = SimpleModule()
        module.setDeserializerModifier(object: BeanDeserializerModifier() {
            override fun modifyDeserializer(config: DeserializationConfig, beanDesc: BeanDescription, deserializer: JsonDeserializer<*>): JsonDeserializer<*> {
                if (beanDesc.beanClass == ReleaseDescription::class.java) {
                    return ReleaseDescriptionDeserializer()
                } else {
                    return deserializer
                }
            }
        })
        registerModule(module)
    }
}

class Updater {
    companion object {
        private val ProjectID = "2692014"
        private val ApiURL = "https://gitlab.com/api/v3/projects/$ProjectID/repository/tags"
        private val ApiToken = "_zH4xLGa-_4Hs7FdheF-"

        private val htmlHeader: String by lazy {
            Updater::class.java.getResourceAsStream("changelog_header.html").bufferedReader().use { it.readText() }
        }

        fun checkForUpdate(): UpdateResult {
            val localVersion = DownloaderApplication.version

            val request = Request.Builder()
                    .addHeader("PRIVATE-TOKEN", ApiToken)
                    .url(ApiURL)
                    .build()

            val response = DefaultHttpClient.newCall(request).execute()
            val tags: Array<Tag> = objectMapper.readValue(response.body().charStream(), Array<Tag>::class.java)

            val changelog = StringBuilder(htmlHeader)

            changelog.append("<body>")

            tags
                    .filter { Version(it.name) > localVersion }
                    .sortedByDescending { Version(it.name) }
                    .forEach {
                        changelog.append("<h4>${it.name}:</h4><ul>")

                        for (change in it.release.description.changes) {
                            changelog.append("<li>$change</li>")
                        }

                        changelog.append("</ul>")
                    }
            changelog.append("</body></html>")

            val latest = tags.maxBy { Version(it.name) }
            return latest?.release?.let {
                val remoteVersion = Version(it.tag_name)
                if (remoteVersion > localVersion) {
                    val link = when {
                        JavaFXApplication.isExe -> it.description.exe
                        else                    -> it.description.jar
                    }
                    UpdateResult(remoteVersion, link, changelog.toString())
                } else null
            } ?: UpdateResult.NO_UPDATE
        }
    }
}