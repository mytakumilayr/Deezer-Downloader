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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zeloon.deezer.domain.internal.PlaylistId;
import com.zeloon.deezer.utils.ReflectionHelper;

@JsonIgnoreProperties({"type", "tracklist", "creation_date"})
public class Playlist {

    /**
     * The playlist's Deezer id
     */
    public final PlaylistId id = null;

    /**
     * The playlist's title
     */
    public final String title = null;

    /**
     * The playlist description
     */
    public final String description = null;

    /**
     * The playlist's duration (seconds)
     */
    public final Integer duration = null;
    @JsonProperty(value = "public")

    /**
     * If the playlist is public or not
     */
    public final Boolean is_public = null;

    /**
     * If the playlist is the love tracks playlist
     */
    public final Boolean is_loved_track = null;

    /**
     * If the playlist is collaborative or not
     */
    public final Boolean collaborative = null;

    /**
     * The playlist's rate
     */
    public final Integer rating = null;

    /**
     * Nb tracks not seen
     */
    public final Integer unseen_track_count = null;

    /**
     * The number of playlist's fans
     */
    public final Integer fans = null;

    /**
     * The url of the playlist on Deezer
     */
    public final String link = null;

    /**
     * The share link of the playlist on Deezer
     */
    public final String share = null;

    /**
     * The url of the playlist's cover. Add 'size' parameter to the url to change size. Can be 'small', 'medium', 'big'
     */
    public final String picture = null;

    /**
     * The url of the playlist's cover in size small.
     */
    public final String picture_small = null;

    /**
     * The url of the playlist's cover in size medium.
     */
    public final String picture_medium = null;

    /**
     * The url of the playlist's cover in size big.
     */
    public final String picture_big = null;

    /**
     * The url of the playlist's cover in size xl.
     */
    public final String picture_xl = null;

    /**
     * The checksum for the track list
     */
    public final String checksum = null;

    /**
     * user object containing : id, name
     */
    public final Creator creator = null;

    /**
     * list of track
     */
    public final Tracks tracks = null;

    public final Integer nb_tracks = null;

    private void setUser(Creator user) {
        ReflectionHelper.setFinalField(this, "creator", user);
    }
}

