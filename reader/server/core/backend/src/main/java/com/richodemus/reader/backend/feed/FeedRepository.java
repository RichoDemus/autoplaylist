package com.richodemus.reader.backend.feed;

import com.richodemus.reader.backend.model.Feed;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedUrl;

import java.util.Optional;

public interface FeedRepository {
    Optional<Feed> getFeed(FeedId feedId);

    FeedId getFeedId(FeedUrl feedUrl);
}
