package com.richo.reader.web;

import com.richo.reader.backend.model.FeedWithoutItems;
import com.richodemus.reader.dto.FeedId;

import java.util.Arrays;
import java.util.List;

public class TestData
{
	public static final FeedWithoutItems FEED1 = new FeedWithoutItems(new FeedId("id1"), new FeedId("name1"), 2);
	public static final FeedWithoutItems FEED2 = new FeedWithoutItems(new FeedId("id2"), new FeedId("name2"), 2);
	public static final List<FeedWithoutItems> FEEDS = Arrays.asList(FEED1, FEED2);
}
