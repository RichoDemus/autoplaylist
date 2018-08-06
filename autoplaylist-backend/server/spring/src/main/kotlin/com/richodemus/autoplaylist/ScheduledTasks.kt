package com.richodemus.autoplaylist

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
internal class ScheduledTasks {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Runs 05:05 every day
    @Scheduled(cron = "0 5 5 1/1 * ?")
    fun test() {
        logger.info("This should print at 05:05")
    }
}
