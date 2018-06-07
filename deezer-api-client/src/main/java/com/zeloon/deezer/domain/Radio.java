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

@JsonIgnoreProperties({"type", "time_add"})
public class Radio {
        /**
         * The radio deezer ID
         */
        public final Integer id = null;

        /**
         * The radio title
         */
        public final String title = null;

        /**
         * The radio title
         */
        public final String description = null;

        /**
         * The share link of the radio on Deezer
         */
        public final String share = null;

        /**
         * The url of the radio picture. Add 'size' parameter to the url to change size. Can be 'small', 'medium', 'big'
         */
        public final String picture = null;

        /**
         * The url of the radio picture in size small.
         */
        public final String picture_small = null;

        /**
         * The url of the radio picture in size medium.
         */
        public final String picture_medium = null;

        /**
         * The url of the radio picture in size big.
         */
        public final String picture_big = null;

        /**
         * The url of the radio picture in size xl.
         */
        public final String picture_xl = null;

        /**
         * API Link to the tracklist of this radio
         */
        public final String tracklist = null;
}
