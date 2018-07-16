package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.RefreshToken

private val rToken = RefreshToken("enter-a-refresh-token")
/*
fun main_rename_to_just_main(args: Array<String>) {
    val validToken = getAccessToken(rToken).map { it.accessToken }

    validToken.map { println("Token: $it") }

    val userId = SpotifyUserId("your-username-here")
    val artist = ArtistName("Gloryhammer")

    val tracksFutures = getTracks(validToken.join(), artist)


    val tracks = tracksFutures.join()
    println()
    println("Tracks:")
    tracks.forEach { println("\t" + it) }

    val trackUris = tracks.map { it.uri }


    val accessToken = validToken.join()


    val createPlaylistAndAddTracksFuture = createPlaylist(accessToken, userId, PlaylistName(artist.value), trackUris)


    val snapshotIds = createPlaylistAndAddTracksFuture.join()

    println("Done, snapshot id is: $snapshotIds")
}
*/
