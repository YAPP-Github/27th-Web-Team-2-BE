package com.nomoney.auth.domain

import java.time.LocalDateTime

data class KakaoOAuthToken(
    val accessToken: String,
    val accessTokenExpiresAt: LocalDateTime,
    val refreshToken: String?,
    val refreshTokenExpiresAt: LocalDateTime?,
    val scope: String?,
)
