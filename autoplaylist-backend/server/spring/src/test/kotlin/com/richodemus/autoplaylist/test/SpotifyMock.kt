package com.richodemus.autoplaylist.test

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.PlayList
import com.richodemus.autoplaylist.spotify.PlayListId
import com.richodemus.autoplaylist.spotify.PlaylistName
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import io.github.vjames19.futures.jdk8.Future
import java.util.UUID

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
}
