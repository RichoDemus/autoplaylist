package com.richodemus.reader.subscription_service

import com.richodemus.reader.dto.UserId
import com.richodemus.reader.events_v2.Event
import com.richodemus.reader.events_v2.UserSubscribedToFeed
import com.richodemus.reader.events_v2.UserUnwatchedItem
import com.richodemus.reader.events_v2.UserWatchedItem
import org.slf4j.LoggerFactory

data class User(val id: UserId,
                val feeds: List<Feed>) {
    constructor(userId: UserId) : this(userId, emptyList())

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun process(evt: Event): User {
        return when {
            evt is UserSubscribedToFeed && evt.userId == id -> subscribe(evt)
            evt is UserWatchedItem && evt.userId == id -> watch(evt)
            evt is UserUnwatchedItem && evt.userId == id -> unwatch(evt)
            else -> this
        }
    }

    private fun subscribe(evt: UserSubscribedToFeed): User {
        if (feeds.any { it.id == evt.feedId }) {
            logger.warn("User $id is already subscribed to ${evt.feedId}")
            return this
        }
        return this.copy(feeds = feeds.plus(Feed(evt.feedId)))
    }

    private fun watch(evt: UserWatchedItem): User {
        if (feeds.none { it.id == evt.feedId }) {
            logger.warn("User $id was not subscribed to feed ${evt.feedId} when watching item ${evt.itemId}")
            return this
        }
        return this.copy(feeds = feeds.map { it.process(evt) })
    }

    private fun unwatch(evt: UserUnwatchedItem): User {
        if (feeds.none { it.id == evt.feedId }) {
            logger.warn("User $id was not subscribed to feed ${evt.feedId} when watching item ${evt.itemId}")
            return this
        }
        return this.copy(feeds = feeds.map { it.process(evt) })
    }
}
