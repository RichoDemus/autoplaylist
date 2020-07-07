package com.richodemus.reader.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class User @JsonCreator
constructor(@param:JsonProperty("feeds") val feeds: List<FeedWithoutItems>, @param:JsonProperty("labels") val labels: List<Label>)
