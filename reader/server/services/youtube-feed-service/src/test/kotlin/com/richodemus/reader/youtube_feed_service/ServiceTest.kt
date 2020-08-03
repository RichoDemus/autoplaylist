package com.richodemus.reader.youtube_feed_service

import arrow.core.Either
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
import java.time.Clock
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

class ServiceTest {
    @Test
    fun `Empty should not contain anything`() {
        val youtubeClient = mock<YoutubeClient> {
            on { getId(any()) } doReturn Either.right(emptyList())
        }

        val saveRoot = "target/data/" + UUID.randomUUID()
        val target = YoutubeFeedService(
                Cache(JsonFileSystemPersistence(saveRoot, "channels", Channel::class.java)),
                Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java)),
                YoutubeRepository(youtubeClient),
                InMemoryEventStore(),
                Clock.fixed(date("2020-01-04").toInstant(), ZoneId.of("UTC")),
                PassthroughPrioritizer()
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
            on { getStatistics(any()) } doReturn Either.left("no stats!")
        }

        val saveRoot = "target/data/" + UUID.randomUUID()
        val eventStore = InMemoryEventStore()
        val target = YoutubeFeedService(
                Cache(JsonFileSystemPersistence(saveRoot, "channels", Channel::class.java)),
                Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java)),
                YoutubeRepository(youtubeClient),
                eventStore,
                Clock.fixed(date("2020-01-04").toInstant(), ZoneId.of("UTC")),
                PassthroughPrioritizer()
        )

        eventStore.produce(UserSubscribedToFeed(UserId("asd"), FeedId("channel-id")))
        target.updateChannelsAndVideos()
        val result = target.getVideos(FeedId("channel-id"))

        assertThat(result).isNotNull
        assertThat(result).containsOnly(
                Video(ItemId("item-1"), "title1", "desc1", OffsetDateTime.MIN, OffsetDateTime.MIN, Duration.ZERO, 0),
                Video(ItemId("item-2"), "title2", "desc2", OffsetDateTime.MIN, OffsetDateTime.MIN, Duration.ZERO, 0)
        )
    }

    @Test
    fun `Download video statistics`() {
        val youtubeClient = mock<YoutubeClient> {
            on { getChannel(eq(FeedId("channel-id"))) } doReturn
                    Either.right(listOf(
                            Channel(FeedId("channel-id"), FeedName("Channel"), PlaylistId("playlist-id"), OffsetDateTime.MIN, OffsetDateTime.MIN)
                    ))
            on { getVideos(eq(PlaylistId("playlist-id"))) } doReturn sequenceOf(
                    Video(ItemId("item-1"), "title1", "desc1", date("2020-01-01"), OffsetDateTime.MIN, Duration.ZERO, 0),
                    Video(ItemId("item-2"), "title2", "desc2", date("2020-01-02"), OffsetDateTime.MIN, Duration.ZERO, 0)
            )

            on { getChannel(eq(FeedId("channel-id2"))) } doReturn
                    Either.right(listOf(
                            Channel(FeedId("channel-id2"), FeedName("Channel2"), PlaylistId("playlist-id2"), OffsetDateTime.MIN, OffsetDateTime.MIN)
                    ))
            on { getVideos(eq(PlaylistId("playlist-id2"))) } doReturn sequenceOf(
                    Video(ItemId("item-3"), "title3", "desc3", date("2020-01-03"), OffsetDateTime.MIN, Duration.ZERO, 0),
                    Video(ItemId("item-4"), "title4", "desc4", date("2020-01-04"), OffsetDateTime.MIN, Duration.ZERO, 0)
            )

            on { getStatistics(eq(listOf("item-1", "item-2", "item-3", "item-4").map { ItemId(it) })) } doReturn
                    Either.right(mapOf(
                            Pair(ItemId("item-1"), Pair(Duration.ofSeconds(1), 1L)),
                            Pair(ItemId("item-2"), Pair(Duration.ofMinutes(1), 2L)),
                            Pair(ItemId("item-3"), Pair(Duration.ofHours(1), 3L)),
                            Pair(ItemId("item-4"), Pair(Duration.ofDays(1), 4L))
                    ))
        }

        val saveRoot = "target/data/" + UUID.randomUUID()
        val eventStore = InMemoryEventStore()
        val target = YoutubeFeedService(
                Cache(JsonFileSystemPersistence(saveRoot, "channels", Channel::class.java)),
                Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java)),
                YoutubeRepository(youtubeClient),
                eventStore,
                Clock.fixed(date("2020-01-04").toInstant(), ZoneId.of("UTC")),
                PassthroughPrioritizer()
        )

        eventStore.produce(UserSubscribedToFeed(UserId("asd"), FeedId("channel-id")))
        eventStore.produce(UserSubscribedToFeed(UserId("asd"), FeedId("channel-id2")))
        target.updateChannelsAndVideos()
        var result = target.getVideos(FeedId("channel-id"))

        assertThat(result).isNotNull
        assertThat(result).containsOnly(
                Video(ItemId("item-1"), "title1", "desc1", date("2020-01-01"), date("2020-01-04"), Duration.ofSeconds(1), 1),
                Video(ItemId("item-2"), "title2", "desc2", date("2020-01-02"), date("2020-01-04"), Duration.ofMinutes(1), 2)
        )

        result = target.getVideos(FeedId("channel-id2"))

        assertThat(result).isNotNull
        assertThat(result).containsOnly(
                Video(ItemId("item-3"), "title3", "desc3", date("2020-01-03"), date("2020-01-04"), Duration.ofHours(1), 3),
                Video(ItemId("item-4"), "title4", "desc4", date("2020-01-04"), date("2020-01-04"), Duration.ofDays(1), 4)
        )
    }

    @Test
    fun `Should stop downloading once  it gets an old item`() {
        val youtubeClient = mock<YoutubeClient> {
            on { getChannel(eq(FeedId("channel-id"))) } doReturn Either.right(listOf(Channel(FeedId("channel-id"), FeedName("Channel"), PlaylistId("playlist-id"), OffsetDateTime.MIN, OffsetDateTime.MIN)))
            on { getVideos(eq(PlaylistId("playlist-id"))) } doReturn sequenceOf(
                    Video(ItemId("item-2"), "title2", "desc2", date("2020-01-02"), OffsetDateTime.MIN, Duration.ZERO, 0),
                    Video(ItemId("item-1"), "title1", "desc1", date("2020-01-01"), OffsetDateTime.MIN, Duration.ZERO, 0)
            ) doReturn sequenceOf(
                    Video(ItemId("item-3"), "title3", "desc3", date("2020-01-03"), OffsetDateTime.MIN, Duration.ZERO, 0),
                    Video(ItemId("item-2"), "title2", "desc2", date("2020-01-02"), OffsetDateTime.MIN, Duration.ZERO, 0),
                    Video(ItemId("item-1"), "title1", "desc1", date("2020-01-01"), OffsetDateTime.MIN, Duration.ZERO, 0)
            )
            on { getStatistics(eq(listOf("item-1", "item-2").map { ItemId(it) })) } doReturn
                    Either.right(mapOf(
                            Pair(ItemId("item-1"), Pair(Duration.ofSeconds(1), 1L)),
                            Pair(ItemId("item-2"), Pair(Duration.ofMinutes(1), 2L))
                    ))
            on { getStatistics(eq(listOf("item-1", "item-2", "item-3").map { ItemId(it) })) } doReturn
                    Either.right(mapOf(
                            Pair(ItemId("item-1"), Pair(Duration.ofSeconds(1), 1L)),
                            Pair(ItemId("item-2"), Pair(Duration.ofMinutes(1), 2L)),
                            Pair(ItemId("item-3"), Pair(Duration.ofHours(1), 3L))
                    ))
        }

        val saveRoot = "target/data/" + UUID.randomUUID()
        val eventStore = InMemoryEventStore()
        val target = YoutubeFeedService(
                Cache(JsonFileSystemPersistence(saveRoot, "channels", Channel::class.java)),
                Cache(JsonFileSystemPersistence(saveRoot, "videos", Videos::class.java)),
                YoutubeRepository(youtubeClient),
                eventStore,
                Clock.fixed(date("2020-01-04").toInstant(), ZoneId.of("UTC")),
                PassthroughPrioritizer()
        )

        eventStore.produce(UserSubscribedToFeed(UserId("asd"), FeedId("channel-id")))
        target.updateChannelsAndVideos()
        var result = target.getVideos(FeedId("channel-id"))

        assertThat(result).isNotNull
        assertThat(result).containsOnly(
                Video(ItemId("item-2"), "title2", "desc2", date("2020-01-02"), date("2020-01-04"), Duration.ofMinutes(1), 2),
                Video(ItemId("item-1"), "title1", "desc1", date("2020-01-01"), date("2020-01-04"), Duration.ofSeconds(1), 1)
        )

        target.updateChannelsAndVideos()
        result = target.getVideos(FeedId("channel-id"))
        assertThat(result).isNotNull
        assertThat(result).containsOnly(
                Video(ItemId("item-3"), "title3", "desc3", date("2020-01-03"), date("2020-01-04"), Duration.ofHours(1), 3),
                Video(ItemId("item-2"), "title2", "desc2", date("2020-01-02"), date("2020-01-04"), Duration.ofMinutes(1), 2),
                Video(ItemId("item-1"), "title1", "desc1", date("2020-01-01"), date("2020-01-04"), Duration.ofSeconds(1), 1)
        )
    }
}
