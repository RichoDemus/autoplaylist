package com.richodemus.autoplaylist.dto.events

import java.time.ZoneOffset
import java.time.ZonedDateTime

internal fun now() = ZonedDateTime.now(ZoneOffset.UTC).toString()
