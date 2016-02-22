package com.richo.reader.youtube_feed_service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeriodicDownloadOrchestrator
{
	private final ScheduledExecutorService executor;

	public PeriodicDownloadOrchestrator()
	{
		executor = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("yt-downloader-%s").build());
	}

	public void start()
	{
		executor.scheduleAtFixedRate(this::addDownloadsTasksToExecutor, calculateDelayUntilRun(), 1, TimeUnit.DAYS);
	}

	public void stop()
	{
		executor.shutdown();
	}

	private void addDownloadsTasksToExecutor()
	{
		
	}

	private long calculateDelayUntilRun()
	{
		return 0L;
	}
}
