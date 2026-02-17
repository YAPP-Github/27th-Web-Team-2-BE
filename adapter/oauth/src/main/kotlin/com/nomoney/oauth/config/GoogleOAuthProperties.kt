package com.nomoney.oauth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.google")
data class GoogleOAuthProperties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)
