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
import com.zeloon.deezer.domain.internal.TrackId;

import java.util.Calendar;
import java.util.List;

@JsonIgnoreProperties({"type", "time_add"})
public class Track {

    /**
     * The track's Deezer id
     */
    public final TrackId id = null;

    /**
     * true if the track is readable in the player for the current user
     */
    public final Boolean readable = null;

    /**
     * The track's fulltitle
     */
    public final String title = null;

    /**
     * The track's short title
     */
    public final String title_short = null;

    /**
     * The track version
     */
    public final String title_version = null;

    /**
     * The track unseen status
     */
    public final Boolean unseen = null;

    /**
     * The track isrc
     */
    public final String isrc = null;

    /**
     * The url of the track on Deezer
     */
    public final String link = null;

    /**
     * The share link of the track on Deezer
     */
    public final String share = null;

    /**
     * The track's duration in seconds
     */
    public final Integer duration = null;

    /**
     * The position of the track in its album
     */
    public final Integer track_position = null;

    /**
     * The track's album's disk number
     */
    public final Integer disk_number = null;

    /**
     * The track's Deezer rank
     */
    public final Integer rank = null;

    /**
     * The track's release date
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    public final Calendar release_date = null;

    /**
     * Whether the track contains explicit lyrics
     */
    public final Boolean explicit_lyrics = null;

    /**
     * The url of track's preview file. This file contains the first 30 seconds of the track
     */
    public final String preview = null;

    /**
     * Beats per minute
     */
    public final Double bpm = null;

    /**
     * Signal strength
     */
    public final Double gain = null;

    /**
     * List of countries where the track is available
     */
    public final String[] available_countries = null;

    /**
     * Return an alternative readable track if the current track is not readable
     */
    public final Track alternative = null;

    /**
     * Return a list of contributors on the track
     */
    public final List<Artist> contributors = null;

    /**
     * artist object containing : id, name, link, share, picture, picture_small, picture_medium, picture_big, nb_album, nb_fan, radio, tracklist, role
     */
    public final Artist artist = null;

    /**
     * album object containing : id, title, link, cover, cover_small, cover_medium, cover_big, release_date
     */
    public final Album album = null;
}
