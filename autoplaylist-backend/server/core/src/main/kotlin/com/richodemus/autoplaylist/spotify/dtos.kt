package com.richodemus.autoplaylist.spotify

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.richodemus.autoplaylist.dto.RefreshToken

data class Tokens(
        @JsonProperty("access_token") val accessToken: AccessToken,
        @JsonProperty("scope") val scope: String,
        @JsonProperty("token_type") val tokenType: String,
        @JsonProperty("expires_in") val expiresIn: Int,
        @JsonProperty("refresh_token") val refreshToken: RefreshToken?
)

data class AccessToken(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "AccessToken can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

data class PlayList(val id: PlayListId, val name: PlaylistName)
data class PlaylistName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "PlaylistName can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

data class PlayListId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "PlayListId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

data class ArtistName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "ArtistName can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

data class TrackName(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "TrackName can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}

data class Track(val id: TrackId, val name: TrackName, val uri: TrackUri)

data class TrackId(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "TrackId can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}


data class TrackUri(@get:JsonIgnore val value: String) {
    init {
        require(value.isNotBlank()) { "TrackUri can't be empty" }
    }

    @JsonValue
    override fun toString() = value
}
