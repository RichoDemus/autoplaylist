package com.richo.reader.backend.feed;

import com.richo.reader.backend.model.Feed;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;
import com.richodemus.reader.dto.ItemId;

import java.util.List;
import java.util.Optional;

public interface FeedRepository
{
	Optional<Feed> getFeed(FeedId feedId);

	List<ItemId> getItemIds(FeedId feedId);

	void registerChannel(FeedId feedId);

	FeedId getFeedId(FeedUrl feedUrl);
}
