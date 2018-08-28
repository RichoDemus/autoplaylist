package com.richodemus.autoplaylist.spotify

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.AlbumName
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackUri


internal data class User(val id: SpotifyUserId)

internal data class PlaylistsResponse(val items: List<Playlist>, val total: Int)


internal data class FindArtistResponse(val artists: Artists)
internal data class GetArtistREsponse(val artists: Artists)
internal data class Artists(val items: List<Artist>, val total: Int)
internal data class Artist(val id: ArtistId, val name: ArtistName)


internal data class GetAlbumsResponse(val items: List<Album>, val total: Int)
internal data class Album(
        val id: AlbumId,
        val name: AlbumName,
        val album_group: String
)

internal data class GetTracksFromAlbumResponse(val items: List<Track>, val total: Int)
internal data class GetTracksFromPlaylistResponse(val items: List<Item>, val total: Int)
internal data class Item(val track: Track)


internal data class AddTracksToPlaylistRequest(val uris: List<TrackUri>)
internal data class AddTracksToPlaylistRespose(val snapshot_id: SnapshotId)

// todo remove or figure out if we use this
internal data class SnapshotId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "${javaClass.simpleName} can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}
