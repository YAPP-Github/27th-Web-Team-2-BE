package com.nomoney.auth.port

import com.nomoney.auth.domain.AuthToken

interface AuthTokenRepository {
    fun save(authToken: AuthToken): AuthToken
    fun findByTokenValue(tokenValue: String): AuthToken?
}
