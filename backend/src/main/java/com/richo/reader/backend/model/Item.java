package com.richo.reader.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Item
{
	private final String id;
	private final String title;
	private final String description;
	private final String uploadDate;
	private final String url;

	@JsonCreator
	public Item(@JsonProperty("id") String id, @JsonProperty("title") String title, @JsonProperty("description") String description, @JsonProperty("uploadDate") String uploadDate, @JsonProperty("url") String url)
	{
		this.id = id;
		this.title = title;
		this.description = description;
		this.uploadDate = uploadDate;
		this.url = url;
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

	public String getUploadDate()
	{
		return uploadDate;
	}

	public String getUrl()
	{
		return url;
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
		return Objects.equals(id, item.id) &&
				Objects.equals(title, item.title) &&
				Objects.equals(description, item.description) &&
				Objects.equals(uploadDate, item.uploadDate) &&
				Objects.equals(url, item.url);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, title, description, uploadDate, url);
	}
}
