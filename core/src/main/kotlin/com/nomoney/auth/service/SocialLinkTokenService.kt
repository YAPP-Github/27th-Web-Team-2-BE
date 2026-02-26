package com.nomoney.auth.service

import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.SocialLinkTokenRepository
import com.nomoney.exception.InvalidRequestException
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialLinkTokenService(
    private val socialLinkTokenRepository: SocialLinkTokenRepository,
) {
    @Transactional
    fun issueLinkToken(userId: UserId): String {
        val tokenValue = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusMinutes(LINK_TOKEN_TTL_MINUTES)
        socialLinkTokenRepository.save(userId, tokenValue, expiresAt)
        return tokenValue
    }

    @Transactional
    fun consumeLinkToken(tokenValue: String): UserId {
        val token = socialLinkTokenRepository.findByTokenValue(tokenValue)
            ?: throw InvalidRequestException("유효하지 않은 연동 토큰입니다.", "tokenValue: $tokenValue")

        if (token.used) {
            throw InvalidRequestException("이미 사용된 연동 토큰입니다.", "tokenValue: $tokenValue")
        }

        if (token.isExpired()) {
            throw InvalidRequestException("만료된 연동 토큰입니다.", "tokenValue: $tokenValue")
        }

        socialLinkTokenRepository.markAsUsed(tokenValue)
        return token.userId
    }

    companion object {
        private const val LINK_TOKEN_TTL_MINUTES = 10L
    }
}
