package com.richodemus.reader.common.chronicler_adapter

import com.richodemus.reader.events.EventType

internal data class ChroniclerEvent(val id: String, val type: EventType, val data: String)
