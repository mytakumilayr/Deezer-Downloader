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

public abstract class Search<T> {
    public final String text;
    public final SearchOrder searchOrder;
    public final Class<T> resultType;
    public final String searchPath;

    public Search(Class<T> resultType, String searchPath, String text) {
        this(resultType, searchPath, text, null);
    }

    public Search(Class<T> resultType, String searchPath, String text, SearchOrder searchOrder) {
        this.text = text;
        this.resultType = resultType;
        this.searchOrder = searchOrder;
        this.searchPath = searchPath;
    }
}
