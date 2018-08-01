package com.richodemus.autoplaylist.spotify

class RateLimitExceededException(val retryAfter: Long) : Exception() {
    override fun toString(): String {
        return "RateLimitExceededException(retryAfter=$retryAfter)"
    }
}
