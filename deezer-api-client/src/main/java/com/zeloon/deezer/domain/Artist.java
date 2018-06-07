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
import com.zeloon.deezer.domain.internal.ArtistId;

@JsonIgnoreProperties({
        "type",
        "time_add"
})
public class Artist {

    /**
     * The artist's Deezer id
     */
    public final ArtistId id = null;

    /**
     * The artist's name
     */
    public final String name = null;

    /**
     * The url of the artist on Deezer
     */
    public final String link = null;

    /**
     * The share link of the artist on Deezer
     */
    public final String share = null;

    /**
     * The url of the artist picture. Add 'size' parameter to the url to change size. Can be 'small', 'medium', 'big'
     */
    public final String picture = null;

    /**
     * The url of the artist picture in size small.
     */
    public final String picture_small = null;

    /**
     * The url of the artist picture in size medium.
     */
    public final String picture_medium = null;

    /**
     * The url of the artist picture in size big.
     */
    public final String picture_big = null;

    /**
     * The url of the artist picture in size xl.
     */
    public final String picture_xl = null;

    /**
     * The number of artist's albums
     */
    public final Integer nb_album = null;

    /**
     * The number of artist's fans
     */
    public final Long nb_fan = null;

    /**
     * true if the artist has a smartradio
     */
    public final Boolean radio = null;

    /**
     * API Link to the top of this artist
     */
    public final String tracklist = null;

    /**
     *  The role of this artist in the compilation
     */
    public final String role = null;
}












