package com.richodemus.reader.dto

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName

data class Feed(val id: FeedId, val name: FeedName, val items: List<Item>)
