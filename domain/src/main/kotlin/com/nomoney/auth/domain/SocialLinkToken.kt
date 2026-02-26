package com.nomoney.auth.domain

import java.time.LocalDateTime

data class SocialLinkToken(
    val tokenValue: String,
    val userId: UserId,
    val expiresAt: LocalDateTime,
    val used: Boolean,
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
