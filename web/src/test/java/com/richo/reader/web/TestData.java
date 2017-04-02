package com.richo.reader.web;

import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import com.richodemus.reader.web.dto.FeedWithoutItems;

import java.util.Arrays;
import java.util.List;

public class TestData
{
	public static final FeedWithoutItems FEED1 = new FeedWithoutItems(new FeedId("id1"), new FeedName("name1"), 2);
	public static final FeedWithoutItems FEED2 = new FeedWithoutItems(new FeedId("id2"), new FeedName("name2"), 2);
	public static final List<FeedWithoutItems> FEEDS = Arrays.asList(FEED1, FEED2);
}
