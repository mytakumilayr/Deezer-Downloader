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

import java.util.Date;

@JsonIgnoreProperties("type")
public class User {

    /**
     * The user's Deezer ID
     */
    public final Long id = null;

    /**
     * The user's Deezer nickname
     */
    public final String name = null;

    /**
     * The user's last name
     */
    public final String lastname = null;

    /**
     * The user's first name
     */
    public final String firstname = null;

    /**
     * The user's email
     */
    public final String email = null;

    /**
     * The user's status
     */
    public final Integer status = null;

    /**
     * The user's birthday
     */
    public final Date birthday = null;

    /**
     * The user's inscription date
     */
    public final Date inscription_date = null;

    /**
     * The user's gender : F or M
     */
    public final Character gender = null;

    /**
     * The url of the profil for the user on Deezer
     */
    public final String link = null;

    /**
     * The url of the user's profil picture. Add 'size' parameter to the url to change size. Can be 'small', 'medium', 'big'
     */
    public final String picture = null;

    /**
     * The url of the user's profil picture in size small.
     */
    public final String picture_small = null;

    /**
     * The url of the user's profil picture in size medium.
     */
    public final String picture_medium = null;

    /**
     * The url of the user's profil picture in size big.
     */
    public final String picture_big = null;

    /**
     * The user's country
     */
    public final String country = null;

    /**
     * The user's language
     */
    public final String lang = null;

    /**
     * API Link to the flow of this user
     */
    public final String tracklist = null;
}