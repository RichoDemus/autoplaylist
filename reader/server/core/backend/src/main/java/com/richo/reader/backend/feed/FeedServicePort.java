package com.richo.reader.backend.feed;

import com.richo.reader.backend.model.Feed;
import com.richo.reader.youtube_feed_service.Video;
import com.richo.reader.youtube_feed_service.YoutubeFeedService;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class FeedServicePort implements FeedRepository
{
	private final YoutubeFeedService feedService;

	@Inject
	public FeedServicePort(YoutubeFeedService feedService)
	{
		this.feedService = feedService;
	}

	@Override
	public Optional<Feed> getFeed(FeedId feedId)
	{
		List<Video> videos = feedService.getVideos(feedId);
		if(videos.isEmpty())
			return Optional.empty();
		return Optional.of(new Feed(feedId, new FeedName("used?"), convertVideos(videos)));
	}

	@Override
	public List<ItemId> getItemIds(FeedId feedId)
	{
		throw new IllegalStateException("Not used?");
//		return feedService.getChannel(feedId)
//				.map(com.richo.reader.youtube_feed_service.Feed::getItems)
//				.orElseGet(ArrayList::new)
//				.stream()
//				.map(Item::getId)
//				.collect(toList());
	}

	@Override
	public FeedId getFeedId(FeedUrl feedUrl)
	{
		return feedService.getFeedId(feedUrl);
	}

	private List<com.richo.reader.backend.model.Item> convertVideos(List<Video> videos)
	{
		return videos.stream()
				.map(this::convertVideo)
				.collect(toList());
	}

	private com.richo.reader.backend.model.Item convertVideo(Video video)
	{
		return new com.richo.reader.backend.model.Item(video.getId(), video.getTitle(), video.getDescription(), video.getUploadDate().toString(), "https://youtube.com/watch?v=" + video.getId(), video.getDuration(), video.getViews());
	}
}
