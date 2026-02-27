package com.nomoney.auth.port

import com.nomoney.auth.domain.SocialOAuthToken
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId

interface SocialOAuthTokenRepository {
    fun findByUserIdAndProvider(userId: UserId, provider: SocialProvider): SocialOAuthToken?
    fun upsert(token: SocialOAuthToken): SocialOAuthToken
}
