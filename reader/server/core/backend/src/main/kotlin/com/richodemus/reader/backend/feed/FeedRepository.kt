package com.richodemus.reader.backend.feed

import com.richodemus.reader.backend.model.Feed
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl

interface FeedRepository {
    fun getFeed(feedId: FeedId): Feed?
    fun getFeedId(feedUrl: FeedUrl): FeedId?
}
