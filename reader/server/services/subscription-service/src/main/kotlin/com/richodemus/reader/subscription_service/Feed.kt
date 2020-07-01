package com.richodemus.reader.subscription_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.events_v2.Event
import com.richodemus.reader.events_v2.UserUnwatchedItem
import com.richodemus.reader.events_v2.UserWatchedItem
import org.slf4j.LoggerFactory

data class Feed(val id: FeedId, val watchedItems: List<ItemId>) {
    constructor(id: FeedId) : this(id, emptyList())

    private val logger = LoggerFactory.getLogger(javaClass)
    internal fun process(evt: Event): Feed {
        return when {
            evt is UserWatchedItem && evt.feedId == id -> watchItem(evt)
            evt is UserUnwatchedItem && evt.feedId == id -> unwatchItem(evt)
            else -> this
        }
    }

    private fun watchItem(evt: UserWatchedItem): Feed {
        if (watchedItems.contains(evt.itemId)) {
            logger.warn("Item ${evt.itemId} is already watched in Feed $id")
            return this
        }
        return this.copy(watchedItems = watchedItems.plus(evt.itemId))
    }

    private fun unwatchItem(evt: UserUnwatchedItem): Feed {
        if (!watchedItems.contains(evt.itemId)) {
            logger.warn("Item ${evt.itemId} is already not watched in Feed $id")
            return this
        }
        return this.copy(watchedItems = watchedItems.minus(evt.itemId))
    }
}
