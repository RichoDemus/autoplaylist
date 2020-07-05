package com.richodemus.reader.youtube_feed_service

data class Videos(val videos: List<Video>) {
    companion object {
        internal fun empty() = Videos(emptyList())
    }
}
