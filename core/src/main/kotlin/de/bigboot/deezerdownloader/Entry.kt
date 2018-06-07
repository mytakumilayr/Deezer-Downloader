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

sealed class Entry(val type: Type) {
    class Track(val track: com.zeloon.deezer.domain.Track): Entry(Type.Track)
    class Album(val album: com.zeloon.deezer.domain.Album): Entry(Type.Album)
    class Playlist(val playlist: com.zeloon.deezer.domain.Playlist): Entry(Type.Playlist)
    class Artist(val artist: com.zeloon.deezer.domain.Artist): Entry(Type.Artist)

    enum class Type {
        Track, Album, Playlist, Artist
    }
}

val Entry.deezerId get() = when (this) {
    is Entry.Album -> "album:${this.album.id.value}"
    is Entry.Artist -> "artist:${this.artist.id.value}"
    is Entry.Playlist -> "playlist:${this.playlist.id.value}"
    is Entry.Track -> "track:${this.track.id.value}"
}