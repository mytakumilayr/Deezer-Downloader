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

import eu.stosdev.bindFXML
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.*
import javafx.util.Callback
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.io.*


class LoggerConsoleController {
    private val listViewLog: ListView<String> by bindFXML()
    private val toggleButtonAutoScroll: ToggleButton by bindFXML()
    private val choiceBoxLogLevel: ChoiceBox<Level> by bindFXML()

    fun handleRemoveSelected() {
        listViewLog.items.removeAll(listViewLog.selectionModel.selectedItems)
    }

    fun handleClearLog() {
        listViewLog.items.clear()
    }

    fun handleSaveLog() {
        val sb = StringBuilder()
        for (item in listViewLog.items) {
            sb.append(item)
            sb.append("\n")
        }

        val writer = BufferedWriter(FileWriter("log.txt"))
        writer.write(sb.toString())
        writer.close()

        val alert = Alert(Alert.AlertType.INFORMATION).apply {
            title = "Ok!"
            headerText = "log.txt gespeichert"
        }
        alert.showAndWait()
    }

    fun initialize() {
        listViewLog.selectionModel.selectionMode = SelectionMode.MULTIPLE
        val loggerContext = LogManager.getContext(false) as LoggerContext
        val loggerConfiguration = loggerContext.configuration
        val loggerConfig = loggerConfiguration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        /* ChoiceBox füllen */
        for (level in Level.values()) {
            choiceBoxLogLevel.items.add(level)
        }
        /* Aktuellen LogLevel in der ChoiceBox als Auswahl setzen */
        choiceBoxLogLevel.selectionModel.select(loggerConfig.level)
        choiceBoxLogLevel.selectionModel.selectedItemProperty().addListener { _, _, newLevel ->
            loggerConfig.level = newLevel
            loggerContext.updateLoggers() // übernehme aktuellen LogLevel
        }

        listViewLog.cellFactory = Callback { LogStringCell() }

        /* den Origial System.out Stream in die ListView umleiten */
        val pOut = PipedOutputStream()
        System.setOut(PrintStream(pOut))
        var pIn: PipedInputStream? = null
        try {
            pIn = PipedInputStream(pOut)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val reader = BufferedReader(InputStreamReader(pIn!!))

        val task = object : Task<Void>() {
            @Throws(Exception::class)
            override fun call(): Void? {
                while (!isCancelled) {
                    try {
                        val line = reader.readLine()
                        if (line != null) {
                            Platform.runLater {
                                listViewLog.items.add(line)

                                /* Auto-Scroll + Select */
                                if (toggleButtonAutoScroll.selectedProperty().get()) {
                                    listViewLog.scrollTo(listViewLog.items.size - 1)
                                    listViewLog.selectionModel.select(listViewLog.items.size - 1)
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                return null
            }
        }
        val thread = Thread(task)
        thread.isDaemon = true
        thread.start()
    }
}