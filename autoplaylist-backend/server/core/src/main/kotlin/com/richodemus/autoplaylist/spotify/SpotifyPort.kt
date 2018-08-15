package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import java.util.concurrent.CompletableFuture

interface SpotifyPort {
    fun getToken(code: String): CompletableFuture<Tokens>
    fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId>
    fun getPlaylists(accessToken: AccessToken): CompletableFuture<List<Playlist>>
    fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens>
    fun findArtist(accessToken: AccessToken, name: ArtistName): CompletableFuture<List<Artist>>
    fun getAlbums(accessToken: AccessToken, artistId: ArtistId): CompletableFuture<List<Album>>
    fun getTracks(
            accessToken: AccessToken,
            playlistId: PlaylistId
    ): CompletableFuture<List<Track>>

    fun createPlaylist(
            accessToken: AccessToken,
            name: PlaylistName
    ): CompletableFuture<Playlist>

    fun addTracksToPlaylist(
            accessToken: AccessToken,
            playlistId: PlaylistId,
            tracks: List<TrackUri>
    ): CompletableFuture<Unit>
}
