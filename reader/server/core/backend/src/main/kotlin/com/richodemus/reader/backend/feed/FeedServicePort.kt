package com.richodemus.reader.backend.feed

import com.richodemus.reader.backend.model.Feed
import com.richodemus.reader.backend.model.Item
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.youtube_feed_service.Video
import com.richodemus.reader.youtube_feed_service.YoutubeFeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class FeedServicePort @Autowired constructor(private val feedService: YoutubeFeedService) : FeedRepository {
    override fun getFeed(feedId: FeedId): Feed? {
        val channel = feedService.getChannel(feedId)
        val videos = feedService.getVideos(feedId)
        return if (videos.isEmpty() || channel == null) null else (Feed(feedId, channel.name, convertVideos(videos)))
    }

    override fun getFeedId(feedUrl: FeedUrl): FeedId? {
        return feedService.getFeedId(feedUrl)
    }

    private fun convertVideos(videos: List<Video>): List<Item> {
        return videos.stream()
                .map { video: Video -> convertVideo(video) }
                .collect(Collectors.toList())
    }

    private fun convertVideo(video: Video): Item {
        return Item(video.id, video.title, video.description, video.uploadDate.toString(), "https://youtube.com/watch?v=" + video.id, video.duration, video.views)
    }
}
