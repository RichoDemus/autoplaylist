package com.richodemus.autoplaylist.test

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import com.richodemus.autoplaylist.dto.Album
import com.richodemus.autoplaylist.dto.AlbumId
import com.richodemus.autoplaylist.dto.AlbumName
import com.richodemus.autoplaylist.dto.ArtistId
import com.richodemus.autoplaylist.dto.ArtistName
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.dto.Track
import com.richodemus.autoplaylist.dto.TrackId
import com.richodemus.autoplaylist.dto.TrackName
import com.richodemus.autoplaylist.dto.TrackUri
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlayList
import com.richodemus.autoplaylist.spotify.PlayListId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import com.richodemus.autoplaylist.test.dto.Artist
import io.github.vjames19.futures.jdk8.Future
import java.util.UUID

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

private var code = ""
@Suppress("unused")
var SpotifyPort.oAuth2Code: String
    get() {
        return code
    }
    set(value) {
        code = value
    }

private var userId = ""
@Suppress("unused")
var SpotifyPort.spotifyUserId: String
    get() {
        return userId
    }
    set(value) {
        userId = value
    }


fun SpotifyPort.mockDefaultBehavior() {
    oAuth2Code = UUID.randomUUID().toString()
    spotifyUserId = UUID.randomUUID().toString()
    whenever(this.getToken(oAuth2Code)).doReturn(Future {
        Tokens(
                AccessToken("access-token"),
                "scope",
                "type",
                100000,
                RefreshToken("refresh-token")
        )
    })
    whenever(this.getUserId(AccessToken("access-token"))).doReturn(Future {
        SpotifyUserId(spotifyUserId)
    })
    whenever(this.refreshToken(any())).doReturn(Future {
        Tokens(
                AccessToken("access-token"),
                "scope",
                "type",
                100000,
                RefreshToken("refresh-token")
        )
    })
    whenever(this.getPlaylists(any())).doReturn(Future {
        listOf(
                PlayList(PlayListId("id1"), PlaylistName("name1")),
                PlayList(PlayListId("id2"), PlaylistName("name2"))
        )
    })
    whenever(this.createPlaylist(any(), any(), any())).doReturn(Future {
        PlayList(PlayListId("playlistId"), PlaylistName("playlistName"))
    })
    whenever(this.getTracks(any(), any(), eq(PlayListId("playlistId")))).doReturn(Future { emptyList<TrackId>() })
    whenever(this.addTracksToPlaylist(any(), any(), eq(PlayListId("playlistId")), any())).doReturn(Future { })
    whenever(this.findArtist(any(), eq(ARTIST.name))).doReturn(Future { listOf(com.richodemus.autoplaylist.dto.Artist(ARTIST.id, ARTIST.name)) })
    whenever(this.getAlbums(any(), eq(ARTIST.id))).doReturn(Future { ARTIST.albums })
}
