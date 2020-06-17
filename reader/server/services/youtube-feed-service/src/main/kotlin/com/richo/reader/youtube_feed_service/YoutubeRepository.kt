package com.richo.reader.youtube_feed_service

import arrow.core.Either
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.PlaylistItem
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Named

fun main() {
    println("hello")
    val repository = YoutubeRepository("")

    val execute = repository.yt().channels()
            .list("contentDetails,snippet")
            .setKey(repository.apiKey)
            .setId("UC1E-JS8L0j1Ei70D9VEFrPQ")
            .execute()
            .items


    println("channels")
    execute.forEach {
        println("\t$it")
        val uploads = it.contentDetails.relatedPlaylists.uploads
        println("\tuploads: $uploads")
        println("\ttitle: ${it.snippet.title}")

        val exec = repository.yt().playlistItems()
                .list("snippet")
                .setKey(repository.apiKey)
//                .setPageToken("")
                .setPlaylistId(uploads)
                .setMaxResults(50L)
                .execute()
        val ajtems = exec
                .items

        println("\titems:")
        println("\t\tpage token: ${exec.nextPageToken}")
        ajtems.forEach {
            println("\t\titem: ${it.snippet.resourceId.videoId} - ${toItem(it)}")
        }

        val statItems = repository.yt().videos()
                .list("statistics,contentDetails")
                .setKey(repository.apiKey)
                .setId("cPKatnm8A4A,qPTie7TGx-c")
                .execute()
                .items

        println("\tstats:")
        statItems.forEach {
            println("\t\tstat: ${it.contentDetails.duration} - ${it.statistics.viewCount}")
        }

    }
}

fun toItem(playlistItem: PlaylistItem): Item {
    val videoId = playlistItem.snippet.resourceId.videoId
    val title = playlistItem.snippet.title
    val description = playlistItem.snippet.description
    val uploadDate: LocalDateTime = convertDate(playlistItem.snippet.publishedAt)
    return Item(videoId, title, description, uploadDate.toEpochSecond(ZoneOffset.UTC), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), Duration.ZERO.toMillis(), 0L)
}

fun convertDate(publishedAt: DateTime): LocalDateTime {
    val epoch = publishedAt.value / 1000
    val timeZoneShift = publishedAt.timeZoneShift
    val nanoSecond = 0
    return LocalDateTime.ofEpochSecond(epoch, nanoSecond, ZoneOffset.ofHours(timeZoneShift / 60))
}

internal class YoutubeRepository @Inject constructor(@Named("apiKey") val apiKey: String) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val youtube: YouTube

    init {
        val urlOverride: Optional<String> = getUrlOverride()
        logger.info("youtube url: \"{}\"", urlOverride.orElse("Not set"))
        val builder = YouTube.Builder(
                NetHttpTransport(),
                JacksonFactory(),
                HttpRequestInitializer { request: HttpRequest? -> })
                .setApplicationName("Richo-Reader")
        urlOverride.ifPresent { rootUrl: String? -> builder.rootUrl = rootUrl }
        youtube = builder.build()
    }

    fun yt() = youtube

    private fun getUrlOverride(): Optional<String> {
        val env = System.getenv("YOUTUBE_URL")
        return if (env != null) {
            Optional.of(env)
        } else Optional.ofNullable(System.getProperty("YOUTUBE_URL"))
    }

    internal fun getFeedsAndItems(feeds: List<Feed>): List<Feed> {


        val asd: Either<String, String> = Either.right("asd")
        TODO("Not yet implemented")
    }
}
