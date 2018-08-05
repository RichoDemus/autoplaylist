package com.richodemus.autoplaylist.test.spotifymock

import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.AlbumName
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackId
import com.richodemus.autoplaylist.dto.TrackName
import com.richodemus.autoplaylist.dto.TrackUri
import com.richodemus.autoplaylist.test.dto.Artist

val ARTIST = Artist(
        ArtistId("powerwolf"),
        ArtistName("Powerwolf"),
        listOf(
                Album(AlbumId("blessed"), AlbumName("Blessed & Possessed"), listOf(
                        Track(TrackId("blessed1"), TrackName("Armata Strigoi"), TrackUri("uri1")),
                        Track(TrackId("blessed2"), TrackName("Army of the Night"), TrackUri("uri2"))
                )),
                Album(AlbumId("preachers"), AlbumName("Preachers of the Night"), listOf(
                        Track(TrackId("preachers1"), TrackName("Amen and Attack"), TrackUri("uri3")),
                        Track(TrackId("preachers2"), TrackName("Kreuzfeuer"), TrackUri("uri4"))
                ))
        )
)
