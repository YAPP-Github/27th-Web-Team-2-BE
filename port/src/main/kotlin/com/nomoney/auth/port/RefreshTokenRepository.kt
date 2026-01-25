package com.nomoney.auth.port

import com.nomoney.auth.domain.RefreshToken

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken
    fun findByTokenValue(tokenValue: String): RefreshToken?
    fun markAsUsed(tokenValue: String)
}
