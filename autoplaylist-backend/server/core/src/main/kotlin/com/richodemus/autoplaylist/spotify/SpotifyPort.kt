package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri

interface SpotifyPort {
    suspend fun getToken(code: String): Tokens
    suspend fun getUserId(accessToken: AccessToken): SpotifyUserId
    suspend fun getPlaylists(accessToken: AccessToken): List<Playlist>
    suspend fun refreshToken(refreshToken: RefreshToken): Tokens
    suspend fun findArtist(accessToken: AccessToken, name: ArtistName): List<Artist>
    suspend fun getAlbums(accessToken: AccessToken, artistId: ArtistId): List<Album>
    suspend fun getTracks(
            accessToken: AccessToken,
            playlistId: PlaylistId
    ): List<Track>

    suspend fun createPlaylist(
            accessToken: AccessToken,
            name: PlaylistName
    ): Playlist

    suspend fun addTracksToPlaylist(
            accessToken: AccessToken,
            playlistId: PlaylistId,
            tracks: List<TrackUri>
    )
}
