package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackId
import com.richodemus.autoplaylist.dto.TrackUri
import java.util.concurrent.CompletableFuture

interface SpotifyPort {
    fun getToken(code: String): CompletableFuture<Tokens>
    fun getUserId(accessToken: AccessToken): CompletableFuture<SpotifyUserId>
    fun getPlaylists(accessToken: AccessToken): CompletableFuture<List<PlayList>>
    fun refreshToken(refreshToken: RefreshToken): CompletableFuture<Tokens>
    fun findArtist(accessToken: AccessToken, name: ArtistName): CompletableFuture<List<ArtistId>>
    fun getAlbums(accessToken: AccessToken, artistId: ArtistId): CompletableFuture<List<Album>>
    fun getTracks(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            playlistId: PlayListId
    ): CompletableFuture<List<TrackId>>

    fun createPlaylist(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            name: PlaylistName
    ): CompletableFuture<PlayList>

    fun addTracksToPlaylist(
            accessToken: AccessToken,
            spotifyUserId: SpotifyUserId,
            id: PlayListId,
            tracks: List<TrackUri>
    ): CompletableFuture<Unit>
}
