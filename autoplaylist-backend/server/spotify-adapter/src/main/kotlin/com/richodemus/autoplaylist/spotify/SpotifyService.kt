package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackUri
import com.richodemus.autoplaylist.dto.UserId

interface SpotifyService {
    suspend fun getUserId(code: String): UserId

    suspend fun getUserId(userId: UserId): SpotifyUserId

    suspend fun getPlaylists(userId: UserId): List<Playlist>

    suspend fun findArtist(userId: UserId, name: ArtistName): List<Artist>

    suspend fun getArtist(userId: UserId, artistId: ArtistId): Artist?

    suspend fun getAlbums(userId: UserId, artistId: ArtistId): List<Album>

    suspend fun getTracks(
            userId: UserId,
            playlistId: SpotifyPlaylistId
    ): List<Track>

    suspend fun createPlaylist(
            userId: UserId,
            name: PlaylistName
    ): Playlist

    suspend fun addTracksToPlaylist(
            userId: UserId,
            playlistId: SpotifyPlaylistId,
            tracks: List<TrackUri>
    )
}
