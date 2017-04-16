package com.richo.reader.user_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId

data class Feed(val id: FeedId, val watchedItems: List<ItemId>)
