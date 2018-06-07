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

package kotlinExtensions.kotlin

object Hex {
    private val chars = "0123456789abcdef"

    fun encode(input: ByteArray): String {
        val output = StringBuilder(2 * input.size)
        for (value in input) {
            output.append(chars[(value.toInt() and 0xF0) shr 4]).append(chars[(value.toInt() and 0x0F)])
        }
        return output.toString()
    }

    fun decode(input: String): ByteArray {
        if (input.length < 2) {
            return ByteArray(0)
        } else {
            val len = input.length / 2
            val output = ByteArray(len)

            var index = 0
            while (index < len) {
                output[index] = Integer.parseInt(input.substring(index * 2, index * 2 + 2), 16).toByte()
                index++
            }
            return output
        }
    }
}