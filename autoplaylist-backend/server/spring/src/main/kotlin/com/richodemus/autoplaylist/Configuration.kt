package com.richodemus.autoplaylist

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
internal class Configuration {

    @Value("\${spotify.url}")
    private lateinit var apiUrl: String

    @Value("\${spotify.accountsUrl}")
    private lateinit var accountsUrl: String

    @Value("\${spotify.clientId}")
    private lateinit var clientId: String

    @Value("\${spotify.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${spotify.redirectUrl}")
    private lateinit var redirectUrl: String

    @Value("\${gcs.project}")
    private lateinit var gcsProject: String

    @Value("\${gcs.bucket}")
    private lateinit var gcsBucket: String

    @Bean(name = ["spotifyUrl"])
    fun url(): String {
        return apiUrl
    }

    @Bean(name = ["spotifyAccountsUrl"])
    fun accountsUrl(): String {
        return accountsUrl
    }

    @Bean(name = ["spotifyClientId"])
    fun clientId(): String {
        return clientId
    }

    @Bean(name = ["spotifyClientSecret"])
    fun clientSecret(): String {
        return clientSecret
    }

    @Bean(name = ["spotifyRedirectUrl"])
    fun redirectUrl(): String {
        return redirectUrl
    }

    @Bean(name = ["gcsProject"])
    fun gcsProject(): String {
        return gcsProject
    }

    @Bean(name = ["gcsBucket"])
    fun gcsBucket(): String {
        return gcsBucket
    }
}
