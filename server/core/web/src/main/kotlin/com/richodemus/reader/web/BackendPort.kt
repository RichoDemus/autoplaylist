package com.richodemus.reader.web

import com.richo.reader.backend.Backend
import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedUrl
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.UserId
import com.richodemus.reader.web.dto.Feed
import com.richodemus.reader.web.dto.FeedWithoutItems
import com.richodemus.reader.web.dto.Item
import java.util.Optional
import javax.inject.Inject

class BackendPort @Inject constructor(val backend: Backend) {
    fun getAllFeedsWithoutItems(username: UserId) = backend.getAllFeedsWithoutItems(username).map { f -> f.toDto() }

    fun getFeed(username: UserId, feedId: FeedId): Feed? {
        return backend.getFeed(username, feedId).toDto()
    }

    fun addFeed(username: UserId, feedUrl: FeedUrl) {
        backend.addFeed(username, feedUrl)
    }

    fun markAsRead(username: UserId, feedId: FeedId, itemId: ItemId) {
        backend.markAsRead(username, feedId, itemId)
    }

    fun markAsUnread(username: UserId, feedId: FeedId, itemId: ItemId) {
        backend.markAsUnread(username, feedId, itemId)
    }

    fun markOlderItemsAsRead(username: UserId, feedId: FeedId, itemId: ItemId) {
        backend.markOlderItemsAsRead(username, feedId, itemId)
    }

    private fun com.richo.reader.backend.model.FeedWithoutItems.toDto(): FeedWithoutItems {
        return FeedWithoutItems(this.id, this.name, this.numberOfAvailableItems)
    }

    private fun Optional<com.richo.reader.backend.model.Feed>.toDto(): Feed? {
        if (!this.isPresent) {
            return null
        }
        val feed = this.get()
        val items = feed.items.map { item -> item.toDto() }
        return Feed(feed.id, feed.name, items)
    }

    private fun com.richo.reader.backend.model.Item.toDto(): Item {
        return Item(this.id, this.title, this.description, this.uploadDate, this.url, this.duration, this.views)
    }

}
