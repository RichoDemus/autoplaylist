package com.richodemus.autoplaylist.spotify

// todo use some spring thing instead

internal fun clientId() = env("CLIENT_ID")
internal fun clientSecret() = env("CLIENT_SECRET")
internal fun redirectUrl() = env("REDIRECT_URL")

private fun env(name: String) = System.getenv(name) ?: throw IllegalStateException("Missing env $name")
