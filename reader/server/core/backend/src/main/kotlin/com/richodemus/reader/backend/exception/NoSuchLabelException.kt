package com.richodemus.reader.backend.exception

class NoSuchLabelException(msg: String?) : Exception(msg) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
