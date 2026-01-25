package com.nomoney.auth.domain

import java.time.LocalDateTime

data class AuthToken(
    val tokenValue: String,
    val userId: UserId,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime,
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
}
