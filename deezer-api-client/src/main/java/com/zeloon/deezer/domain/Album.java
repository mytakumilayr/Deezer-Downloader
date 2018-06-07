/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zeloon.deezer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zeloon.deezer.domain.internal.AlbumId;

import java.util.Calendar;
import java.util.List;
@JsonIgnoreProperties({
        "genre",
        "type",
        "approved_type",
        "language",
        "time_add"
})
public class Album {

    /**
     * The Deezer album id
     */
    public final AlbumId id = null;

    /**
     * The album title
     */
    public final String title = null;

    /**
     * The album UPC
     */
    public final String upc = null;

    /**
     * The url of the album on Deezer
     */
    public final String link = null;

    /**
     * The share link of the album on Deezer
     */
    public final String share = null;

    /**
     * The url of the album's cover. Add 'size' parameter to the url to change size. Can be 'small', 'medium', 'big'
     */
    public final String cover = null;

    /**
     * The url of the album's cover in size small.
     */
    public final String cover_small = null;

    /**
     * The url of the album's cover in size medium.
     */
    public final String cover_medium = null;

    /**
     * The url of the album's cover in size big.
     */
    public final String cover_big = null;

    /**
     * The url of the album's cover in size xl.
     */
    public final String cover_xl = null;

    /**
     * The album's first genre id (You should use the genre list instead). NB : -1 for not found
     */
    public final String genre_id = null;

    /**
     * List of genre object
     */
    public final Genres genres = null;

    /**
     * The album's label name
     */
    public final String label = null;

    /**
     * The album's duration (seconds)
     */
    public final Integer duration = null;

    /**
     * The number of album's Fans
     */
    public final Integer fans = null;

    /**
     * The album's rating
     */
    public final Integer rating = null

    /**
     * The album's release date
     */;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    public final Calendar release_date = null;

    /**
     * The record type of the album (EP / ALBUM / etc..)
     */
    public final String record_type = null;

    /**
     * Return an alternative album object if the current album is not available
     */
    public final Boolean available = null;

    public final Album alternative = null;

    /**
     * API Link to the tracklist of this album
     */
    public final String tracklist = null;

    /**
     * Whether the album contains explicit lyrics
     */
    public final Boolean explicit_lyrics = null;

    /**
     * Return a list of contributors on the album
     */
    public final List<Artist> contributors = null;

    /**
     * artist object containing : id, name, picture, picture_small, picture_medium, picture_big
     */
    public final Artist artist = null;

    /**
     * list of track
     */
    public final Tracks tracks = null;

    public final Integer nb_tracks = null;
}



