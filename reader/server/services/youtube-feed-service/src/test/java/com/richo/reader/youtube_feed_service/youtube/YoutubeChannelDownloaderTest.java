package com.richo.reader.youtube_feed_service.youtube;


import com.google.api.services.youtube.model.PlaylistItem;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class YoutubeChannelDownloaderTest {
    @Ignore("This test uses the live youtube api")
    @Test
    public void testDownloadVideosFromSingleVideoChannel() {

        final YoutubeChannelDownloader downloader = new YoutubeChannelDownloader(null, "AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");

        final YoutubeVideoChunk chunk = downloader.getVideoChunk(new FeedId("RichoDemus")).get();

        List<PlaylistItem> videoChunk = chunk.getNextVideoChunk();

        Assert.assertEquals(videoChunk.size(), 1);
        Assert.assertEquals("T-Rex optical illusion", videoChunk.get(0).getSnippet().getTitle());
        Assert.assertEquals("Zs6bAFlcH0M", videoChunk.get(0).getSnippet().getResourceId().getVideoId());
        Assert.assertEquals(1409920676000L, videoChunk.get(0).getSnippet().getPublishedAt().getValue());
        Assert.assertEquals(0, videoChunk.get(0).getSnippet().getPublishedAt().getTimeZoneShift());

    }

    @Ignore("This test uses the live youtube api")
    @Test
    public void testDownloadVideosFromChannelWithLotsOfVideos() {
        final YoutubeChannelDownloader downloader = new YoutubeChannelDownloader(null, "AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");

        final YoutubeVideoChunk chunk = downloader.getVideoChunk(new FeedId("Thunderf00t")).get();

        List<PlaylistItem> videos = chunk.getNextVideoChunk();

        Assert.assertEquals(videos.size(), 50);
        videos.stream().map((video) -> video.getSnippet().getTitle()).collect(Collectors.toList()).forEach(System.out::println);

        videos = chunk.getNextVideoChunk();

        Assert.assertEquals(videos.size(), 50);
        videos.stream().map((video) -> video.getSnippet().getTitle()).collect(Collectors.toList()).forEach(System.out::println);
    }

    @Ignore("This test uses the live youtube api")
    @Test
    public void shouldGetFeedName() {
        final YoutubeChannelDownloader target = new YoutubeChannelDownloader(null, "AIzaSyChI7lMyLfc1ckOqcC-z2Oz-Lrq6d09x30");

        final FeedName result = target.getName(new FeedId("UCf1iroepad-o5w2il-06Gjg")).get();

        assertThat(result).isEqualTo(new FeedName("Armoured Media"));
    }
}
