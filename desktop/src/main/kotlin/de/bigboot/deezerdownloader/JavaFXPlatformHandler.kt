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

import de.bigboot.deezerdownloader.DownloaderApplication.config
import de.bigboot.deezerdownloader.i18n.CoreStrings
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.DialogPane
import kotlinExtensions.OS
import kotlinExtensions.okhttp.DefaultHttpClient
import nl.komponents.kovenant.ui.promiseOnUi
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.reflect.KClass

private class JavaFXPlatformDownloader: PlatformDownloader {
    override fun download(trackID: String, url: String, outputFile: File, listener: PlatformDownloader.Listener?) {
        try {
            val request = Request.Builder().url(url).build()
            val response = DefaultHttpClient.newCall(request).execute()

            if (response.code() == 403)
                throw DeezerPrivateException.TrackUnavailableException()

            outputFile.parentFile?.mkdirs()
            outputFile.outputStream().buffered().use { output ->
                DeezerDecryptStream(response.body().byteStream(), trackID).use { decrypted ->
                    val len = response.body().contentLength()
                    var progressed = 0
                    var read = 0
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    listener?.init()

                    do {
                        read = decrypted.read(buffer)

                        if (read >= 0) {
                            output.write(buffer, 0, read)
                        }

                        progressed += read
                        listener?.progress(progressed.toDouble() / len)
                    } while (read >= 0)

                    listener?.finished()
                }
            }
        } catch (e: Exception) {
            listener?.error(DeezerPrivateException.DownloadException(trackID, e))
        } finally {
            listener?.progress(1.0)
        }
    }

}

class JavaFXPlatformHandler: DataHandler, ImageHandler, DownloadHandler, LoggerHandler, DialogHandler {
    val logger: Logger by lazy { LoggerFactory.getLogger((this::class as KClass<*>).java) }

    override val dataPath: File
        get() = File(when {
            OS.isWindows && System.getenv("APPDATA") != null -> "${System.getenv("APPDATA")}/DeezerDownloader/"
            (OS.isUnix || OS.isMac) && System.getProperty("user.home") != null  -> "${System.getProperty("user.home")}/.config/DeezerDownloader/"
            else -> throw RuntimeException("Unknown OS Detected, unable to find a suitable data directory")
        })
    override val applicationPath: File
        get() = JavaFXApplication.exefile.parentFile
    override val downloadPath: File
        get() = File(applicationPath, "Downloads")
    override val cacheDir: File
        get() = when {
            config.portable -> File(applicationPath, "cache").apply { mkdirs(); deleteOnExit() }
            else -> Files.createTempDirectory("DeezerDownloaderCache").toFile().apply { deleteOnExit() }
        }


    override fun createDownloader(): PlatformDownloader {
        return JavaFXPlatformDownloader()
    }


    override fun trace(msg: () -> Any?) { if (logger.isTraceEnabled) logger.trace(msg.invoke().toString()) }
    override fun debug(msg: () -> Any?) { if (logger.isDebugEnabled) logger.debug(msg.invoke().toString()) }
    override fun info(msg: () -> Any?) { if (logger.isInfoEnabled) logger.info(msg.invoke().toString()) }
    override fun warn(msg: () -> Any?) { if (logger.isWarnEnabled) logger.warn(msg.invoke().toString()) }
    override fun error(msg: () -> Any?) { if (logger.isErrorEnabled) logger.error(msg.invoke().toString()) }

    override fun trace(t: Throwable, msg: () -> Any?) { if (logger.isTraceEnabled) logger.trace(msg.invoke().toString(), t) }
    override fun debug(t: Throwable, msg: () -> Any?) { if (logger.isDebugEnabled) logger.debug(msg.invoke().toString(), t) }
    override fun info(t: Throwable, msg: () -> Any?) { if (logger.isInfoEnabled) logger.info(msg.invoke().toString(), t) }
    override fun warn(t: Throwable, msg: () -> Any?) { if (logger.isWarnEnabled) logger.warn(msg.invoke().toString(), t) }
    override fun error(t: Throwable, msg: () -> Any?) { if (logger.isErrorEnabled) logger.error(msg.invoke().toString(), t) }


    override fun getImageInfo(binaryData: ByteArray): ImageHandler.ImageInfo {
        val image = ImageIO.read(ByteArrayInputStream(binaryData))
        val mimetype = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(binaryData))

        return ImageHandler.ImageInfo(mimetype, image.width, image.height)
    }

    override fun createImage(data: ByteArray): ImageHandler.Image {
        val image = ImageIO.read(ByteArrayInputStream(data))

        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)

        return ImageHandler.Image(out.toByteArray(), ImageHandler.ImageInfo("image/png", image.width, image.height))
    }

    override fun createScaledImage(data: ByteArray, w: Int, h: Int): ImageHandler.Image {
        val image = ImageIO.read(ByteArrayInputStream(data))
        val scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH)

        val scaledImage = BufferedImage(w, h, image.type)
        val g2d = scaledImage.createGraphics()
        g2d.drawImage(scaled, 0, 0, null)
        g2d.dispose()

        val out = ByteArrayOutputStream(1024 * 1024)
        ImageIO.write(scaledImage, "png", out)

        return ImageHandler.Image(out.toByteArray(), ImageHandler.ImageInfo("image/png", w, h))

    }

    override fun askForExistingFileBehaviour(file: File): Config.ExistingFileBehaviour {
        return promiseOnUi {
            Platform.runLater {  }
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.dialogPane.applyCss()
            val graphic = alert.dialogPane.graphic
            val save = SimpleBooleanProperty(false)
            alert.dialogPane = object : DialogPane() {
                override fun createDetailsButton(): Node = CheckBox().apply {
                    text = CoreStrings.file_exists__save()
                    save.bind(selectedProperty())
                }
            }
            alert.title = CoreStrings.file_exists__title()
            alert.contentText = CoreStrings.file_exists__text(file.name)
            val buttonTypeOverwrite = ButtonType(Config.ExistingFileBehaviour.Overwrite.text())
            val buttonTypeSkip = ButtonType(Config.ExistingFileBehaviour.Skip.text())
            alert.buttonTypes.setAll(buttonTypeOverwrite, buttonTypeSkip)
            alert.dialogPane.expandableContent = Group()
            alert.dialogPane.isExpanded = true
            alert.dialogPane.graphic = graphic
            val result = when(alert.showAndWait().orElse(null)) {
                buttonTypeOverwrite -> Config.ExistingFileBehaviour.Overwrite
                buttonTypeSkip -> Config.ExistingFileBehaviour.Skip
                else -> Config.ExistingFileBehaviour.Ask
            }
            if (save.get())
                DownloaderApplication.config.existingFileBehaviour = result
            return@promiseOnUi result
        }.get()
    }
}