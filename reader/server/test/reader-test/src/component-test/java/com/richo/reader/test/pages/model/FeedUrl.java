package com.richo.reader.test.pages.model;

public class FeedUrl
{
	private final String value;

	public FeedUrl(final String value)
	{
		this.value = value;
	}

	public String toJson()
	{
		return "\"" + value + "\"";
	}

	@Override
	public String toString()
	{
		return value;
	}
}
