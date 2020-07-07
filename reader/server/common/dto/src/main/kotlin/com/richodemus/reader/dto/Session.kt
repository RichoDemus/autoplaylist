package com.richodemus.reader.dto

class Session(val username: Username, val token: String) {
    init {
        require(token.isNotBlank()) { "${javaClass.simpleName} can't be empty" }
    }
}
