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

import java.util.List;

@JsonIgnoreProperties({
        "ads",
        "hosts",
        "upload_token",
        "user_token",
        "upload_token_lifetime",
        "has_podcasts",
        "pop"
})
public class Infos {

    /**
     * Iso value of current country
     */
    public final String country_iso = null;

    /**
     * The current country
     */
    public final String country = null;

    /**
     * true if Deezer is opened in the current country
     */
    public final Boolean open = null;

    /**
     * An array of available offers in the current country
     */
    public final List<Offer> offers = null;
}
