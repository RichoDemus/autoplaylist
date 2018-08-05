package com.richodemus.autoplaylist.test.dto

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.Artist
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName

data class Artist(
        val id: ArtistId,
        val name: ArtistName,
        val albums: List<Album>
) {
    fun toArtist() = Artist(id, name)
}
