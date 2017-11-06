package com.richodemus.reader.common.kafka_adapter

internal data class WrapperEvent(val id: String, val type: String, val page: Long, val data: String)
