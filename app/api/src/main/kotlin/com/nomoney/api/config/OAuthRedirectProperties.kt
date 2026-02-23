package com.nomoney.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.redirect")
data class OAuthRedirectProperties(
    val successUrl: String,
    val failureUrl: String,
)
