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

internal fun getTrack(uri: TrackUri) = listOf(ARTIST, ARTIST_WITH_DUPLICATE_TRACKS)
        .flatMap { it.albums }
        .flatMap { it.tracks }
        .find { it.uri == uri }
        ?: throw IllegalStateException("No track with uri $uri")


val ARTIST = Artist(
        ArtistId("powerwolf"),
        ArtistName("Powerwolf"),
        listOf(
                Album(AlbumId("blessed"), AlbumName("Blessed & Possessed"), listOf(
                        Track(TrackId("blessed1"), TrackName("Armata Strigoi"), TrackUri("b1uri")),
                        Track(TrackId("blessed2"), TrackName("Army of the Night"), TrackUri("b2uri"))
                )),
                Album(AlbumId("preachers"), AlbumName("Preachers of the Night"), listOf(
                        Track(TrackId("preachers1"), TrackName("Amen and Attack"), TrackUri("p1uri")),
                        Track(TrackId("preachers2"), TrackName("Kreuzfeuer"), TrackUri("p2uri"))
                ))
        )
)

val ARTIST_WITH_DUPLICATE_TRACKS = Artist(
        ArtistId("civil_war"),
        ArtistName("Civil War"),
        listOf(
                Album(AlbumId("measure1"), AlbumName("The Last Full Measure"), listOf(
                        Track(TrackId("rdv1"), TrackName("Road to Victory"), TrackUri("rdv1uri")),
                        Track(TrackId("d1"), TrackName("Deliverance"), TrackUri("d1uri"))
                )),
                Album(AlbumId("measure2"), AlbumName("The Last Full Measure"), listOf(
                        Track(TrackId("rdv2"), TrackName("Road to Victory"), TrackUri("rdv2uri")),
                        Track(TrackId("d2"), TrackName("Deliverance"), TrackUri("d2uri"))
                ))
        )
)
