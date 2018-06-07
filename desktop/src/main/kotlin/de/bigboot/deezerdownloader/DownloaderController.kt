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

import com.zeloon.deezer.domain.internal.AlbumId
import com.zeloon.deezer.domain.internal.ArtistId
import com.zeloon.deezer.domain.internal.PlaylistId
import com.zeloon.deezer.domain.internal.TrackId
import com.zeloon.deezer.domain.internal.search.SearchAutocomplete
import de.bigboot.deezerdownloader.*
import de.bigboot.deezerdownloader.i18n.CoreStrings
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import eu.stosdev.KotlinFXMLLoader
import eu.stosdev.bindFXML
import javafx.animation.Interpolator
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Callback
import javafx.util.Duration
import kotlinExtensions.deezer.Deezer
import kotlinExtensions.deezer.collect
import kotlinExtensions.java.I18n
import kotlinExtensions.javafx.enableDragging
import kotlinExtensions.javafx.stage
import kotlinExtensions.javafx.window
import kotlinExtensions.okhttp.DefaultHttpClient
import kotlinExtensions.sfl4j.error
import kotlinExtensions.sfl4j.trace
import nl.komponents.kovenant.CancelablePromise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import okhttp3.Request
import java.io.File
import java.net.URLDecoder

class DownloaderController {
    private val root: Node by bindFXML()
    private val closeLeft: Button by bindFXML()
    private val closeRight: Button by bindFXML()
    private val searchView: TextField by bindFXML()
    private val searchResultsTracksView: ListView<Entry> by bindFXML()
    private val searchResultsAlbumsView: ListView<Entry> by bindFXML()
    private val searchResultsPlaylistsView: ListView<Entry> by bindFXML()
    private val searchResultsContainer: TabPane by bindFXML()
    private val searchResultsArtistsView: ListView<Entry> by bindFXML()
    private val searchResultsViews: Array<ListView<Entry>> by lazy { arrayOf(
            searchResultsTracksView,
            searchResultsAlbumsView,
            searchResultsPlaylistsView,
            searchResultsArtistsView
    )}
    private val downloadsView: ListView<Download> by bindFXML()
    private val toolbarView: GridPane by bindFXML()
    private val searchingOverlayView: Pane by bindFXML()
    private val downloadSelectedBarView: ToolBar by bindFXML()
    private val logTab: Tab by bindFXML()
    private val downloader = Downloader()

    var downloadSelectedBarVisible: Boolean = false
        set(value) {
            field = value
            when (value) {
                true -> showDownloadSelectedBarAnimation.playFromStart()
                false -> hideDownloadSelectedBarAnimation.playFromStart()
            }
        }

    private val hideDownloadSelectedBarAnimation: TranslateTransition by lazy { TranslateTransition().apply {
        downloadSelectedBarView.translateYProperty().unbind()
        node = downloadSelectedBarView
        duration = Duration(250.0)
        interpolator = Interpolator.EASE_BOTH
        toYProperty().bind(downloadSelectedBarView.heightProperty())
    } }

    private val showDownloadSelectedBarAnimation: TranslateTransition by lazy { TranslateTransition().apply {
        downloadSelectedBarView.translateYProperty().unbind()
        node = downloadSelectedBarView
        duration = Duration(250.0)
        interpolator = Interpolator.EASE_BOTH
        toY = 0.0
    } }

    private var searchPromise: CancelablePromise<List<Entry>, Exception>? = null

    @Suppress("UNCHECKED_CAST")
    private fun getActiveResultsList(): ListView<Entry>
        = searchResultsContainer.selectionModel.selectedItem.content as ListView<Entry>


    fun initialize() {
        toolbarView.enableDragging()

        searchResultsViews.forEach {
            it.cellFactory = Callback {
                SearchResultCell().apply {
                    addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
                        if (event.clickCount > 1) {
                            val download = Download(it.items[index])
                            downloader.queueDownload(download)
                            downloadsView.items.add(download)
                            it.selectionModel.clearSelection()
                        }
                    }
                }
            }
            it.selectionModel.selectionMode = SelectionMode.MULTIPLE
            it.selectionModel.selectedItems.addListener (ListChangeListener { searchResultsSelectionChanged() })
        }


        downloadsView.cellFactory = Callback {
            DownloadCell().apply {
                addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
                    if (event.clickCount > 1) {
                        val download = downloadsView.items[index]
                        val outputFile = download.outputFile ?:
                                download.children?.find { it.outputFile != null }?.outputFile
                        if (outputFile != null) {
                            JavaFXApplication.instance?.hostServices?.showDocument(outputFile.parentFile.toString())
                            trace { "Opening folder: ${outputFile.parentFile}" }
                        }
                    }
                }
                onDeleteClicked {
                    downloader.cancelDownload(it)
                    downloadsView.items.remove(it)
                }
            }
        }

        val logLoader = KotlinFXMLLoader(this::class.java.getResource("/Log.fxml"), I18n.defaultBundle)
        val log: Node = logLoader.load()
        logTab.content = log

        run {
            val column = toolbarView.columnConstraints[3]
            var resize: Boolean = false

            column.maxWidth = DownloaderApplication.config.searchBarSize

            searchView.textProperty().addListener { _, _, new -> onSearchTextChanged(new) }
            searchView.addEventFilter(MouseEvent.ANY) { event ->

                when {
                    event.eventType == MouseEvent.MOUSE_MOVED -> {
                        if (event.x < 10) {
                            searchView.cursor = Cursor.H_RESIZE
                            event.consume()
                        } else {
                            searchView.cursor = Cursor.DEFAULT
                        }
                    }

                    event.eventType == MouseEvent.MOUSE_PRESSED && event.button == MouseButton.PRIMARY && event.x < 10 -> {
                        event.consume()
                        resize = true
                    }

                    event.eventType == MouseEvent.MOUSE_DRAGGED && event.button == MouseButton.PRIMARY && resize -> {
                        val bounds = searchView.localToScreen(searchView.boundsInLocal)
                        column.maxWidth = Math.max(0.0, bounds.maxX - event.screenX)
                        DownloaderApplication.config.searchBarSize = column.maxWidth
                        searchView.requestLayout()
                        event.consume()
                    }

                    event.eventType == MouseEvent.MOUSE_RELEASED && resize -> {
                        resize = false
                        event.consume()
                    }
                }
            }
        }

        arrayOf(closeLeft, closeRight).forEach { it.managedProperty().bind(it.visibleProperty()) }

        downloadSelectedBarView.translateYProperty().bind(downloadSelectedBarView.heightProperty())
        downloadSelectedBarView.clip = Rectangle().apply {
            heightProperty().bind(downloadSelectedBarView.heightProperty().subtract(downloadSelectedBarView.translateYProperty()))
            widthProperty().bind(downloadSelectedBarView.widthProperty())
        }

        if (DownloaderApplication.config.checkForUpdate) {
            task {
                Updater.checkForUpdate()
            } successUi {
                if (it != UpdateResult.NO_UPDATE) {
                    val updateDialog = Alert(Alert.AlertType.CONFIRMATION)
                    updateDialog.title = CoreStrings.app__title()
                    updateDialog.headerText = CoreStrings.update__available()
                    updateDialog.contentText = ""

                    updateDialog.dialogPane.expandableContent = WebView().apply {
                        engine.loadContent(it.changelog)
                    }

                    val buttonTypeYes = ButtonType(CoreStrings.update__yes(), ButtonBar.ButtonData.YES)
                    val buttonTypeNo = ButtonType(CoreStrings.update__no(), ButtonBar.ButtonData.CANCEL_CLOSE)
                    val buttonTypeDisableUpdates = ButtonType(CoreStrings.update__disable(), ButtonBar.ButtonData.NO)

                    updateDialog.buttonTypes.setAll(buttonTypeYes, buttonTypeNo, buttonTypeDisableUpdates)


                    when(updateDialog.showAndWait().orElse(null)) {
                        buttonTypeYes -> update(it)
                        buttonTypeDisableUpdates -> DownloaderApplication.config.checkForUpdate = false
                        else -> { /* Do nothing */ }
                    }
                }
            }
        }
    }

    private fun update(update: UpdateResult) {
        val dialog = Dialog<Void>().apply {
            title = CoreStrings.update__updating()
            initModality(Modality.WINDOW_MODAL)
            initOwner(root.stage)
            initStyle(StageStyle.UTILITY)
        }

        val progressBar = ProgressBar()
        val label = Label(CoreStrings.update__download()).apply {
            maxWidth = Double.MAX_VALUE
            alignment = Pos.CENTER
        }

        val content = VBox().apply {
            children.addAll(progressBar, label)
        }

        var cancel = false

        dialog.dialogPane.apply {
            this.content = content
            buttonTypes.addAll(ButtonType.CANCEL)

            lookupButton(ButtonType.CANCEL).pressedProperty().addListener { _, _, _ ->
                cancel = true
            }
        }


        promiseOnUi {
            dialog.show()
        } then {
            val request = Request.Builder().url(update.link).build()
            val response = DefaultHttpClient.newCall(request).execute()

            val jarDir = DownloaderApplication.exefile.parentFile
            val updateFile = File(jarDir, "DeezerDownloader.upd")

            updateFile.outputStream().use { output ->
                response.body().byteStream().use { input ->
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(8*1024)
                    var bytes = input.read(buffer)
                    while (bytes >= 0 && !cancel) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes

                        Platform.runLater {
                            progressBar.progress = bytesCopied.toDouble() / response.body().contentLength().toDouble()
                        }

                        bytes = input.read(buffer)
                    }
                }
            }

            if (cancel) {
                throw RuntimeException("Update cancelled by user")
            }

            updateFile
        } alwaysUi {
            dialog.close()
        } success {
            if (DownloaderApplication.isExe) {
                ProcessBuilder(it.absolutePath).apply {
                    directory(it.parentFile)
                }.start()
            } else {
                val jre = System.getProperty("java.home")
                val extension = if (System.getProperty("os.name").contains("win", true)) ".exe" else ""
                val java = File(File(jre, "bin"), "java$extension")

                ProcessBuilder(java.absolutePath, "-jar", it.absolutePath).apply {
                    directory(it.parentFile)
                }.start()
            }

            File(URLDecoder.decode(this::class.java.protectionDomain.codeSource.location.path, "UTF-8")).deleteOnExit()
            System.exit(0)
        } fail {
            error(it) { "Update Error" }
        }

    }

    private fun searchResultsSelectionChanged() {
        downloadSelectedBarVisible = getActiveResultsList().selectionModel.selectedItems.isNotEmpty()
    }

    private fun onSearchTextChanged(text: String) {
        searchPromise?.cancel(SearchCancelledException())

        if (text.length < 3) {
            searchingOverlayView.isVisible = false
            return
        }

        data class SearchResult(
                val tracks: List<Entry.Track>,
                val albums: List<Entry.Album>,
                val playlists: List<Entry.Playlist>,
                val artists: List<Entry.Artist>
        )

        searchingOverlayView.isVisible = true
        val promise = task {

            val result = Deezer.search(SearchAutocomplete(text)).collect(30)

            val urlRegex: Regex by lazy { "(?:https?://)?(?:www\\.)?deezer\\.com/(artist|album|playlist|track)/(\\d+)(?:\\?.+)?".toRegex() }

            val match = urlRegex.find(text)
            if (match != null) {
                val (type, id) = match.destructured
                when (type) {
                    "track" -> result.tracks.add(0, Deezer.get(TrackId(id.toLong())))
                    "album" -> result.albums.add(0, Deezer.get(AlbumId(id.toLong())))
                    "artist" -> result.artists.add(0, Deezer.get(ArtistId(id.toLong())))
                    "playlist" -> result.playlists.add(0, Deezer.get(PlaylistId(id.toLong())))
                }
            }

            SearchResult (
                    result.tracks.map { Entry.Track(it) },
                    result.albums.map { Entry.Album(it) },
                    result.playlists.map { Entry.Playlist(it) },
                    result.artists.map { Entry.Artist(it) }
            )

        } fail {
            when (it) {
                is SearchCancelledException -> { }
                else -> {
                    it.printStackTrace()
                    searchingOverlayView.isVisible = false
                }
            }
        } successUi {
            searchResultsTracksView.items = FXCollections.observableList(it.tracks)
            searchResultsAlbumsView.items = FXCollections.observableList(it.albums)
            searchResultsPlaylistsView.items = FXCollections.observableList(it.playlists)
            searchResultsArtistsView.items = FXCollections.observableList(it.artists)
            searchingOverlayView.isVisible = false
        }
        @Suppress("UNCHECKED_CAST")
        searchPromise = promise as CancelablePromise<List<Entry>, Exception>
    }

    fun onCloseButtonPressed() {
        val window = root.scene.window

        if (window is Stage)
            window.close()

        Platform.exit()
    }

    fun onDownloadSelectedPressed() {
        val resultsView = getActiveResultsList()
        val selectedItems = ArrayList(resultsView.selectionModel.selectedItems)
        resultsView.selectionModel.clearSelection()
        selectedItems.forEach {
            val download = Download(it)
            downloader.queueDownload(download)
            downloadsView.items.add(download)
        }

    }
    fun onPauseButtonClicked(event: ActionEvent) {
        val button = event.source as Button
        val icon = button.graphic as FontAwesomeIconView
        val tooltip = button.tooltip

        downloader.paused = icon.glyphName == "PAUSE"

        icon.glyphName = if (downloader.paused) "PLAY" else "PAUSE"
        tooltip.text = if (downloader.paused) CoreStrings.main__resume_downloads() else CoreStrings.main__pause_downloads()
    }

    fun onCleanButtonClicked() {
        downloadsView.items.removeIf {
            it.state == Download.State.Finished || it.state == Download.State.Error || it.state == Download.State.Skipped
        }
    }

    fun onSettingsButtonClicked() {
        val settingsDialog = Dialog<Unit>()

        val loader = KotlinFXMLLoader(this::class.java.getResource("/Settings.fxml"), I18n.defaultBundle)

        settingsDialog.dialogPane.padding = Insets(0.0)
        settingsDialog.dialogPane.content = loader.load()


        settingsDialog.isResizable = true

        settingsDialog.dialogPane.buttonTypes.add(ButtonType.OK)

        settingsDialog.initOwner(root.window)
        settingsDialog.title = CoreStrings.settings__title()

        settingsDialog.dialogPane.prefHeight = 650.0
        settingsDialog.showAndWait()

        val controller = loader.getController<SettingsController>()
        if (controller.restartRequired) {
            val restartDialog = Alert(Alert.AlertType.CONFIRMATION)
            restartDialog.title = CoreStrings.app__title()
            restartDialog.headerText = CoreStrings.settings__needs_restart__header(CoreStrings.app__title())
            restartDialog.contentText = CoreStrings.settings__needs_restart__content(CoreStrings.app__title())

            restartDialog.buttonTypes.setAll(ButtonType.YES, ButtonType.NO)

            when(restartDialog.showAndWait().orElse(null)) {
                ButtonType.YES -> JavaFXApplication.instance?.restart()
                else -> { /* Do nothing */ }
            }
        }
    }

    private class SearchCancelledException: RuntimeException()

}