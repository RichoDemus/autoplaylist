package com.richo.reader.web;

import com.richo.reader.backend.model.Feed;

import java.util.Arrays;
import java.util.List;

public class TestData
{
	public static final Feed FEED1 = new Feed("id1", "name1", 2);
	public static final Feed FEED2 = new Feed("id2", "name2", 2);
	public static final List<Feed> FEEDS = Arrays.asList(FEED1, FEED2);
}
