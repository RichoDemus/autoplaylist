package com.richodemus.reader.dto

class Session(val username: UserId, val token: String) {
    init {
        require(token.isNotBlank()) { "Token can't be empty" }
    }
}
