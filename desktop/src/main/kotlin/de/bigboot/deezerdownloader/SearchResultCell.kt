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

import com.zeloon.deezer.domain.Album
import com.zeloon.deezer.domain.Artist
import com.zeloon.deezer.domain.Playlist
import com.zeloon.deezer.domain.Track
import eu.stosdev.KotlinFXMLLoader
import eu.stosdev.bindOptionalFXML
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import kotlinExtensions.deezer.durationString
import nl.komponents.kovenant.CancelablePromise
import nl.komponents.kovenant.ui.successUi

class SearchResultCell: ListCell<Entry>() {
    private val entryRootView: Node by lazy {
        KotlinFXMLLoader(this::class.java.getResource("/EntryItem.fxml")).apply { setController(this@SearchResultCell) }.load<Node>()
    }
    private val headerRootView: Node by lazy {
        KotlinFXMLLoader(this::class.java.getResource("/HeaderItem.fxml")).apply { setController(this@SearchResultCell) }.load<Node>()
    }

    private val titleView: Label? by bindOptionalFXML()
    private val subtitleView: Label? by bindOptionalFXML()
    private val infoView: Label? by bindOptionalFXML()
    private val additionalInfoView: Label? by bindOptionalFXML()
    private val imageView: ImageView? by bindOptionalFXML()

    private var currentEntry: Entry? = null
    private val headerTitleView: Label? by bindOptionalFXML()

    private var loadImagePromise: CancelablePromise<Image, Exception>? = null

    override fun updateItem(item: Entry?, empty: Boolean) {
        super.updateItem(item, empty)

        if (isEmpty || item == null) {
            graphic = null
            currentEntry = null
            return
        }

        if (currentEntry == item)
            return

        currentEntry = item

        when (item) {
            is Entry.Track -> bindTrack(item.track)
            is Entry.Album -> bindAlbum(item.album)
            is Entry.Artist -> bindArtist(item.artist)
            is Entry.Playlist -> bindPlaylist(item.playlist)
        }
    }

    private fun bindTrack(track: Track)
            = bindEntry(track.title, track.album.title, track.artist.name, track.durationString, track.album.cover)

    private fun bindAlbum(album: Album)
            = bindEntry(album.title, album.artist.name, "${album.nb_tracks?:"0"} tracks", imageURL = album.cover)

    private fun bindArtist(artist: Artist)
            = bindEntry(artist.name, "Top20", imageURL = artist.picture)

    private fun bindPlaylist(playlist: Playlist)
            = bindEntry(playlist.title, playlist.creator.name, "${playlist.nb_tracks?:"0"} tracks", imageURL = playlist.picture)

    private fun bindEntry(title: String, subtitle: String = "", info: String = "", additionalInfo: String = "", imageURL: String? = null) {
        graphic = entryRootView

        titleView?.text = title
        subtitleView?.text = subtitle
        infoView?.text = additionalInfo
        additionalInfoView?.text = info
        imageView?.image = null

        loadImagePromise?.cancel(RuntimeException("Cancelled"))

        loadImagePromise = imageURL?.let(::loadCover)
        loadImagePromise?.successUi { imageView?.image = it }

    }

    private fun bindHeader(title: String) {
        graphic = headerRootView

        headerTitleView?.text = title
    }
}