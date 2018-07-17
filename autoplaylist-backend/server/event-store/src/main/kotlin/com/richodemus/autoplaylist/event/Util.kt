package com.richodemus.autoplaylist.event

import java.time.ZoneOffset
import java.time.ZonedDateTime

internal fun now() = ZonedDateTime.now(ZoneOffset.UTC).toString()
