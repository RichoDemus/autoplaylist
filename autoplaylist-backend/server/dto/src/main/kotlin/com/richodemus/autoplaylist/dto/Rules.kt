package com.richodemus.autoplaylist.dto

data class Rules(
        val artists: List<ArtistId> = emptyList(),
        val exclusions: List<Exclusion> = emptyList()
)
