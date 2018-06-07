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

import java.io.File
import java.util.*


object DownloaderApplication {
    private var dataHandler = DataHandler.Instance

    val config: Config = let {
        if (File(dataHandler.applicationPath, "config.json").exists()) {
            Config.create(File(dataHandler.applicationPath, "config.json").absolutePath).apply { portable = true }
        } else {
//            Config.create(when {
//                OS.isAndroid -> "/data/data/de.bigboot.deezerdownloader/config.json" // TODO: Do something about this...
//                OS.isWindows && System.getenv("APPDATA") != null -> "${System.getenv("APPDATA")}/DeezerDownloader/config.json"
//                (OS.isUnix || OS.isMac) && System.getProperty("user.home") != null  -> "${System.getProperty("user.home")}/.config/DeezerDownloader.json"
//                else -> "config.json"
//            })
            Config.create(File(dataHandler.dataPath, "config.json").absolutePath)
        }
    }

    val cacheDir = when {
        config.portable -> File(dataHandler.applicationPath, ".cache").apply { mkdirs(); deleteOnExit() }
        else -> dataHandler.cacheDir.apply { mkdir(); deleteOnExit() }
    }

    val version: Version by lazy {
        val bundle = ResourceBundle.getBundle("app")
        val version = bundle.getString("version")

        Version(version)
    }

    var exefile: File = File("")
    var isExe: Boolean = false

    var instance: DownloaderApplication? = null; private set

    private val closeListeners = ArrayList<()->Unit>()
    fun onClose(cb: ()->Unit) {
        closeListeners.add(cb)
    }
}
