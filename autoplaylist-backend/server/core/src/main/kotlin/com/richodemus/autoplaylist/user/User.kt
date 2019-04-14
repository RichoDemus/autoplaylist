package com.richodemus.autoplaylist.user

import com.richodemus.autoplaylist.dto.PlaylistId
import com.richodemus.autoplaylist.dto.PlaylistName
import com.richodemus.autoplaylist.dto.UserId
import com.richodemus.autoplaylist.dto.events.Event
import com.richodemus.autoplaylist.dto.events.PlaylistCreated
import com.richodemus.autoplaylist.event.EventStore
import com.richodemus.autoplaylist.playlist.Playlist
import com.richodemus.autoplaylist.spotify.SpotifyService

class User internal constructor(
        private val eventStore: EventStore,
        private val spotifyPort: SpotifyService,
        val userId: UserId
) {
    private var playlists = emptyList<Playlist>()

    fun process(event: Event) {
        when (event) {
            is PlaylistCreated -> createPlaylist(event)
        }
    }

    fun createPlaylist(name: PlaylistName): Playlist {
        val event = PlaylistCreated(userId = userId, playlistName = name)

        eventStore.produce(event)
        return createPlaylist(event)
    }

    // todo remove once this is CQRS:ed
    @Synchronized
    private fun createPlaylist(event: PlaylistCreated): Playlist {
        if (playlists.any { it.id == event.playlistId }) {
            return playlists.first { it.id == event.playlistId }
        }
        // todo don't use accessToken!!!
        val playlist = Playlist(event.playlistId, userId, event.playlistName, spotifyPort, eventStore)
        playlists = playlists.plus(playlist)
        return playlist
    }

    override fun toString(): String {
        return "User(userId=$userId)"
    }

    fun getPlaylist(playlistId: PlaylistId): Playlist {
        return playlists.first { it.id == playlistId }
    }

    fun getPlaylists() = playlists
}
