package com.richo.reader.web.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class ReaderConfiguration extends Configuration
{
	private boolean offlineMode;

	@JsonProperty
	public void setOfflineMode(boolean offlineMode)
	{
		this.offlineMode = offlineMode;
	}

	@JsonProperty
	public boolean isOfflineMode()
	{
		return offlineMode;
	}
}
