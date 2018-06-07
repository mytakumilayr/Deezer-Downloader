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

import de.bigboot.deezerdownloader.JavaFXApplication.Companion.exefile
import javafx.application.Application
import java.io.File
import java.net.URLDecoder
import java.util.logging.LogManager






class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            JavaFXApplication.exefile = File(if (System.getProperty("launch4j.exefile", null) != null) {
                JavaFXApplication.isExe = true
                System.getProperty("launch4j.exefile")
            } else {
                URLDecoder.decode(JavaFXApplication::class.java.protectionDomain.codeSource.location.path, "UTF-8")
            })

            if (File("DeezerDownloader.upd").isFile)
                File("DeezerDownloader.upd").delete()

            if (exefile.extension == "upd") {
                val extension = if (JavaFXApplication.isExe) ".exe" else ".jar"
                val targetFile = File(exefile.parentFile, "DeezerDownloader$extension")

                if (targetFile.exists())
                    targetFile.delete()

                exefile.copyTo(targetFile)

                if (JavaFXApplication.isExe) {
                    ProcessBuilder(targetFile.absolutePath).apply {
                        directory(targetFile.parentFile)
                    }.start()
                } else {
                    val jre = System.getProperty("java.home")
                    val javaExtension = if (System.getProperty("os.name").contains("win", true)) ".exe" else ""
                    val java = File(File(jre, "bin"), "java$javaExtension")

                    ProcessBuilder(java.absolutePath, "-jar", targetFile.absolutePath).apply {
                        directory(targetFile.parentFile)
                    }.start()
                }

                exefile.deleteOnExit()
                System.exit(0)
            }

            LogManager.getLogManager().readConfiguration("org.jaudiotagger.level=WARNING\n".byteInputStream())
            Application.launch(JavaFXApplication::class.java, *args)
        }
    }
}

