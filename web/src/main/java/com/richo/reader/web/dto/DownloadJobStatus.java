package com.richo.reader.web.dto;

import java.time.LocalDateTime;

public class DownloadJobStatus
{
	public String lastRun;
	public final boolean running;

	public DownloadJobStatus(final LocalDateTime lastRun, boolean running)
	{
		this.lastRun = lastRun.toString();
		this.running = running;
	}
}
