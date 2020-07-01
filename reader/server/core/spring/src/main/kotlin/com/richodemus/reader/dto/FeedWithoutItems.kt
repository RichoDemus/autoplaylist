package com.richodemus.reader.dto

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.FeedName

data class FeedWithoutItems(val id: FeedId, val name: FeedName, val numberOfAvailableItems: Int)
