package com.richo.reader.youtube_feed_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richodemus.reader.dto.FeedId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;


public class JsonFileSystemPersistence {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String saveRoot;
    private final ObjectMapper objectMapper;

    @Inject
    JsonFileSystemPersistence(@Named("saveRoot") String saveRoot) {
        this.saveRoot = saveRoot;
        this.objectMapper = new ObjectMapper();
    }

    public Optional<Feed> getChannel(FeedId feedId) {
        try {
            final File file = new File(saveRoot + "/feeds/" + feedId + "/data.json");
            if (!file.exists()) {
                logger.debug("Feed {} not on disk", feedId);
                return Optional.empty();
            }
            logger.trace("Reading feed {} from disk", feedId);
            return Optional.ofNullable(objectMapper.readValue(file, Feed.class));
        } catch (Exception e) {
            logger.warn("Unable to load feed: {}", feedId, e);
            return Optional.empty();
        }
    }

    void updateChannel(final Feed feed) {
        try {
            feed.getItems().sort(Comparator.comparing(Item::getUploadDate));

            final String path = saveRoot + "/feeds/" + feed.getId();
            final boolean success = new File(path).mkdirs();
            logger.trace("Creating {} successful: {}", path, success);
            objectMapper.writeValue(new File(path + "/data.json"), feed);
        } catch (IOException e) {
            logger.warn("Unable to write feed {} to disk", feed.getId(), e);
        }
    }
}
