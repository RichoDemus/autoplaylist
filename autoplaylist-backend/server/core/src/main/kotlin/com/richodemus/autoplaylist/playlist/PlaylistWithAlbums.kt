package com.richodemus.autoplaylist.playlist

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.PlaylistId

data class PlaylistWithAlbums(val id: PlaylistId, val albums: List<Album>)
