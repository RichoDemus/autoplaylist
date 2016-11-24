package com.richo.reader.web;

import com.richo.reader.backend.model.Feed;
import com.richodemus.reader.dto.FeedId;

import java.util.Arrays;
import java.util.List;

public class TestData
{
	public static final Feed FEED1 = new Feed(new FeedId("id1"), new FeedId("name1"), 2);
	public static final Feed FEED2 = new Feed(new FeedId("id2"), new FeedId("name2"), 2);
	public static final List<Feed> FEEDS = Arrays.asList(FEED1, FEED2);
}
