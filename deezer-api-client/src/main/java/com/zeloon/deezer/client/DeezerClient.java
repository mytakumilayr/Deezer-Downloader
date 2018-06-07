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
package com.zeloon.deezer.client;

import com.zeloon.deezer.domain.*;
import com.zeloon.deezer.domain.internal.*;
import com.zeloon.deezer.domain.internal.search.*;
import com.zeloon.deezer.io.ResourceConnection;
import com.zeloon.deezer.service.DeezerRestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DeezerClient {

    public static final String PREFIX_ALBUM = "album";
    public static final String PREFIX_ARTIST = "artist";
    public static final String PREFIX_USER = "user";
    public static final String PREFIX_PLAYLIST = "playlist";
    public static final String PREFIX_TRACK = "track";

    private DeezerRestTemplate baseService;

    public DeezerClient(ResourceConnection resourceConnection) {
        baseService = new DeezerRestTemplate(resourceConnection);
    }

    public Album get(final AlbumId albumId) {
        return baseService.get(PREFIX_ALBUM, albumId.value, Album.class);
    }

    public Comments getComments(final AlbumId albumId) {
        return baseService.get(PREFIX_ALBUM, albumId.value, "comments", Comments.class);
    }

    public Fans getFans(final AlbumId albumId) {
        return baseService.get(PREFIX_ALBUM, albumId.value, "fans", Fans.class);
    }

    public Tracks getTracks(final AlbumId albumId) {
        return baseService.get(PREFIX_ALBUM, albumId.value, "tracks", Tracks.class);
    }

    public Artist get(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, Artist.class);
    }

    public Top getTop(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, "top", Top.class);
    }

    public Albums getAlbums(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, "albums", Albums.class);
    }

    public Comments getComments(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, "comments", Comments.class);
    }

    public Fans getFans(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, "fans", Fans.class);
    }

    public Related getRelated(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, "related", Related.class);
    }

    public ArtistRadio getRadio(final ArtistId artistId) {
        return baseService.get(PREFIX_ARTIST, artistId.value, "radio", ArtistRadio.class);
    }

    public Editorial getEditorial() {
        return baseService.get("editorial", Editorial.class);
    }

    public Comment get(final CommentId commentId) {
        return baseService.get("comment", commentId.value, Comment.class);
    }

    public Genres getGenres() {
        return baseService.get("genre", Genres.class);
    }

    public Infos getInfos() {
        return baseService.get("infos", Infos.class);
    }

    public Track get(final TrackId trackId) {
        return baseService.get(PREFIX_TRACK, trackId.value, Track.class);
    }

    public Playlist get(final PlaylistId playlistId) {
        return baseService.get(PREFIX_PLAYLIST, playlistId.value, Playlist.class);
    }

    public Comments getComments(final PlaylistId playlistId) {
        return baseService.get(PREFIX_PLAYLIST, playlistId.value, "comments", Comments.class);
    }

    public Fans getFans(final PlaylistId playlistId) {
        return baseService.get(PREFIX_PLAYLIST, playlistId.value, "fans", Fans.class);
    }

    public Tracks getTracks(final PlaylistId playlistId) {
        return baseService.get(PREFIX_PLAYLIST, playlistId.value, "tracks", Tracks.class);
    }

    public Radio getRadio() {
        return baseService.get("radio", Radio.class);
    }

    public RadioGenres getRadioGenres() {
        return baseService.get("radio/genres", RadioGenres.class);
    }

    public Radio getRadioTop() {
        return baseService.get("radio/top", Radio.class);
    }

    public <T> T search(final Search<T> search) {
        return baseService.get(getSearchQuery(search), search.resultType);
    }

    public <T extends Data> T getNextData(T data, Class<T> clazz) {
        if (data.next == null)
            return null;

        return baseService.get(data.next, clazz);
    }

    public Tracks getNext(Tracks tracks) {
        return getNextData(tracks, Tracks.class);
    }

    public Albums getNext(Albums albums) {
        return getNextData(albums, Albums.class);
    }

    public Artists getNext(Artists artists) {
        return getNextData(artists, Artists.class);
    }

    public Playlists getNext(Playlists playlists) {
        return getNextData(playlists, Playlists.class);
    }

    public Users getNext(Users users) {
        return getNextData(users, Users.class);
    }

    public Autocomplete getNext(Autocomplete autocomplete) {
        if (autocomplete.next == null)
            return null;

        return baseService.get(autocomplete.next, Autocomplete.class);
    }

    public User get(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, User.class);
    }

    public Albums getAlbums(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "albums", Albums.class);
    }

    public Artists getArtists(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "artists", Artists.class);
    }

    public Charts getCharts(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "charts", Charts.class);
    }

    public Followings getFollowings(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "followings", Followings.class);
    }

    public Followers getFollowers(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "followers", Followers.class);
    }

    public Playlists getPlaylists(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "playlists", Playlists.class);
    }

    public Radio getRadios(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "radios", Radio.class);
    }

    public Tracks getTracks(final UserId userId) {
        return baseService.get(PREFIX_USER, userId.value, "tracks", Tracks.class);
    }

    private String getSearchQuery(final Search search) {
        StringBuilder queryBuilder = new StringBuilder("search");
        queryBuilder.append(search.searchPath);
        queryBuilder.append("?q=");
        try {
            queryBuilder.append(URLEncoder.encode(search.text, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (search.searchOrder != null) {
            queryBuilder.append("&order=");
            queryBuilder.append(search.searchOrder);
        }
        return queryBuilder.toString();
    }

    public boolean isRetryOnQuotaLimitReached() {
        return baseService.isRetryOnQuotaLimitReached();
    }

    public void setRetryOnQuotaLimitReached(boolean retryOnQuotaLimitReached) {
        baseService.setRetryOnQuotaLimitReached(retryOnQuotaLimitReached);
    }

}
