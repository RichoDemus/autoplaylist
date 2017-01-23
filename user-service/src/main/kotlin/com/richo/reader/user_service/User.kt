package com.richo.reader.user_service

import com.richodemus.reader.dto.FeedId
import com.richodemus.reader.dto.ItemId
import com.richodemus.reader.dto.Label
import com.richodemus.reader.dto.Username

data class User(val name: Username, val feeds: Map<FeedId, List<ItemId>>, val nextLabelId: Long, val labels: List<Label>)
