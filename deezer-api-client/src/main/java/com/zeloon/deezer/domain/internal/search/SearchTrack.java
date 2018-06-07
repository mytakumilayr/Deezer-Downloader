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

import com.zeloon.deezer.domain.Tracks;

public class SearchTrack extends Search<Tracks> {
    private static final String SEARCH_PATH = "";

    public SearchTrack(String text) {
        super(Tracks.class, SEARCH_PATH, text);
    }

    public SearchTrack(String text, SearchOrder searchOrder) {
        super(Tracks.class, SEARCH_PATH, text, searchOrder);
    }

}
