package com.richodemus.reader.common.kafka_adapter

import com.richodemus.reader.events.EventType

//todo remove
internal data class ChroniclerEvent(val id: String, val type: EventType, val data: String)