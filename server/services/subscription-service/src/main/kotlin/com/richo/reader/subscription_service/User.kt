package com.richo.reader.subscription_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.LegacyLabel
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.dto.Username
import org.slf4j.LoggerFactory

data class User(val id: UserId, val name: Username, val feeds: MutableMap<FeedId, MutableList<ItemId>>, val nextLabelId: Long, val labels: List<LegacyLabel>) {
    private val logger = LoggerFactory.getLogger(javaClass)
    fun subscribe(feedId: FeedId) {
        if (feeds.containsKey(feedId)) {
            logger.info("User $name is already subscribed to $feedId")
            return
        }
        feeds.put(feedId, mutableListOf())
    }

    fun watch(feedId: FeedId, itemId: ItemId) {
        if (!feeds.containsKey(feedId)) {
            throw IllegalStateException("User $name is not subscribed to $feedId")
        }
        feeds[feedId]!!.add(itemId)
    }

    fun unWatch(feedId: FeedId, itemId: ItemId) {
        if (!feeds.containsKey(feedId)) {
            throw IllegalStateException("User $name is not subscribed to $feedId")
        }
        feeds[feedId]!!.remove(itemId)
    }
}
