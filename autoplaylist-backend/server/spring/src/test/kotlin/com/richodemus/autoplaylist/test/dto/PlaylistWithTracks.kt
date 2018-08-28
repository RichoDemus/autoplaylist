package com.richodemus.autoplaylist.test.dto

import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.SpotifyPlaylistId
import com.richodemus.autoplaylist.dto.Track

data class PlaylistWithTracks(
        val id: SpotifyPlaylistId,
        val name: PlaylistName,
        val tracks: List<Track> = emptyList()
)
