package com.richo.reader.backend.user;

import com.richo.reader.backend.model.Feed;
import com.richo.reader.backend.model.Item;
import com.richo.reader.backend.youtube.model.YoutubeChannel;
import com.richo.reader.backend.youtube.model.YoutubeVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class User
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String name;
	private final Set<Feed> feeds;

	public User(String username, Set<Feed> feeds)
	{
		this.name = username;
		this.feeds = feeds;
	}

	public Set<Feed> getSubscribedFeeds()
	{
		return feeds;
	}

	public void updateChannel(YoutubeChannel channel)
	{
		final Feed target = feeds.stream().filter(feed -> feed.getName().equals(channel.getName())).findAny().orElseThrow(() -> new RuntimeException(name + " does not have the feed " + channel.getName()));

		target.addNewItems(channel.getVideos().stream().map(this::videoToItem).collect(Collectors.toSet()));
	}

	public void addChannel(YoutubeChannel channel)
	{
		final String id = channel.getName();
		final Feed feed = new Feed(id, id);
		feed.addNewItems(channel.getVideos().stream().map(this::videoToItem).collect(Collectors.toSet()));
		feeds.add(feed);
	}

	private Item videoToItem(YoutubeVideo video)
	{
		return new Item(video.getVideoId(), video.getTitle(), video.getDescription(), video.getUrl(), video.getUploadDate());
	}

	public String getName()
	{
		return name;
	}
}
