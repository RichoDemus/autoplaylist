package com.richodemus.reader.dto

import com.richodemus.reader.dto.ItemId
import java.time.Duration

class Item(val id: ItemId,
           val title: String,
           val description: String,
           val uploadDate: String,
           val url: String,
           duration: Duration,
           val views: Long) {
    val duration: String

    init {
        this.duration = durationToString(duration)
    }

    private fun durationToString(duration: Duration): String {
        return "" + duration.toMinutes() + ":" + toDoubleDigitSeconds(duration.minusMinutes(duration.toMinutes()).seconds)
    }

    private fun toDoubleDigitSeconds(seconds: Long): String {
        val string = seconds.toString()
        if (string.length == 1) {
            return "0$string"
        }
        return string
    }
}
