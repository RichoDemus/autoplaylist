package com.richodemus.autoplaylist.spotify

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.richodemus.autoplaylist.dto.SpotifyUserId


internal data class User(val id: SpotifyUserId)

internal data class PlayListsResponse(val items: List<PlayList>, val total: Int)


internal data class FindArtistResponse(val artists: Artists)
internal data class Artists(val items: List<Artist>, val total: Int)
internal data class Artist(val id: ArtistId, val name: ArtistName)

internal data class ArtistId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "ArtistId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class GetAlbumsResponse(val items: List<Album>, val total: Int)
internal data class Album(val id: AlbumId, val name: String, val album_group: String)
internal data class AlbumId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "AlbumId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

internal data class GetTracksResponse(val items: List<Track>, val total: Int)


internal data class AddTracksToPlaylistRequest(val uris: List<TrackUri>)
internal data class AddTracksToPlaylistRespose(val snapshot_id: SnapshotId)
internal data class SnapshotId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "SnapshotId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}
