package com.nomoney.auth.port

import com.nomoney.auth.domain.SocialLinkToken
import com.nomoney.auth.domain.UserId
import java.time.LocalDateTime

interface SocialLinkTokenRepository {
    fun save(userId: UserId, tokenValue: String, expiresAt: LocalDateTime): SocialLinkToken
    fun findByTokenValue(tokenValue: String): SocialLinkToken?
    fun markAsUsed(tokenValue: String)
}
