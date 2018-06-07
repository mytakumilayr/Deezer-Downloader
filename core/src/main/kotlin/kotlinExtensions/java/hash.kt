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

package kotlinExtensions.java

import kotlinExtensions.kotlin.Hex
import java.security.MessageDigest

enum class HashType(val algorithmName: String) {
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256")
}

fun ByteArray.hash(hashType: HashType): String {
    try {
        return Hex.encode(MessageDigest.getInstance(hashType.algorithmName).digest(this))
    } catch (ex: Exception) {
        throw RuntimeException("no UTF-8 decoder available", ex)
    }
}

fun ByteArray.md5() = hash(HashType.MD5)
fun ByteArray.sha1() = hash(HashType.SHA1)
fun ByteArray.sha256() = hash(HashType.SHA256)

fun String.hash(hashType: HashType): String = this.toByteArray().hash(hashType)
fun String.md5() = hash(HashType.MD5)
fun String.sha1() = hash(HashType.SHA1)
fun String.sha256() = hash(HashType.SHA256)