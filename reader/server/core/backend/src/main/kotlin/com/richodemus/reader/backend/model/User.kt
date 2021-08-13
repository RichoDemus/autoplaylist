package com.richodemus.reader.backend.model

import com.google.common.collect.ImmutableMap
import com.richodemus.reader.backend.exception.UserNotSubscribedToThatChannelException
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import java.util.function.Consumer

class User {
    @JvmField
    val id: UserId
    val name: Username
    private val feeds: MutableMap<FeedId, MutableSet<ItemId>>
    var nextLabelId: Long = 0
        private set

    constructor(id: UserId, username: Username, nextLabelId: Long, feedsIds: MutableMap<FeedId, MutableSet<ItemId>>) {
        this.id = id
        name = username
        this.nextLabelId = nextLabelId
        feeds = feedsIds
    }

    constructor(userId: UserId, username: Username, feedIds: Set<FeedId>) {
        id = userId
        name = username
        feeds = HashMap()
        feedIds.forEach(Consumer { id: FeedId -> feeds[id] = HashSet() })
    }

    fun getFeeds(): Map<FeedId, MutableSet<ItemId>> {
        return ImmutableMap.copyOf(feeds)
    }

    fun addFeed(feedId: FeedId) {
        feeds[feedId] = HashSet()
    }

    @Throws(UserNotSubscribedToThatChannelException::class)
    fun markAsRead(feedId: FeedId, itemId: ItemId?) {
        if (!feeds.containsKey(feedId)) {
            throw UserNotSubscribedToThatChannelException("$name is not subscribed to feed $feedId")
        }
        feeds[feedId]!!.add(itemId!!)
    }

    fun isRead(feedId: FeedId?, videoId: ItemId?): Boolean {
        return feeds[feedId]!!.contains(videoId)
    }

    fun markAsUnRead(feedId: FeedId?, itemId: ItemId?) {
        feeds[feedId]!!.remove(itemId)
    }

    @Synchronized
    fun incrementAndGetNextLabelId(): Long {
        return nextLabelId++
    }

    override fun toString(): String {
        return name.toString()
    }
}
