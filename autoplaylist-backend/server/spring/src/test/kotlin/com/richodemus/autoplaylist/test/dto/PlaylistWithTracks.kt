package com.richodemus.autoplaylist.test.dto

import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.spotify.Playlist
import com.richodemus.autoplaylist.spotify.PlaylistId
import com.richodemus.autoplaylist.spotify.PlaylistName
import java.util.UUID

data class PlaylistWithTracks(
        val name: PlaylistName,
        val id: PlaylistId = PlaylistId(UUID.randomUUID().toString()),
        val tracks: List<Track> = emptyList()
) {
    fun toPlaylist() = Playlist(id, name)

}
