package com.richodemus.reader.backend.feed;

import com.richodemus.reader.backend.model.Feed;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.youtube_feed_service.Video;
import com.richodemus.reader.youtube_feed_service.YoutubeFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class FeedServicePort implements FeedRepository {
    private final YoutubeFeedService feedService;

    @Autowired
    public FeedServicePort(YoutubeFeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public Optional<Feed> getFeed(FeedId feedId) {
        var channel = feedService.getChannel(feedId);
        List<Video> videos = feedService.getVideos(feedId);
        if (videos.isEmpty() || channel == null)
            return Optional.empty();
        return Optional.of(new Feed(feedId, channel.getName(), convertVideos(videos)));
    }

    @Override
    public FeedId getFeedId(FeedUrl feedUrl) {
        return feedService.getFeedId(feedUrl);
    }

    private List<com.richodemus.reader.backend.model.Item> convertVideos(List<Video> videos) {
        return videos.stream()
                .map(this::convertVideo)
                .collect(toList());
    }

    private com.richodemus.reader.backend.model.Item convertVideo(Video video) {
        return new com.richodemus.reader.backend.model.Item(video.getId(), video.getTitle(), video.getDescription(), video.getUploadDate().toString(), "https://youtube.com/watch?v=" + video.getId(), video.getDuration(), video.getViews());
    }
}
