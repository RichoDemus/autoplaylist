package com.richodemus.reader.dto

class Session(val username: Username, val token: String) {
    init {
        require(token.isNotBlank()) { "Token can't be empty" }
    }
}
