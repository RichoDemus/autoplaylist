package com.richodemus.autoplaylist.spotify

// todo use some spring thing instead

private val scope = "playlist-read-private"

internal fun clientId() = env("CLIENT_ID")
internal fun clientSecret() = env("CLIENT_SECRET")
internal fun redirectUrl() = env("REDIRECT_URL")
internal fun scope() = scope

private fun env(name: String) = System.getenv(name) ?: throw IllegalStateException("Missing env $name")
