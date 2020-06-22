package com.richo.reader.youtube_feed_service

import arrow.core.Either
import arrow.core.left
import com.codahale.metrics.MetricRegistry
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.richodemus.reader.common.google_cloud_storage_adapter.InMemoryEventStore
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.PlaylistId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

class ServiceTest {
    @Test
    fun `Empty should not contain anything`() {
        val youtubeClient = mock<YoutubeClient> {
            on { getChannel(any()) } doReturn Either.right(emptyList())
        }

        val saveRoot = "target/data/" + UUID.randomUUID()
        val target = YoutubeFeedService(
                Cache(JsonFileSystemPersistence(saveRoot, "channels", Channel::class.java)),
                Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java)),
                YoutubeRepository(youtubeClient),
                MetricRegistry(),
                InMemoryEventStore()
        )

        val result = target.getChannel(FeedId("asd"))

        assertThat(result).isNull()
    }

    @Test
    fun `Adding Feed should download it`() {
        val youtubeClient = mock<YoutubeClient> {
            on { getChannel(eq(FeedId("channel-id"))) } doReturn Either.right(listOf(Channel(FeedId("channel-id"), FeedName("Channel"), PlaylistId("playlist-id"), OffsetDateTime.MIN, OffsetDateTime.MIN)))
            on { getVideos(eq(PlaylistId("playlist-id"))) } doReturn sequenceOf(
                    Video(ItemId("item-1"), "title1", "desc1", OffsetDateTime.MIN, OffsetDateTime.MIN, Duration.ZERO, 0),
                    Video(ItemId("item-2"), "title2", "desc2", OffsetDateTime.MIN, OffsetDateTime.MIN, Duration.ZERO, 0)
            )
        }

        val saveRoot = "target/data/" + UUID.randomUUID()
        val eventStore = InMemoryEventStore()
        val target = YoutubeFeedService(
                Cache(JsonFileSystemPersistence(saveRoot, "channels", Channel::class.java)),
                Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java)),
                YoutubeRepository(youtubeClient),
                MetricRegistry(),
                eventStore
        )

        eventStore.produce(UserSubscribedToFeed(UserId("asd"), FeedId("channel-id")))
        target.updateChannelsAndVideos()
        val result = target.getVideos(FeedId("channel-id"))

        assertThat(result).isNotNull
        assertThat(result).isEqualTo(Videos(listOf(
                Video(ItemId("item-1"), "title1", "desc1", OffsetDateTime.MIN, OffsetDateTime.MIN, Duration.ZERO, 0),
                Video(ItemId("item-2"), "title2", "desc2", OffsetDateTime.MIN, OffsetDateTime.MIN, Duration.ZERO, 0)
        )))
    }
}
