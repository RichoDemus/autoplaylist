package com.richo.reader.backend.persistence;

import com.richo.reader.backend.youtube.model.YoutubeChannel;

import java.util.Optional;

public interface ChannelPersister
{
	public Optional<YoutubeChannel> getChannel(String channelName);

	public void updateChannel(YoutubeChannel channel);
}
