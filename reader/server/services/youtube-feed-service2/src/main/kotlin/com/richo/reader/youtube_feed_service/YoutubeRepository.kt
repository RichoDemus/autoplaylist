package com.richo.reader.youtube_feed_service

import arrow.core.Either
import arrow.core.extensions.either.monad.map
import arrow.core.filterOrElse
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.PlaylistItem
import com.richodemus.reader.dto.FeedId
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named

fun main() {
    val apiKey = System.getProperty("apiKey") ?: throw IllegalArgumentException("missing prop apiKey")
    val builder = YouTube.Builder(
            NetHttpTransport(),
            JacksonFactory(),
            HttpRequestInitializer { request: HttpRequest? -> })
            .setApplicationName("Richo-Reader")
    val youtube = builder.build()


//    val channel = repository.getChannel(FeedId("UC1E-JS8L0j1Ei70D9VEFrPQ"))

//    println("channel: $channel")

    val execute = youtube
            .channels()
            .list("contentDetails,snippet")
            .setKey(apiKey)
            .setId("UC1E-JS8L0j1Ei70D9VEFrPQ")
            .execute()
            .items
            ?: emptyList()


    println("channels")
    execute.forEach {
        println("\t$it")
        val uploads = it.contentDetails.relatedPlaylists.uploads
        println("\tuploads: $uploads")
        println("\ttitle: ${it.snippet.title}")
        println("\tcreated: ${it.snippet.publishedAt
        }")

        val exec = youtube.playlistItems()
                .list("snippet")
                .setKey(apiKey)
//                .setPageToken("")
                .setPlaylistId(uploads)
                .setMaxResults(50L)
                .execute()
        val ajtems = exec
                .items

        println("\titems:")
        println("\t\tpage token: ${exec.nextPageToken}")
        ajtems.forEach {
            println("\t\titem: ${it.snippet.resourceId.videoId} - ${it}")
        }

        val statItems = youtube.videos()
                .list("statistics,contentDetails")
                .setKey(apiKey)
                .setId("cPKatnm8A4A,qPTie7TGx-c")
                .execute()
                .items

        println("\tstats:")
        statItems.forEach {
            println("\t\tstat: ${it.contentDetails.duration} - ${it.statistics.viewCount}")
        }

    }
}

//fun toItem(playlistItem: PlaylistItem): Item {
//    val videoId = playlistItem.snippet.resourceId.videoId
//    val title = playlistItem.snippet.title
//    val description = playlistItem.snippet.description
//    val uploadDate: LocalDateTime = convertDate(playlistItem.snippet.publishedAt)
//    return Item(videoId, title, description, uploadDate.toEpochSecond(ZoneOffset.UTC), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), Duration.ZERO.toMillis(), 0L)
//}
//
//fun convertDate(publishedAt: DateTime): LocalDateTime {
//    val epoch = publishedAt.value / 1000
//    val timeZoneShift = publishedAt.timeZoneShift
//    val nanoSecond = 0
//    return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60))
//}

internal class YoutubeRepository @Inject constructor(
        private val youtubeClient: YoutubeClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getChannel(id: FeedId): Either<String, Channel> {
        return youtubeClient.getChannel(id)
                .filterOrElse({it.isNotEmpty()}, {"No such channel: $id"})
                .peek {
                    if ((it.size > 1)) {
                        logger.warn("More than 1 channels for id {}", id)
                    } 
                }
                .map { it.first() }
    }



//    internal fun getFeedsAndItems(feeds: List<Feed>): List<Feed> {
//
//        TODO("Not yet implemented")
//    }
}
