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

class Version(version: String) : Comparable<Version> {
    private val versionString: String

    init {
        var resultVersion = version
        if (resultVersion.startsWith("v"))
            resultVersion = resultVersion.substring(1)

        if (!resultVersion.matches("[0-9]+(\\.[0-9]+)*".toRegex()) && !resultVersion.startsWith("git-"))
            throw IllegalArgumentException("Invalid version format: $resultVersion")

        versionString = resultVersion
    }

    override fun compareTo(other: Version): Int {
        if (this.versionString.startsWith("git-") || other.versionString.startsWith("git-"))
            return 0

        val thisParts = this.versionString.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        val thatParts = other.versionString.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        val length = Math.max(thisParts.size, thatParts.size)
        for (i in 0..length - 1) {
            val thisPart = if (i < thisParts.size)
                Integer.parseInt(thisParts[i])
            else
                0
            val thatPart = if (i < thatParts.size)
                Integer.parseInt(thatParts[i])
            else
                0
            if (thisPart < thatPart)
                return -1
            if (thisPart > thatPart)
                return 1
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.let { it::class.java } != this::class.java) return false

        other as Version

        return this.compareTo(other) == 0
    }

    override fun hashCode(): Int = toString().hashCode()

    override fun toString(): String = versionString
}