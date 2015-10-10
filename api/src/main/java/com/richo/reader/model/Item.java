package com.richo.reader.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
