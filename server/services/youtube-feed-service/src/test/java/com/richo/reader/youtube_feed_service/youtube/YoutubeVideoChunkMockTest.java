package com.richo.reader.youtube_feed_service.youtube;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class YoutubeVideoChunkMockTest {
    @Test
    public void shouldReturnNewestItemWhenCreatedWithOlderFirst() {
        final PlaylistItem older = new PlaylistItem().setSnippet(new PlaylistItemSnippet().setTitle("older").setPublishedAt(new DateTime(10000L)));
        final PlaylistItem newer = new PlaylistItem().setSnippet(new PlaylistItemSnippet().setTitle("newer").setPublishedAt(new DateTime(20000L)));

        final YoutubeVideoChunkMock target = new YoutubeVideoChunkMock(Arrays.asList(older, newer));

        final PlaylistItem firstResult = target.getNextVideoChunk().get(0);
        final PlaylistItem secondResult = target.getNextVideoChunk().get(0);

        assertThat(firstResult).isEqualTo(newer);
        assertThat(secondResult).isEqualTo(older);
    }

    @Test
    public void shouldReturnNewestItemWhenCreatedWithNewerFirst() {
        final PlaylistItem older = new PlaylistItem().setSnippet(new PlaylistItemSnippet().setTitle("older").setPublishedAt(new DateTime(10000L)));
        final PlaylistItem newer = new PlaylistItem().setSnippet(new PlaylistItemSnippet().setTitle("newer").setPublishedAt(new DateTime(20000L)));

        final YoutubeVideoChunkMock target = new YoutubeVideoChunkMock(Arrays.asList(newer, older));

        final PlaylistItem firstResult = target.getNextVideoChunk().get(0);
        final PlaylistItem secondResult = target.getNextVideoChunk().get(0);

        assertThat(firstResult).isEqualTo(newer);
        assertThat(secondResult).isEqualTo(older);
    }

    @Test
    public void shouldReturnEmptyListWhenEmpty() {
        final YoutubeVideoChunkMock target = new YoutubeVideoChunkMock(Lists.emptyList());
        assertThat(target.chunksLeft()).isEqualTo(0);
        assertThat(target.getNextVideoChunk()).isEmpty();
    }
}
