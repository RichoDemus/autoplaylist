package com.richodemus.reader.web.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class ItemOperation @JsonCreator
constructor(@JsonProperty("action") action: String) {
    val action: Operation

    init {
        this.action = Operation.valueOf(action)
    }

    override fun toString(): String {
        return action.toString()
    }

    enum class Operation {
        MARK_READ, MARK_UNREAD, MARK_OLDER_ITEMS_AS_READ
    }

    companion object {
        val MARK_AS_READ = ItemOperation("MARK_READ")
        val MARK_AS_UNREAD = ItemOperation("MARK_UNREAD")
        val MARK_OLDER_ITEMS_AS_READ = ItemOperation("MARK_OLDER_ITEMS_AS_READ")
    }
}
