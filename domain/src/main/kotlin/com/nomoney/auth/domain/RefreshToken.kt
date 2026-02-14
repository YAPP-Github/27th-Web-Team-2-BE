package com.nomoney.auth.domain

import java.time.LocalDateTime

@JvmInline
value class RefreshTokenId(val value: Long)

data class RefreshToken(
    val id: RefreshTokenId,
    val tokenValue: String,
    val userId: UserId,
    val expiresAt: LocalDateTime,
    val used: Boolean,
    val createdAt: LocalDateTime,
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
}
