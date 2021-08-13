package com.richodemus.reader.backend.exception

class ItemNotInFeedException(msg: String?) : RuntimeException(msg) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
