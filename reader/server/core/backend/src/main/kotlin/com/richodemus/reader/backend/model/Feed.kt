package com.richodemus.reader.backend.model

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import java.util.Objects

class Feed(val id: FeedId, val name: FeedName?, val items: List<Item>) {
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val feed = o as Feed
        return id == feed.id &&
                name == feed.name &&
                items == feed.items
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, items)
    }

    override fun toString(): String {
        return name.toString() + "(" + id + "), " + items.size + " items"
    }
}
