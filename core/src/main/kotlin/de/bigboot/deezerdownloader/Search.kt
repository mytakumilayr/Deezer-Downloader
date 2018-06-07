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
import kotlinExtensions.deezer.Deezer
import kotlinExtensions.deezer.collect
import nl.komponents.kovenant.CancelablePromise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi

class Search {
    private var searchPromise: CancelablePromise<List<Entry>, Exception>? = null
    private val listeners: MutableList<Listener> = ArrayList()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun doSearch(text: String) {
        searchPromise?.cancel(SearchCancelledException())

        if (text.length < 3) {
            listeners?.forEach { it.searchFinished(SearchResult()) }
            return
        }

        listeners.forEach { it.searchStarted() }
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

        } fail { error ->
            when (error) {
                is SearchCancelledException -> { }
                else -> listeners.forEach { it.searchFailed(error) }
            }
        } success { result ->
            listeners.forEach { it.searchFinished(result) }
        }
        @Suppress("UNCHECKED_CAST")
        searchPromise = promise as CancelablePromise<List<Entry>, Exception>
    }

    data class SearchResult(
            val tracks: List<Entry.Track>,
            val albums: List<Entry.Album>,
            val playlists: List<Entry.Playlist>,
            val artists: List<Entry.Artist>
    ) {
        constructor(): this(ArrayList(0), ArrayList(0), ArrayList(0), ArrayList(0))
    }

    interface Listener {
        fun searchStarted()
        fun searchFinished(result: SearchResult)
        fun searchFailed(e: Throwable)
    }

    private class SearchCancelledException: RuntimeException()
}