package com.nomoney.api.auth

import com.nomoney.api.config.OAuthRedirectProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OAuthRedirectProperties::class)
class AuthConfig
