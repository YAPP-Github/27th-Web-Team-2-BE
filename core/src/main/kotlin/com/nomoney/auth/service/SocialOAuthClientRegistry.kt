package com.nomoney.auth.service

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.port.SocialOAuthClient
import com.nomoney.exception.UnsupportedSocialProviderException
import org.springframework.stereotype.Component

@Component
class SocialOAuthClientRegistry(
    private val clients: List<SocialOAuthClient>,
) {
    fun getClient(provider: SocialProvider): SocialOAuthClient {
        return clients.find { it.supports(provider) }
            ?: throw UnsupportedSocialProviderException("지원하지 않는 소셜 로그인 제공자: $provider")
    }
}
