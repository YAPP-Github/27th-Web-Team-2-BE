package com.nomoney.oauth.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties::class)
class OAuthConfig {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
