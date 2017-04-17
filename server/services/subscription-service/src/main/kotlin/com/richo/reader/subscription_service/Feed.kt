package com.richo.reader.subscription_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId

data class Feed(val id: FeedId, val watchedItems: List<ItemId>)
