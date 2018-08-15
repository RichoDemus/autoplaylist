package com.richodemus.autoplaylist

import com.richodemus.autoplaylist.dto.UserId
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import javax.servlet.http.HttpSession

@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

internal fun HttpSession.getUserId(): UserId? {
    return this.getAttribute("userId") as UserId?
}
