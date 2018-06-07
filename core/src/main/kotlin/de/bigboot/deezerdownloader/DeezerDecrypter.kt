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

import kotlinExtensions.kotlin.Hex
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.MessageFormat
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

enum class StreamQuality(val formatID: Int, val bitrate: Int) {
    Low(1, 128), Medium(5, 256), High(3, 320)
}

class DecryptException(cause: Throwable) : RuntimeException(cause)

class DeezerDecrypter {
    var input: InputStream? = null
    var output: OutputStream? = null
    var trackID: String? = null

    fun init(): IInitializedDecrypter = InitializedDecrypter(this)

    companion object {
        fun getDownloadURL(puid: String, format: Int, id: String, mediaVersion: Int): String {
            val proxyLetter = puid.substring(0, 1)
            val separator = "¤"
            var data: String = puid + separator + format + separator + id + separator + mediaVersion
            val dataHash = md5(data)
            data = aes(dataHash + separator + data + separator).substring(0 until 160)

            return MessageFormat.format("http://e-cdn-proxy-{0}.deezer.com/mobile/1/{1}", proxyLetter, data)
        }
    }
}

interface IInitializedDecrypter {
    /**
     * Decrypt the next chunk of data
     * @return the amount of bytes read, -1 if EOF or error, 0 if no data was available
     */
    fun decrypt(): Int

    var error: Exception?
}

class DeezerDecryptStream(val input: InputStream, val trackID: String): InputStream() {
    var totalRead: Long = 0
    var totalWritten: Long = 0

    private val chunkSize = 2048
    private val intervalChunk = 3

    val buffer = ByteBuffer.allocate(chunkSize).apply { position(limit()) }
    private var chunk = ByteArray(chunkSize)

    private val bin = input.buffered()

    private var currentChunk = 0
    private var eof = false

    fun fillBuffer() {
        if(buffer.hasRemaining() || eof)
            return

        buffer.clear()

        var bufferOffset = 0

        while (bufferOffset < buffer.limit() && !eof) {

            var offset = 0

            while (offset < chunkSize && !eof) {
                val read = bin.read(chunk, offset, chunkSize - offset)

                if (read >= 0) {
                    offset += read
                    totalRead += read
                } else {
                    eof = true
                }
            }


            if (currentChunk % intervalChunk == 0)
                chunk = decryptBlowfish(chunk, getBlowfishKey(trackID))

            buffer.put(chunk, 0, offset)
            bufferOffset += offset
        }
        buffer.flip()

        currentChunk++

    }
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        if (!buffer.hasRemaining()) fillBuffer()
        if (eof && !buffer.hasRemaining()) return -1

        val available = Math.min(len, buffer.remaining())
        buffer.get(b, off, available)
        totalWritten += available

        return available
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(): Int {
        if (!buffer.hasRemaining()) fillBuffer()
        if (eof && !buffer.hasRemaining()) return -1
        return buffer.get().toInt()
    }

    override fun close() {
        return input.close()
    }

    override fun reset() {
        return input.reset()
    }

    override fun available(): Int {
        if (eof) return -1
        return input.available() + buffer.remaining()
    }

    override fun skip(p0: Long): Long {
        return super.skip(p0)
    }

}

private  class InitializedDecrypter(data: DeezerDecrypter): IInitializedDecrypter {
    private var input = data.input ?: throw IllegalStateException("Input is not set")
    private var output = data.output ?: throw IllegalStateException("Output is not set")
    private var trackID = data.trackID ?: throw IllegalStateException("TrackID is not set")

    private val chunkSize = 2048
    private val intervalChunk = 3
    private var chunk = ByteArray(chunkSize)

    private val bin = input.buffered()
    private val bout = output.buffered()

    private var currentChunk = 0
    private var offset = 0
    private var eof = false

    override var error: Exception? = null

    override fun decrypt(): Int {
        if (eof || error != null)
            return -1

        var read = bin.read(chunk, offset, chunkSize-offset)

        if (read < 0) {
            eof = true
            read = 0

            try {
                bout.close()
            } catch (ignore: IOException) {}
        }

        offset += read

        if (offset < chunkSize && !eof)
            return 0

        if (currentChunk % intervalChunk == 0)
            chunk = decryptBlowfish(chunk, getBlowfishKey(trackID))

        try {
            bout.write(chunk, 0, offset)
        } catch (e: IOException) {
            error = e
            return -1
        }

        offset = 0
        currentChunk++

        return read
    }
}

private fun getBlowfishKey(songId: String): ByteArray {
    val hash = md5(songId)
    val part1 = hash.substring(0, 16)
    val part2 = hash.substring(16, 32)
    val data = arrayOf("g4el58wc0zvf9na1", part1, part2)
    val keyStr = getXor(data, 16)
    return keyStr.toByteArray()
}

private fun getXor(data: Array<String>, len: Int): String {
    var result = ""
    for (i in 0..len - 1) {
        var character = data[0][i].toInt()
        for (j in 1..data.size - 1) {
            character = character xor data[j][i].toInt()
        }
        result += character.toChar()
    }
    return result
}

private fun decryptBlowfish(data: ByteArray, key: ByteArray): ByteArray {
    try {
        val keySpec = SecretKeySpec(key, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish/CBC/NoPadding")
        cipher.init(2, keySpec, IvParameterSpec(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7)))
        return cipher.doFinal(data)
    } catch (e: Exception) {
        e.printStackTrace()
        return ByteArray(data.size)
    }

}

private fun md5(s: String): String {
    try {
        val md5digest: MessageDigest = MessageDigest.getInstance("MD5")
        return Hex.encode(md5digest.digest(s.toByteArray(Charsets.ISO_8859_1)))
    } catch (ex: Exception) {
        throw RuntimeException("no UTF-8 decoder available", ex)
    }

}

private fun aes(clearText: String): String {
    val exception: Exception
    try {
        val skeySpec = SecretKeySpec("jo6aey6haid2Teih".toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(1, skeySpec)
        val encrypted = cipher.doFinal(clearText.toByteArray(Charsets.ISO_8859_1))
        return Hex.encode(encrypted).toLowerCase()
    } catch (e: NoSuchAlgorithmException) {
        exception = e
    } catch (e: NoSuchPaddingException) {
        exception = e
    } catch (e: IllegalBlockSizeException) {
        exception = e
    } catch (e: BadPaddingException) {
        exception = e
    } catch (e: InvalidKeyException) {
        exception = e
    } catch (e: UnsupportedEncodingException) {
        exception = e
    }

    throw DecryptException(exception)
}