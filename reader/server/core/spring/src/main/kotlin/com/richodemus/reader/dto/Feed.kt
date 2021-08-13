package com.richodemus.reader.dto

data class Feed(val id: FeedId, val name: FeedName?, val items: List<Item>)
