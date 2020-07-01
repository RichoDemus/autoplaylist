package com.richodemus.reader.youtube_feed_service;

import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFileSystemPersistenceTest {
    private JsonFileSystemPersistence target;

    @Before
    public void setUp() {
        target = new JsonFileSystemPersistence("target/data/" + UUID.randomUUID());

    }

    @Test
    public void shouldSaveStuffInTheRightPlace() {
        target.updateChannel(new Feed(new FeedId("id"), new FeedName("name"), new ArrayList<>(), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC)));
        Assert.assertTrue(new File("target/data/").exists());
    }

    @Test
    public void shouldBeAbleToLoadSavedData() {
        final FeedId feedId = new FeedId("my-channel");
        final FeedName feedName = new FeedName("my-channel");
        final Item firstVideo = new Item("id1", "title1", "desc1", 0L, 0L, 0L, 0L);
        final Item secondVideo = new Item("id2", "title2", "desc2", 0L, 0L, 0L, 0L);
        final Feed expected = new Feed(feedId, feedName, Arrays.asList(firstVideo, secondVideo), LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC));
        target.updateChannel(expected);

        final Optional<Feed> maybeResult = target.getChannel(feedId);

        Assert.assertTrue("Should've gotten a channel", maybeResult.isPresent());
        final Feed result = maybeResult.get();
        Assert.assertEquals(expected, result);
        assertThat(result.getItems()).isEqualTo(expected.getItems());
    }
}
