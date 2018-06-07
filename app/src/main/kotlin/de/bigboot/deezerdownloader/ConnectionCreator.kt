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

import cn.dreamtobe.filedownloader.OkHttp3Connection
import com.liulishuo.filedownloader.connection.FileDownloadConnection
import com.liulishuo.filedownloader.util.FileDownloadHelper
import kotlinExtensions.okhttp.DefaultHttpClient
import java.io.InputStream

class ConnectionCreator: FileDownloadHelper.ConnectionCreator {
    private class DecryptedOkHttp3Connection(url: String, val trackId: String): OkHttp3Connection(url, DefaultHttpClient) {
        override fun getInputStream(): InputStream {
            val input = super.getInputStream()
            val decrypted = DeezerDecryptStream(input, trackId)
            return decrypted.buffered()
        }
    }

    override fun create(url: String): FileDownloadConnection {
        val trackID = url.substringBefore(";")
        val url = url.substringAfter(";")

        return DecryptedOkHttp3Connection(url, trackID)
    }
}