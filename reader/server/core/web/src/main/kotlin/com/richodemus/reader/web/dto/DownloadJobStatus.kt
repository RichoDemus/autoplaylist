package com.richodemus.reader.web.dto

import java.time.LocalDateTime

class DownloadJobStatus(lastRun: LocalDateTime, val running: Boolean, lastRunOutCome:String) {
    val lastRun: String = lastRun.toString()
    val lastRunOutCome:String = lastRunOutCome
}
