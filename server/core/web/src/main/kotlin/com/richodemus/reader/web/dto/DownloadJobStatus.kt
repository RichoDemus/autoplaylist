package com.richodemus.reader.web.dto

import java.time.LocalDateTime

class DownloadJobStatus(lastRun: LocalDateTime, val running: Boolean) {
    val lastRun: String = lastRun.toString()
}
