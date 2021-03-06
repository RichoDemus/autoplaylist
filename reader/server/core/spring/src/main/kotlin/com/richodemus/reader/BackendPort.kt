package com.richodemus.reader

import com.richodemus.reader.backend.Backend
import com.richodemus.reader.dto.Feed
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.dto.FeedWithoutItems
import com.richodemus.reader.dto.Item
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.Username
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class BackendPort(private val backend: Backend) {
    fun getAllFeedsWithoutItems(username: Username) = backend.getAllFeedsWithoutItems(username).map { f -> f.toDto() }

    fun getFeed(username: Username, feedId: FeedId): Feed? {
        return backend.getFeed(username, feedId).toDto()
    }

    fun addFeed(username: Username, feedUrl: FeedUrl) {
        backend.addFeed(username, feedUrl)
    }

    fun markAsRead(username: Username, feedId: FeedId, itemId: ItemId) {
        backend.markAsRead(username, feedId, itemId)
    }

    fun markAsUnread(username: Username, feedId: FeedId, itemId: ItemId) {
        backend.markAsUnread(username, feedId, itemId)
    }

    fun markOlderItemsAsRead(username: Username, feedId: FeedId, itemId: ItemId) {
        backend.markOlderItemsAsRead(username, feedId, itemId)
    }

    private fun Optional<com.richodemus.reader.backend.model.Feed>.toDto(): Feed? {
        if (!this.isPresent) {
            return null
        }
        val feed = this.get()
        val items = feed.items.map { item -> item.toDto() }
        return Feed(feed.id, feed.name, items)
    }

    private fun com.richodemus.reader.backend.model.Item.toDto(): Item {
        return Item(this.id, this.title, this.description, this.uploadDate, this.url, this.duration, this.views)
    }

    private fun com.richodemus.reader.backend.model.FeedWithoutItems.toDto(): FeedWithoutItems {
        return FeedWithoutItems(this.id, this.name, this.numberOfAvailableItems)
    }
}
