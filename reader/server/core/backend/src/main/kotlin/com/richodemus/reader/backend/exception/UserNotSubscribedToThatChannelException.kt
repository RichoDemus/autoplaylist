package com.richodemus.reader.backend.exception

class UserNotSubscribedToThatChannelException(msg: String?) : RuntimeException(msg) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
