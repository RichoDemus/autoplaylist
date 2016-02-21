package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Item
{
	private final String id;
	private final String title;
	private final String description;
	private final LocalDateTime uploadDate;

	@JsonCreator
	public Item(@JsonProperty("id") String id,
				@JsonProperty("title") String title,
				@JsonProperty("description") String description,
				@JsonProperty("uploadDate") long uploadDate)
	{
		this.id = id;
		this.title = title;
		this.description = description;
		this.uploadDate = LocalDateTime.ofEpochSecond(uploadDate, 0, ZoneOffset.UTC);
	}

	public String getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	@JsonIgnore
	public LocalDateTime getUploadDate()
	{
		return uploadDate;
	}

	@JsonProperty("uploadDate")
	public long getUploadDateAsLong()
	{
		return uploadDate.toEpochSecond(ZoneOffset.UTC);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Item item = (Item) o;
		return id.equals(item.getId());
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public String toString()
	{
		return title;
	}
}
