package com.richo.reader.youtube_feed_service.youtube;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class YoutubeChannelDownloader
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final DurationParser durationParser;
	private final YouTube youtube;
	private final String apiKey;

	@Inject
	public YoutubeChannelDownloader(DurationParser durationParser, @Named("apiKey") String apiKey)
	{
		this.durationParser = durationParser;
		this.apiKey = apiKey;
		final Optional<String> url_override = getUrlOverride();
		logger.info("youtube url: \"{}\"", url_override.orElse("Not set"));
		final YouTube.Builder builder = new YouTube.Builder(
				new NetHttpTransport(),
				new JacksonFactory(),
				request ->
				{
				})
				.setApplicationName("Richo-Reader");
		url_override.ifPresent(builder::setRootUrl);
		youtube = builder.build();
	}

	public FeedId getFeedId(final FeedUrl feedUrl)
	{
		final String path = feedUrl.getValue().getPath();
		final String id = path.split("/")[2];
		if (path.startsWith("/channel/"))
		{
			return new FeedId(id);
		}
		return usernameToChannel(id);
	}

	// todo remove this
	public FeedId nameToId(final FeedName feedName)
	{
		return usernameToChannel(feedName.getValue());
	}

	public Optional<FeedName> getName(final FeedId feedId)
	{
		final List<Channel> channels;
		try
		{
			channels = youtube.channels()
					.list("snippet,status,id,statistics")
					.setKey(apiKey)
					.setId(feedId.getValue())
					.execute()
					.getItems();
		}
		catch (Exception e)
		{
			logger.warn("Unable to figure out feed name for feed id {}", feedId, e);
			return Optional.empty();
		}

		if (channels == null || channels.size() == 0)
		{
			logger.warn("No channels found for id {}", feedId);
			return Optional.empty();
		}

		return Optional.of(new FeedName(channels.get(0).getSnippet().getTitle()));
	}

	private FeedId usernameToChannel(String username)
	{
		final List<Channel> channels;
		try
		{
			channels = youtube.channels()
					.list("snippet,status,id,statistics")
					.setKey(apiKey)
					.setForUsername(username)
					.execute()
					.getItems();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed getting channel id for username " + username, e);
		}

		if (channels == null || channels.size() == 0)
		{
			throw new RuntimeException("No channels found for username " + username);
		}

		return new FeedId(channels.get(0).getId());
	}

	private Optional<String> getUrlOverride()
	{
		final String env = System.getenv("YOUTUBE_URL");
		if (env != null)
		{
			return Optional.of(env);
		}

		return Optional.ofNullable(System.getProperty("YOUTUBE_URL"));
	}

	public static void main(String[] args) throws IOException
	{
		final YoutubeChannelDownloader downloader = new YoutubeChannelDownloader(null, "AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");
		downloader.printVideoViewsfromItemApi();
		downloader.channelStatistics();
	}

	private void channelStatistics() throws IOException
	{
		final List<Channel> channels = youtube.channels()
				.list("snippet,status,id,statistics")
				.setKey(apiKey)
				.setForUsername("RichoDemus")
				.execute()
				.getItems();

		final String channelName = channels.stream()
				.map(Channel::getSnippet)
				.map(ChannelSnippet::getTitle)
				.findAny()
				.orElse("CHANNELNAME_NOT_FOUND");
		final BigInteger views = channels.stream()
				.map(Channel::getStatistics)
				.map(ChannelStatistics::getViewCount)
				.findAny()
				.orElse(BigInteger.ZERO);

		System.out.println(channelName + " has " + views + " views :(");
	}

	private void printVideoViewsfromItemApi() throws IOException
	{
		final List<Video> items = youtube.videos()
				.list("snippet,status,id,statistics,contentDetails")
				.setKey(apiKey)
				.setId("NVbH1BVXywY")
				.execute()
				.getItems();

		final String title = items.stream()
				.map(Video::getSnippet)
				.map(VideoSnippet::getTitle)
				.findAny()
				.orElse("TITLE_NOT_FOUND");
		final String id = items.stream()
				.map(Video::getSnippet)
				.map(VideoSnippet::getTitle)
				.findAny()
				.orElse("TITLE_NOT_FOUND");
		final BigInteger viewCount = items.stream()
				.map(Video::getStatistics)
				.map(VideoStatistics::getViewCount)
				.findAny()
				.orElse(BigInteger.ZERO);
		final String duration = items.stream()
				.map(Video::getContentDetails)
				.map(VideoContentDetails::getDuration)
				.findAny()
				.orElse("UNKNOWN DURATION");
		System.out.println(title + "(" + id + ") has " + viewCount + " views and is " + duration + " long");
	}

	public Optional<YoutubeVideoChunk> getVideoChunk(FeedId feedId)
	{
		final List<Channel> channels;
		try
		{
			channels = youtube.channels()
					.list("contentDetails")
					.setKey(apiKey)
					.setId(feedId.getValue())
					.execute()
					.getItems();
		}
		catch (IOException e)
		{
			logger.error("Unable to find channel {}", feedId, e);
			return Optional.empty();
		}

		if (channels == null)
		{
			logger.error("Got null channels when searching for channel {}", feedId);
			return Optional.empty();
		}

		if (channels.size() == 0)
		{
			logger.error("No such channel: {}", feedId);
			return Optional.empty();
		}

		final Optional<String> playlistId = Optional.of(channels.get(0)).map(this::toPlaylistId);

		if (!playlistId.isPresent())
		{
			logger.error("Did not get a playlistId for channel {}", feedId);
			return Optional.empty();
		}

		return Optional.of(new YoutubeVideoChunk(youtube, durationParser, playlistId.get(), apiKey, this));
	}

	public Map<ItemId, DurationAndViewcount> getStatistics(final ItemId id)
	{
		return getStatistics(id.getValue());
	}

	public Map<ItemId, DurationAndViewcount> getStatistics(final String ids)
	{
		final List<Video> items;
		try
		{
			items = youtube.videos()
					.list("statistics,contentDetails")
					.setKey(apiKey)
					.setId(ids)
					.execute()
					.getItems();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return items.stream()
				.collect(Collectors.toMap(video -> new ItemId(video.getId()), this::toDurationAndViewCount));
	}

	private DurationAndViewcount toDurationAndViewCount(final Video video)
	{
		final Duration duration = getDuration(video);
		final long views = getViews(video);

		return new DurationAndViewcount(duration, views);
	}

	private Duration getDuration(Video video)
	{
		return durationParser.fromYoutubeDuration(video.getContentDetails().getDuration());
	}

	private long getViews(final Video video)
	{
		final BigInteger views = video.getStatistics().getViewCount();
		try
		{
			return views.longValueExact();
		}
		catch (ArithmeticException e)
		{
			logger.error("Unable to convert view count of {} to a long for video {}", views, video.getId());
			return views.longValue();
		}
	}

	public DurationAndViewcount getDurationAndViewCount(final String itemId)
	{
		final List<Video> items;
		try
		{
			items = youtube.videos()
					.list("snippet,status,id,statistics,contentDetails")
					.setKey(apiKey)
					.setId(itemId)
					.execute()
					.getItems();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		Long viewCount;
		try
		{
			viewCount = items.stream()
					.map(Video::getStatistics)
					.map(VideoStatistics::getViewCount)
					.map(BigInteger::longValueExact)
					.findAny()
					.orElseThrow(() -> new RuntimeException("Unable to get view count for video " + itemId));
		}
		catch (ArithmeticException e)
		{
			final BigInteger count = items.stream().map(Video::getStatistics).map(VideoStatistics::getViewCount).findAny().orElseThrow(() -> new RuntimeException("This pretty much can't happen, itemId " + itemId, e));
			logger.error("Unable to convert view count of {} to a long for video {}", count, itemId);
			viewCount = 0L;
		}
		final Duration duration = items.stream()
				.map(Video::getContentDetails)
				.map(VideoContentDetails::getDuration)
				.map(durationParser::fromYoutubeDuration)
				.findAny()
				.orElseThrow(() -> new RuntimeException("Unable to get duration for video " + itemId));
		return new DurationAndViewcount(duration, viewCount);
	}

	private String toPlaylistId(final Channel channel)
	{
		return channel.getContentDetails().getRelatedPlaylists().getUploads();
	}
}
