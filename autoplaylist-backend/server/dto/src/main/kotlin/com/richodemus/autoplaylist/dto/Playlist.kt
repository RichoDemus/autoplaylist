package com.richodemus.autoplaylist.dto

data class Playlist(
        val id: PlaylistId,
        val spotifyPlaylistId: SpotifyPlaylistId?,
        val name: PlaylistName,
        val rules: Rules,
        val albums: List<Album>,
        val sync: Boolean,
        val lastSynced: String
)
