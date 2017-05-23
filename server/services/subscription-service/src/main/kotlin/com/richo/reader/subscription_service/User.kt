package com.richo.reader.subscription_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.LegacyLabel
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.slf4j.LoggerFactory

// todo rewrite this
data class User(var id: UserId, val name: Username, val feeds: MutableMap<FeedId, MutableList<ItemId>>, val nextLabelId: Long, val labels: List<LegacyLabel>) {
    constructor(userId: UserId) : this(userId, Username("unknown"), mutableMapOf(), 0L, emptyList())

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun subscribe(feedId: FeedId) {
        if (feeds.containsKey(feedId)) {
            logger.info("User $id is already subscribed to $feedId")
            return
        }
        feeds.put(feedId, mutableListOf())
    }

    internal fun watch(feedId: FeedId, itemId: ItemId) {
        if (!feeds.containsKey(feedId)) {
            throw IllegalStateException("User $id is not subscribed to $feedId")
        }
        feeds[feedId]!!.add(itemId)
    }

    internal fun unWatch(feedId: FeedId, itemId: ItemId) {
        if (!feeds.containsKey(feedId)) {
            throw IllegalStateException("User $id is not subscribed to $feedId")
        }
        feeds[feedId]!!.remove(itemId)
    }
}
