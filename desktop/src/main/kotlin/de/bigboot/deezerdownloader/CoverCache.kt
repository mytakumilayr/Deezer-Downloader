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

import de.bigboot.deezerdownloader.DownloaderApplication
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import kotlinExtensions.java.sha256
import nl.komponents.kovenant.CancelablePromise
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.task
import java.io.File
import java.util.*
import javax.cache.Cache
import javax.cache.Caching
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.AccessedExpiryPolicy
import javax.cache.expiry.Duration
import javax.cache.integration.CacheLoader
import javax.cache.integration.CacheLoaderException
import javax.cache.integration.CacheWriter
import javax.cache.integration.CacheWriterException
import javax.imageio.ImageIO

private val cacheDir = DownloaderApplication.cacheDir
private val cacheProvider = Caching.getCachingProvider()
private val cacheManager = cacheProvider.cacheManager
private val cacheConfig = MutableConfiguration<String, Image>().apply {
    setTypes(String::class.java, Image::class.java)
    setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
    isStoreByValue = false
    isWriteThrough = true
    isReadThrough = true
    setCacheWriterFactory { object: CacheWriter<String, Image>{
        override fun delete(key: Any) {
            if (!File(cacheDir, (key as String).sha256()).delete())
                throw CacheWriterException("Error deleting file $key")
        }

        override fun writeAll(entries: MutableCollection<Cache.Entry<out String, out Image>>) {
            entries.forEach { write(it) }
        }

        override fun write(entry: Cache.Entry<out String, out Image>) {
            val file = File(cacheDir, entry.key.sha256())
            file.absoluteFile.parentFile.mkdirs()

            ImageIO.write(SwingFXUtils.fromFXImage(entry.value, null), "jpg", file)
        }

        override fun deleteAll(keys: MutableCollection<*>) {
            keys.filterNotNull().forEach { delete(it) }
        }

    } }
    setCacheLoaderFactory{ object: CacheLoader<String, Image> {
        override fun loadAll(keys: MutableIterable<String>): MutableMap<String, Image> {
            return HashMap<String, Image>().apply {
                this.putAll(keys.map { Pair(it, load(it)) })
            }
        }

        override fun load(key: String): Image {
            val file = File(cacheDir, key.sha256())

            if (file.exists())
                return Image(file.absolutePath)
            else
                throw CacheLoaderException("Error getting file $key")
        }

    }}
}
private val cache = cacheManager.createCache("DeezerCache", cacheConfig)
private var loadImageContext = Kovenant.createContext {
    workerContext.dispatcher {
        name = "LoadImageWorker"
        concurrentTasks = 1

        pollStrategy {
            yielding(numberOfPolls = 1000)
            blocking()
        }
    }


    callbackContext {
        dispatcher {
            name = "LoadImageCallback"
            concurrentTasks = 1
        }
        errorHandler =
                fun(e: Exception)
                        = e.printStackTrace(System.err)
    }

    multipleCompletion =
            fun(a: Any?, b: Any?): Unit
                    = System.err.println(
                    "Tried resolving with $b, but is $a")
}
fun loadCover(url: String): CancelablePromise<Image, Exception> =
    task(loadImageContext) {
        try {
            cache.get(url)
        } catch(ex: CacheLoaderException) {
            Image(url).apply { cache.put(url, this) }
        }
    } as CancelablePromise<Image, Exception>
