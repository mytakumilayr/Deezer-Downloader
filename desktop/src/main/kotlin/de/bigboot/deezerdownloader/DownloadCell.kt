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

import de.bigboot.deezerdownloader.Download
import eu.stosdev.KotlinFXMLLoader
import eu.stosdev.bindFXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ProgressBar
import nl.komponents.kovenant.ui.promiseOnUi

class DownloadCell: ListCell<Download>() {
    private val downloadRootView: Node by lazy {
        KotlinFXMLLoader(this::class.java.getResource("/DownloadItem.fxml")).apply { setController(this@DownloadCell) }.load<Node>()
    }

    private val titleView: Label by bindFXML()
    private val progressView: ProgressBar by bindFXML()
    private val deleteBtn: Button by bindFXML()
    private var deleteBtnCallback: ((download: Download)->Unit)? = null

    private var currentDownload: Download? = null


    @Suppress("UNUSED")
    fun initialize() {
        deleteBtn.setOnMouseClicked {
            currentDownload?.let {
                deleteBtnCallback?.invoke(it)
            }
        }
    }

    private fun getTitle(download: Download): String {
        val entry = download.entry
        return when (entry) {
            is Entry.Track -> entry.track.title
            is Entry.Album -> entry.album.title
            is Entry.Artist -> entry.artist.name
            is Entry.Playlist -> entry.playlist.title
        }
    }

    override fun updateItem(item: Download?, empty: Boolean) {
        super.updateItem(item, empty)

        if (currentDownload == item)
            return

        currentDownload?.let { unbindDownload(it) }

        if (isEmpty || item == null) {
            graphic = null
            currentDownload = null
            return
        }

        currentDownload = item


        graphic = downloadRootView


        item.onUpdate (this::updateDownloadState)
        updateDownloadState(item)
    }

    private fun updateDownloadState(download: Download) {
        promiseOnUi {

            Download.State.values().forEach {
                progressView.styleClass.remove("state-${it.name.toLowerCase()}")
            }
            progressView.styleClass.add("state-${download.state.name.toLowerCase()}")


            var title = getTitle(download)
            if (item.count > 1) {
                title = "$title (${item.countFinished}/${item.count})"
            }
            titleView.text = title

            progressView.progress = download.progress

            if (download.state == Download.State.Error) {
                progressView.progress = 1.0
            }
        }
    }

    private fun unbindDownload(item: Download) {
        item.removeCallback(this::updateDownloadState)
    }

    fun onDeleteClicked(cb: (download: Download)->Unit) {
        deleteBtnCallback = cb
    }
}