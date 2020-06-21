package com.richo.reader.youtube_feed_service;

import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.FeedName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonFileSystemPersistenceTest {
    private JsonFileSystemPersistence<String> target;
    private String path;

    @Before
    public void setUp() {
        path = "target/data/" + UUID.randomUUID();
        target = new JsonFileSystemPersistence<>(path, "filename", String.class);
    }

    @Test
    public void shouldSaveStuffInTheRightPlace() throws IOException {
        String path = "target/data/" + UUID.randomUUID();
        JsonFileSystemPersistence<Channel> target = new JsonFileSystemPersistence<>(path, "filename", Channel.class);

        target.updateChannel("id", new Channel(
                new FeedId("channel-id"),
                new FeedName("Channel"),
                OffsetDateTime.parse("2007-12-03T10:15:30+01:00"),
                OffsetDateTime.parse("2007-12-03T10:15:30+01:00")
        ));
        assertTrue(new File(path + "/feeds/id/filename.json").exists());
        String data = Files.readString(Paths.get(path + "/feeds/id/filename.json"));
        System.out.println(data);
    }

    @Test
    public void shouldBeAbleToLoadSavedData() {
        target.updateChannel("id", "cool-data");

        final Optional<String> maybeResult = target.getChannel("id");

        assertTrue("Should've gotten a channel", maybeResult.isPresent());
        final String result = maybeResult.get();
        assertEquals("cool-data", result);
    }
}
