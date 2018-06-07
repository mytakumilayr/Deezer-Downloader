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
package com.zeloon.deezer.domain.internal.search;

import com.zeloon.deezer.domain.Artist;
import com.zeloon.deezer.domain.Artists;

public class SearchArtist extends Search<Artists> {
    private static final String SEARCH_PATH = "/artist";

    public SearchArtist(String text) {
        super(Artists.class, SEARCH_PATH, text);
    }

    public SearchArtist(String text, SearchOrder searchOrder) {
        super(Artists.class, SEARCH_PATH, text, searchOrder);
    }

}
