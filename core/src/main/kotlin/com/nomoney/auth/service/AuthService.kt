package com.nomoney.auth.service

import com.nomoney.auth.domain.AuthToken
import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.AuthTokenRepository
import com.nomoney.exception.UnauthorizedException
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authTokenRepository: AuthTokenRepository,
) {
    private val random = SecureRandom()

    fun issueToken(userId: UserId): AuthToken {
        val tokenValue = generateTokenValue()
        val expiresAt = LocalDateTime.now().plusDays(TOKEN_EXPIRY_DAYS)

        val authToken = AuthToken(
            tokenValue = tokenValue,
            userId = userId,
            expiresAt = expiresAt,
            createdAt = LocalDateTime.now(),
        )

        return authTokenRepository.save(authToken)
    }

    fun validateToken(tokenValue: String): User {
        val authToken = authTokenRepository.findByTokenValue(tokenValue)
            ?: throw UnauthorizedException("유효하지 않은 토큰입니다. token: $tokenValue")

        if (authToken.isExpired()) {
            throw UnauthorizedException("토큰이 만료되었습니다. expiresAt: ${authToken.expiresAt}")
        }

        return User(
            id = authToken.userId,
        )
    }

    private fun generateTokenValue(): String {
        val bytes = ByteArray(TOKEN_LENGTH)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    companion object {
        private const val TOKEN_EXPIRY_DAYS = 30L
        private const val TOKEN_LENGTH = 32
    }
}
