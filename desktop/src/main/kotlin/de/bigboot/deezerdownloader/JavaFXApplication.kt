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

import Utf8ResourceBundle
import de.bigboot.deezerdownloader.*
import de.bigboot.deezerdownloader.DownloaderApplication.cacheDir
import de.bigboot.deezerdownloader.DownloaderApplication.config
import de.bigboot.deezerdownloader.DownloaderApplication.version
import de.bigboot.deezerdownloader.i18n.CoreStrings
import eu.stosdev.KotlinFXMLLoader
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinExtensions.OS
import kotlinExtensions.java.I18n
import kotlinExtensions.sfl4j.error
import kotlinExtensions.sfl4j.info
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.jfx.startKovenant
import nl.komponents.kovenant.jfx.stopKovenant
import nl.komponents.kovenant.task
import java.io.File


class JavaFXApplication : javafx.application.Application() {
    private lateinit var stage: Stage
    private lateinit var root: BorderPane
    private val platformHandler = JavaFXPlatformHandler()

    override fun init() {
        instance = this

        DataHandler.register(platformHandler)
        ImageHandler.register(platformHandler)
        DownloadHandler.register(platformHandler)
        LoggerHandler.register(platformHandler)
        DialogHandler.register(platformHandler)

        I18n.defaultBundle = Utf8ResourceBundle.getBundle("strings")
    }

    override fun start(primaryStage: Stage) {
        try {
            info { "Starting DeezerDownloader Version: $version" }
            startKovenant()

            if (!DownloaderApplication.config.disclaimerAccepted) {
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = CoreStrings.main__disclaimer_title()
                alert.headerText = CoreStrings.main__disclaimer_header()
                alert.dialogPane.content = Label(CoreStrings.main__disclaimer())

                val result = alert.showAndWait()
                if (result.get() == ButtonType.OK) {
                    DownloaderApplication.config.disclaimerAccepted = true
                } else {
                    System.exit(0)
                }
            }

            Kovenant.context {
                workerContext.dispatcher {
                    concurrentTasks = 1
                }
            }

            task {
                if (cacheDir.listFiles().isNotEmpty()) {
                    info { "Cleaning cache..." }
                    cacheDir.listFiles().forEach { it.delete() }
                }
            }

            stage = primaryStage

            loadStage()

        } catch (ex: Exception) {
            error(ex) { "Error while starting DeezerDownloader" }
        }

    }

    private fun loadStage() {

        val loader = KotlinFXMLLoader(this::class.java.getResource("/Downloader.fxml"), I18n.defaultBundle)
        root = loader.load<BorderPane>()

        stage.scene = Scene(root)
        stage.scene.fill = null

        applyStyle()

        stage.title = CoreStrings.app__title()
        stage.icons.addAll(listOf(16, 32, 64, 128, 256, 512).map { Image(this::class.java.getResourceAsStream("/icon-$it.png")) })
        stage.show()
    }

    private fun applyStyle() {
        when(config.useFancy) {
            true -> {
                root.styleClass.apply { remove("native"); add("fancy") }
                stage.initStyle(StageStyle.TRANSPARENT)
            }
            false -> {
                root.styleClass.apply { remove("fancy"); add("native") }
                stage.initStyle(StageStyle.DECORATED)
            }
        }
        root.styleClass.add("os-${OS.name}")

        if (config.useFancy) {
            stage.setOnShown {
                val border = root.border.insets.right.toInt()

                root.prefWidth = config.width.toDouble() + border
                root.prefHeight = config.height.toDouble() + border

                stage.sizeToScene()

                ResizeHelper.addResizeListener(stage) {
                    this.border = border

                    minWidth = root.minWidth.toInt()
                    minHeight = root.minHeight.toInt()
                }

                root.widthProperty().addListener { _, _, value -> config.width = value.toInt() - border }
                root.heightProperty().addListener { _, _, value -> config.height = value.toInt() - border }
            }

        } else {
            root.prefWidth = config.width.toDouble()
            root.prefHeight = config.height.toDouble()

            root.widthProperty().addListener { _, _, value -> config.width = value.toInt() }
            root.heightProperty().addListener { _, _, value -> config.height = value.toInt() }
        }
    }

    fun restart() {
        stage.close()

        stage = Stage()

        loadStage()
    }

    override fun stop() {
        super.stop()
        stopKovenant(true)
        System.exit(0)
    }

    companion object {
        val logDialog: Dialog<Unit> by lazy {
            Dialog<Unit>().apply {
                dialogPane.padding = Insets(0.0)
                dialogPane.content = KotlinFXMLLoader(JavaFXApplication::class.java.getResource("Log.fxml")).load()

                dialogPane.buttonTypes.add(ButtonType.CLOSE)

                title = "Log"
            }
        }

        var exefile: File = File("")
        var isExe: Boolean = false

        var instance: JavaFXApplication? = null; private set
    }
}
