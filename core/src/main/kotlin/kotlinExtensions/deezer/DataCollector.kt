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

package kotlinExtensions.deezer

import com.zeloon.deezer.domain.*
import com.zeloon.deezer.domain.Autocomplete
import java.util.*

fun <T> Data<T>.collect(max: Int = Int.MAX_VALUE): List<T> {
    val result = ArrayList<T>(total?:data?.size?:0)
    var cur: Data<T>? = this

    while (cur != null && result.size < max) {
        result.addAll(cur.data)
        @Suppress("DEPRECATION")
        cur = Deezer.getNextData(cur, this.javaClass)
    }

    while (result.size > max) {
        result.removeAt(result.lastIndex)
    }

    return result
}

fun <T> Data<T>.collectAll(): List<T> {
    return collect()
}

data class Autocomplete (
        val tracks: ArrayList<Track>,
        val albums: ArrayList<Album>,
        val artists: ArrayList<Artist>,
        val playlists: ArrayList<Playlist>,
        val podcasts: ArrayList<Podcast>
)

fun Autocomplete.collect(max: Int = Int.MAX_VALUE): kotlinExtensions.deezer.Autocomplete {
    val tracks = ArrayList<Track>(max)
    val albums = ArrayList<Album>(max)
    val artists = ArrayList<Artist>(max)
    val playlists = ArrayList<Playlist>(max)
    val podcasts = ArrayList<Podcast>(max)

    var cur: Autocomplete? = this
    var count = 0

    while (cur != null && count < max) {
        tracks.addAll(cur.tracks.data)
        albums.addAll(cur.albums.data)
        artists.addAll(cur.artists.data)
        playlists.addAll(cur.playlists.data)
        podcasts.addAll(cur.podcasts.data)

        cur = Deezer.getNext(cur)
        count += 10
    }

    return Autocomplete(tracks, albums, artists, playlists, podcasts)
}