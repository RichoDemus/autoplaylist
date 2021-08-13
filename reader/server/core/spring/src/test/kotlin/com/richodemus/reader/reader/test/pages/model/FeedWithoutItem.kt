package com.richodemus.reader.reader.test.pages.model

import com.fasterxml.jackson.annotation.JsonProperty

class FeedWithoutItem(@param:JsonProperty("id") val id: String, @param:JsonProperty("name") val name: String, @param:JsonProperty("numberOfAvailableItems") val numberOfAvailableItems: Int)
