package com.richodemus.reader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import java.util.*

@SpringBootApplication
open class ReaderApplication

fun main(args: Array<String>) {
	runApplication<ReaderApplication>(*args)
}
