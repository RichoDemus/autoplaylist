package com.richodemus.reader.backend.exception

class NoSuchChannelException(s: String?) : RuntimeException(s) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
