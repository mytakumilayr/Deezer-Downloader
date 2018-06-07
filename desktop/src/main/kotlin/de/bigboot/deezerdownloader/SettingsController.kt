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
import eu.stosdev.bindFXML
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.util.StringConverter
import kotlinExtensions.javafx.window
import org.controlsfx.control.PopOver
import org.controlsfx.control.PropertySheet
import org.controlsfx.property.BeanPropertyUtils
import java.io.File


class SettingsController {
    private val root: BorderPane by bindFXML()
    private val nativeCheckboxView: CheckBox by bindFXML()
    private val maxQualityView: Slider by bindFXML()
    private val imageSize: Slider by bindFXML()
    private val imageSizeCustom: Spinner<Int> by bindFXML()
    private val covertype: ComboBox<Config.CoverType> by bindFXML()
    private val outputFolderView: TextField by bindFXML()
    private val existingFileBehaviour: ComboBox<Config.ExistingFileBehaviour> by bindFXML()
    private val trackTemplateView: TextField by bindFXML()
    private val albumTemplateView: TextField by bindFXML()
    private val playlistTemplateView: TextField by bindFXML()
    private val toplistTemplateView: TextField by bindFXML()
    private val filenameHelp: Label by bindFXML()
    private val version: Label by bindFXML()
    private val embedGenres: CheckBox by bindFXML()
    private val genreSeparator: TextField by bindFXML()
    private val simpleSettingsPane: Pane by bindFXML()
    private val advancedSettingsPane: Pane by bindFXML()
    private val advancedSettingsButton: ToggleButton by bindFXML()
    private val settingsTable: PropertySheet by bindFXML()

    var restartRequired = false; private set
    private var advancedSettingsChanged = false

    fun initialize() {
        maxQualityView.labelFormatter = object: StringConverter<Double>() {
            override fun toString(`object`: Double): String =
                when(`object`.toInt()) {
                    1 -> "128"
                    2 -> "256"
                    3 -> "320"
                    else -> error("Invalid value: $`object`")
                }


            override fun fromString(string: String): Double =
                    when(string) {
                        "128" -> 1.0
                        "256" -> 2.0
                        "320" -> 3.0
                        else -> error("Invalid value: $string")
                    }

        }

        imageSize.labelFormatter = object: StringConverter<Double>() {
            override fun toString(`object`: Double): String =
                when(`object`.toInt()) {
                    1 -> CoreStrings.settings__size_small()
                    2 -> CoreStrings.settings__size_medium()
                    3 -> CoreStrings.settings__size_big()
                    4 -> CoreStrings.settings__size_xl()
                    5 -> CoreStrings.settings__size_custom()
                    else -> error("Invalid value: $`object`")
                }


            override fun fromString(string: String): Double =
                    when(string) {
                        CoreStrings.settings__size_small() -> 1.0
                        CoreStrings.settings__size_medium() -> 2.0
                        CoreStrings.settings__size_big() -> 3.0
                        CoreStrings.settings__size_xl() -> 4.0
                        CoreStrings.settings__size_custom() -> 5.0
                        else -> error("Invalid value: $string")
                    }

        }

        val filenameHelpText= """
            ${CoreStrings.settings__filenames__help()}
            ${CoreStrings.settings__filenames__directories()}

            ${CoreStrings.settings__filenames__replacements()}
            %track%             => ${CoreStrings.settings__filenames__track()}
            %album%             => ${CoreStrings.settings__filenames__album()}
            %artist%            => ${CoreStrings.settings__filenames__artist()}
            %trackNumber%       => ${CoreStrings.settings__filenames__trackNumber()}
            %diskNumber%        => ${CoreStrings.settings__filenames__diskNumber()}
            %year%              => ${CoreStrings.settings__filenames__year()}
            %albumYear%         => ${CoreStrings.settings__filenames__albumYear()}
            %albumArtist%       => ${CoreStrings.settings__filenames__albumArtist()}
            %playlist%          => ${CoreStrings.settings__filenames__playlist()}
            %playlistNumber%    => ${CoreStrings.settings__filenames__playlistNumber()}
            %toplistNumber%     => ${CoreStrings.settings__filenames__toplistNumber()}
            %bpm%               => ${CoreStrings.settings__filenames__bpm()}
            %releaseDate%       => ${CoreStrings.settings__filenames__releaseDate(config.dateFormat)}
        """.trimIndent()

        val popoverContent = Label(filenameHelpText).apply {
            padding = Insets(5.0)
            font = Font.font("Monospaced", font.size)
        }
        val popover = PopOver(popoverContent).apply {

        }
        filenameHelp.setOnMouseClicked { popover.show(filenameHelp) }

        version.text = DownloaderApplication.version.toString()

        covertype.items = FXCollections.observableArrayList(Config.CoverType.values().asList().filter { it.text != null })
        covertype.converter = object: StringConverter<Config.CoverType>() {
            override fun toString(obj: Config.CoverType) = obj.text!!.invoke()

            override fun fromString(string: String)
                    = Config.CoverType.values().firstOrNull { string == it.text!!.invoke() } ?: Config.CoverType.Other

        }

        existingFileBehaviour.items = FXCollections.observableArrayList(Config.ExistingFileBehaviour.values().asList())
        existingFileBehaviour.converter = object: StringConverter<Config.ExistingFileBehaviour>() {
            override fun toString(obj: Config.ExistingFileBehaviour) = obj.text()

            override fun fromString(string: String)
                    = Config.ExistingFileBehaviour.values().firstOrNull { string == it.text() } ?: Config.ExistingFileBehaviour.Overwrite

        }

        genreSeparator.parent.disableProperty().bind(embedGenres.selectedProperty().not())

        settingsTable.isModeSwitcherVisible = false

        initListeners()

        showSimpleSettings()
    }

    private fun initListeners() {
        nativeCheckboxView.selectedProperty().addListener { _, _, new -> if (config.useFancy == new) { config.useFancy = !new; restartRequired = true } }

        outputFolderView.textProperty().addListener{ _, _, new -> config.outputFolder = File(new) }
        existingFileBehaviour.valueProperty().addListener { _, _, new -> config.existingFileBehaviour = new }
        maxQualityView.valueProperty().addListener { _, _, new -> config.maxDownloadQuality = StreamQuality.values()[new.toInt()-1] }
        imageSize.valueProperty().addListener { _, _, new -> config.imageSize = Config.ImageSize.values()[new.toInt()-1] }
        imageSizeCustom.valueProperty().addListener { _, _, new -> config.imageSizeCustom = new }
        imageSizeCustom.focusedProperty().addListener({ _, _, focused -> if (!focused) imageSizeCustom.increment(0) })
        covertype.valueProperty().addListener { _, _, new -> config.coverType = new }

        trackTemplateView.textProperty().addListener { _, _, new -> config.trackTemplate = new }
        albumTemplateView.textProperty().addListener { _, _, new -> config.albumTemplate = new }
        playlistTemplateView.textProperty().addListener { _, _, new -> config.playlistTemplate = new }
        toplistTemplateView.textProperty().addListener { _, _, new -> config.artistTemplate = new }

        embedGenres.selectedProperty().addListener { _, _, new -> config.embedGenres = new }
        genreSeparator.textProperty().addListener { _, _, new -> config.genreSeparator = new }

        advancedSettingsButton.selectedProperty().addListener { _, _, new -> if (new) showAdvancedSettings() else showSimpleSettings() }
    }

    fun onOutputFolderChangePressed() {
        DirectoryChooser().apply {
            initialDirectory = config.outputFolder.apply { this.mkdirs() }
            title = "Select output folder"
        }.showDialog(root.window)?.apply {
            outputFolderView.text = this.canonicalPath
        }
    }

    fun showSimpleSettings() {
        val config = config

        nativeCheckboxView.isSelected = !config.useFancy

        outputFolderView.text = config.outputFolder.path
        existingFileBehaviour.value = config.existingFileBehaviour
        maxQualityView.value = config.maxDownloadQuality.ordinal.toDouble() + 1.0
        imageSize.value = config.imageSize.ordinal.toDouble() + 1.0
        imageSizeCustom.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2000, config.imageSizeCustom, 1)
        imageSizeCustom.disableProperty().bind(Bindings.notEqual(imageSize.valueProperty(), Config.ImageSize.Custom.ordinal.toDouble() + 1.0, 0.5))
        covertype.value = config.coverType

        trackTemplateView.text = config.trackTemplate
        albumTemplateView.text = config.albumTemplate
        playlistTemplateView.text = config.playlistTemplate
        toplistTemplateView.text = config.artistTemplate

        embedGenres.isSelected = config.embedGenres
        genreSeparator.text = config.genreSeparator

        simpleSettingsPane.isVisible = true
        advancedSettingsPane.isVisible = false
    }

    fun showAdvancedSettings() {
        if (!config.showAdvancedSettings) {
            val alert = Alert(AlertType.CONFIRMATION)
            alert.title = CoreStrings.settings__advanced_warning__title()
            alert.headerText = CoreStrings.settings__advanced_warning__header()
            alert.contentText = CoreStrings.settings__advanced_warning__content()

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                config.showAdvancedSettings = true
            } else {
                advancedSettingsButton.isSelected = false
                return
            }
        }

        val entries = BeanPropertyUtils.getProperties(config, {
            !it.readMethod.isAnnotationPresent(Config.Hidden::class.java)
        })
        settingsTable.items.clear()
        settingsTable.items.addAll(entries)

        advancedSettingsPane.isVisible = true
        simpleSettingsPane.isVisible = false
    }

}