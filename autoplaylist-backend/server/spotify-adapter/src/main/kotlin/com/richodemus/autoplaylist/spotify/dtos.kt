package com.richodemus.autoplaylist.spotify

import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.AlbumName
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.TrackUri


internal data class User(val id: SpotifyUserId)

internal data class PlayListsResponse(val items: List<PlayList>, val total: Int)


internal data class FindArtistResponse(val artists: Artists)
internal data class Artists(val items: List<Artist>, val total: Int)
internal data class Artist(val id: ArtistId, val name: ArtistName)


internal data class GetAlbumsResponse(val items: List<Album>, val total: Int)
data class Album(
        val id: AlbumId,
        val name: AlbumName,
        val album_group: String
)

internal data class GetTracksResponse(val items: List<Track>, val total: Int)


internal data class AddTracksToPlaylistRequest(val uris: List<TrackUri>)
internal data class AddTracksToPlaylistRespose(val snapshot_id: SnapshotId)

