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

package kotlinExtensions

enum class OS(val osname: String) {
    Windows("win"), Mac("mac"), Unix("unix"), Solaris("solaris"), Other("other"), Android("android");

    companion object {
        private val osname: String by lazy { System.getProperty("os.name").toLowerCase() }
        val current: OS by lazy {
            try {
                Class.forName("android.app.Activity")
                Android
            } catch (e: ClassNotFoundException) {
                when {
                    osname.contains("win") -> Windows
                    osname.contains("mac") -> Mac
                    osname.contains("nix") || osname.contains("nux") || osname.contains("aix") -> Unix
                    osname.contains("sunos") -> Solaris
                    else -> Other
                }
            }


        }
        val name: String = current.osname

        val isWindows = current == Windows
        val isMac = current == Mac
        val isUnix = current == Unix
        val isSolaris = current == Solaris
        val isAndroid = current == Android
        val isUnknown = current == Other
    }
}

