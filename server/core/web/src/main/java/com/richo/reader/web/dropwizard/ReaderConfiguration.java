package com.richo.reader.web.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class ReaderConfiguration extends Configuration
{
	private final String saveRoot;

	public ReaderConfiguration(@JsonProperty("saveRoot") String saveRoot)
	{
		this.saveRoot = saveRoot;
	}

	String getSaveRoot()
	{
		return saveRoot;
	}
}
