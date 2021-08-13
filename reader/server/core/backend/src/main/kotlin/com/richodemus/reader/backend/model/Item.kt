package com.richodemus.reader.backend.model

import com.richodemus.reader.dto.ItemId
import java.time.Duration
import java.time.LocalDate

class Item(val id: ItemId,
           val title: String,
           val description: String,
           val uploadDate: String,
           val url: String,
           val duration: Duration,
           val views: Long) {
    private fun toDoubleDigitSeconds(seconds: Long): String {
        val string = seconds.toString()
        return if (string.length == 1) {
            "0$string"
        } else string
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val item = o as Item
        return id == item.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun isBefore(targetItem: Item?): Boolean {
        return LocalDate.parse(uploadDate).isBefore(LocalDate.parse(targetItem!!.uploadDate))
    }
}
