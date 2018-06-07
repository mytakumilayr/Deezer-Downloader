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

@JsonIgnoreProperties("type")
public class EditorialItem {

    /**
     * The editorial's Deezer id
     */
    public final Long id = null;

    /**
     * 	The editorial's name
     */
    public final String name = null;

    /**
     * 	The url of the editorial picture.
     */
    public final String picture = null;

    /**
     * The url of the editorial picture in size small.
     */
    public final String picture_small = null;

    /**
     * The url of the editorial picture in size medium.
     */
    public final String picture_medium = null;

    /**
     * The url of the editorial picture in size big.
     */
    public final String picture_big = null;
}
