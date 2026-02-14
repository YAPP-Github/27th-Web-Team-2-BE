package com.nomoney.auth.domain

data class TokenPair(
    val accessToken: AuthToken,
    val refreshToken: RefreshToken,
)
