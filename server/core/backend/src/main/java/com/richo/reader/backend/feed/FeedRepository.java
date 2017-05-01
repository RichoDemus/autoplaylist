package com.richo.reader.backend.feed;

import com.richo.reader.backend.model.Feed;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;

import java.util.Optional;

public interface FeedRepository
{
	Optional<Feed> getFeed(FeedId feedId);

	void registerChannel(FeedId feedId);

	FeedId getFeedId(FeedUrl feedUrl);
}
