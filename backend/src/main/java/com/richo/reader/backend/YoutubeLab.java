package com.richo.reader.backend;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class YoutubeLab
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private YouTube youtube;

	public static void main(String[] args) throws IOException
	{
		new YoutubeLab().getMyVideos();
	}

	public void getMyVideos() throws IOException
	{
		youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {
		}).setApplicationName("youtube-cmdline-search-sample").build();


		final String apiKey = "AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30";


		final ChannelListResponse result = youtube.channels().list("contentDetails").setKey(apiKey).setForUsername("RichoDemus").execute();
		final List<Channel> channels = result.getItems();
		System.out.println(channels.size());
		channels.forEach((chan) -> {
			try
			{
				final String playListId = chan.getContentDetails().getRelatedPlaylists().getUploads();

				youtube.channels().list("snippet").setKey(apiKey).setId(playListId).execute();
				final List<PlaylistItem> snippets = youtube.playlistItems().list("snippet").setKey(apiKey).setPlaylistId(playListId).execute().getItems();
				System.out.println(snippets.size());
				snippets.forEach((item) ->
				{
					System.out.println("Title: " + item.getSnippet().getTitle());
				});


			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});

	}
}
