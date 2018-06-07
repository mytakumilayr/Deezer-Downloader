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

import java.io.File
import java.util.*

private val illegalChars: IntArray by lazy {
    intArrayOf(34, 60, 62, 124, 0,
            1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22,
            23, 24, 25, 26, 27, 28, 29,
            30, 31, 58, 42, 63, 92, 47)
            .apply { Arrays.sort(this) }
}

fun String.sanitizeFileName(): String {
    val cleanName = StringBuilder()
    val len = this.codePointCount(0, this.length)
    (0..len - 1)
            .map { this.codePointAt(it) }
            .filter { Arrays.binarySearch(illegalChars, it) < 0 }
            .forEach { cleanName.appendCodePoint(it) }
    return cleanName.toString()
}