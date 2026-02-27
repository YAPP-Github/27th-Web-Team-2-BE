package com.nomoney.auth.domain

import java.time.LocalDateTime

@JvmInline
value class SocialOAuthTokenId(val value: Long)

data class SocialOAuthToken(
    val id: SocialOAuthTokenId,
    val userId: UserId,
    val provider: SocialProvider,
    val accessToken: String,
    val refreshToken: String?,
    val accessTokenExpiresAt: LocalDateTime,
    val refreshTokenExpiresAt: LocalDateTime?,
    val scope: String?,
    val createdAt: LocalDateTime,
)
