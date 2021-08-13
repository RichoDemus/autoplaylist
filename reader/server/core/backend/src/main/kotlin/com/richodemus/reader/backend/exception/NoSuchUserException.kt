package com.richodemus.reader.backend.exception

class NoSuchUserException(msg: String?) : RuntimeException(msg) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
