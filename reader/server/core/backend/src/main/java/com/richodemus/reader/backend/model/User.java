package com.richodemus.reader.backend.model;

import com.google.common.collect.ImmutableMap;
import com.richodemus.reader.backend.exception.UserNotSubscribedToThatChannelException;
import com.richodemus.reader.dto.FeedId;
import com.richodemus.reader.dto.ItemId;
import com.richodemus.reader.dto.UserId;
import com.richodemus.reader.dto.Username;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User {
    public final UserId id;
    private final Username name;
    private final Map<FeedId, Set<ItemId>> feeds;
    private long nextLabelId;

    public User(final UserId id, final Username username, final long nextLabelId, final Map<FeedId, Set<ItemId>> feedsIds) {
        this.id = id;
        this.name = username;
        this.nextLabelId = nextLabelId;
        this.feeds = feedsIds;
    }

    public User(final UserId userId, Username username, Set<FeedId> feedIds) {
        this.id = userId;
        this.name = username;
        this.feeds = new HashMap<>();
        feedIds.forEach(id -> feeds.put(id, new HashSet<>()));
    }

    public Map<FeedId, Set<ItemId>> getFeeds() {
        return ImmutableMap.copyOf(feeds);
    }

    public void addFeed(FeedId feedId) {
        feeds.put(feedId, new HashSet<>());
    }

    public Username getName() {
        return name;
    }

    public void markAsRead(FeedId feedId, ItemId itemId) throws UserNotSubscribedToThatChannelException {
        if (!feeds.containsKey(feedId)) {
            throw new UserNotSubscribedToThatChannelException(name + " is not subscribed to feed " + feedId);
        }

        feeds.get(feedId).add(itemId);
    }

    public boolean isRead(FeedId feedId, ItemId videoId) {
        return feeds.get(feedId).contains(videoId);
    }

    public void markAsUnRead(FeedId feedId, ItemId itemId) {
        feeds.get(feedId).remove(itemId);
    }

    public long getNextLabelId() {
        return nextLabelId;
    }

    public synchronized long incrementAndGetNextLabelId() {
        return nextLabelId++;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
