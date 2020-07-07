package com.richodemus.reader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ReaderApplication

fun main(args: Array<String>) {
    runApplication<ReaderApplication>(*args)
}
