package com.richodemus.autoplaylist.dto

data class Album(
        val id: AlbumId,
        val name: AlbumName,
        val tracks: List<Track>
)
