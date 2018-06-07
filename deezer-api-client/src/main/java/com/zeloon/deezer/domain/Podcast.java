package com.zeloon.deezer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties("type")
public class Podcast {

    /**
     * The podcast's Deezer id
     */
    public final Integer id = null;

    /**
     * The podcast's title
     */
    public final String title = null;

    /**
     * The podcast's description
     */
    public final String description = null;

    /**
     * If the podcast is available or not
     */
    public final Boolean available = null;

    /**
     * The playlist's rate
     */
    public final Integer rating = null;

    /**
     * The number of playlist's fans
     */
    public final Integer fans = null;

    /**
     * The url of the podcast on Deezer
     */
    public final String link = null;

    /**
     * The share link of the podcast on Deezer
     */
    public final String share = null;

    /**
     * The url of the podcast's cover.
     */
    public final String picture = null;

    /**
     * The url of the podcast's cover in size small.
     */
    public final String picture_small = null;

    /**
     * The url of the podcast's cover in size medium.
     */
    public final String picture_medium = null;

    /**
     * The url of the podcast's cover in size big.
     */
    public final String picture_big = null;

    /**
     * The url of the podcast's cover in size xl.
     */
    public final String picture_xl = null;
}

