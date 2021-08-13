package com.richodemus.reader.reader.test.pages.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class Label @JsonCreator constructor(@param:JsonProperty("id") val id: Int, @param:JsonProperty("name") val name: String, @param:JsonProperty("feeds") val feeds: List<String>)
