package com.richodemus.reader.backend.model

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName
import java.util.Objects

class FeedWithoutItems(val id: FeedId, val name: FeedName?, val numberOfAvailableItems: Int) {
    override fun toString(): String {
        return "FeedWithoutItems{" +
                "id=" + id +
                ", name=" + name +
                ", numberOfAvailableItems=" + numberOfAvailableItems +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as FeedWithoutItems
        return numberOfAvailableItems == that.numberOfAvailableItems &&
                id == that.id &&
                name == that.name
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, numberOfAvailableItems)
    }
}
