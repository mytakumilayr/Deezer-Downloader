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

package kotlinExtensions.deezer

import com.zeloon.deezer.io.CachedResourceConnection
import com.zeloon.deezer.io.ResourceConnection
import de.bigboot.deezerdownloader.DownloaderApplication
import kotlinExtensions.java.sha256
import kotlinExtensions.okhttp.DefaultHttpClient
import okhttp3.Request
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

class CachedHttpResourceConnection: CachedResourceConnection {
//
//    private val cacheDir = DownloaderApplication.cacheDir
//    private val cacheProvider = Caching.getCachingProvider()
//    private val cacheManager = cacheProvider.cacheManager
//    private val cacheConfig = MutableConfiguration<String, String>().apply {
//        setTypes(String::class.java, String::class.java)
//        setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ONE_DAY))
//        isStoreByValue = false
//        isWriteThrough = true
//        isReadThrough = true
//        setCacheWriterFactory {
//            object : CacheWriter<String, String> {
//                override fun delete(key: Any) {
//                    if (!File(cacheDir, (key as String).sha256()).delete())
//                        throw CacheWriterException("Error deleting file $key")
//                }
//
//                override fun writeAll(entries: MutableCollection<Cache.Entry<out String, out String>>) {
//                    entries.forEach { write(it) }
//                }
//
//                override fun write(entry: Cache.Entry<out String, out String>) {
//                    val file = File(cacheDir, entry.key.sha256())
//                    file.absoluteFile.parentFile.mkdirs()
//                    file.createNewFile()
//                    file.writeText(entry.value)
//                }
//
//                override fun deleteAll(keys: MutableCollection<*>) {
//                    keys.filterNotNull().forEach { delete(it) }
//                }
//
//            }
//        }
//        setCacheLoaderFactory {
//            object : CacheLoader<String, String> {
//                override fun loadAll(keys: MutableIterable<String>): MutableMap<String, String> {
//                    return HashMap<String, String>().apply {
//                        this.putAll(keys.map { Pair(it, load(it)) })
//                    }
//                }
//
//                override fun load(key: String): String {
//                    val file = File(cacheDir, key.sha256())
//
//                    if (file.exists())
//                        return file.readText()
//                    else
//                        throw CacheLoaderException("Error getting file $key")
//                }
//
//            }
//        }
//    }
//    private val cache = cacheManager.createCache("CoverCache", cacheConfig)

    override fun getData(url: String?, ignoreCache: Boolean): String {
//        if (!ignoreCache) {
//            try {
//                return cache.get(url)
//            }catch(_: CacheLoaderException) {}
//        }
        val req = Request.Builder().url(url).build()
        val resp = DefaultHttpClient.newCall(req).execute()
        val result = resp.body().string()

//        cache.put(url, result)

        return result
    }

    override fun getData(url: String): String? {
        return getData(url, false)
    }
}