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

package kotlinExtensions.okhttp

import de.bigboot.deezerdownloader.DownloaderApplication
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.net.Proxy

val DefaultHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder().apply {
        cache(Cache(DownloaderApplication.cacheDir, 50 * 1024 * 1024 /* 50 MiB*/))
        addInterceptor(UserAgentInterceptor(DownloaderApplication.config.userAgent))
        if (DownloaderApplication.config.proxyType != Proxy.Type.DIRECT)
            proxy(Proxy(DownloaderApplication.config.proxyType, DownloaderApplication.config.proxyAddress))
    }.build()
}