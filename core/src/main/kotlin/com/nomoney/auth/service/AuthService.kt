package com.nomoney.auth.service

import com.nomoney.auth.domain.AuthToken
import com.nomoney.auth.domain.RefreshToken
import com.nomoney.auth.domain.RefreshTokenId
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.AuthTokenRepository
import com.nomoney.auth.port.RefreshTokenRepository
import com.nomoney.exception.UnauthorizedException
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authTokenRepository: AuthTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
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

    fun issueTokenPair(userId: UserId): TokenPair {
        val accessToken = issueToken(userId)
        val refreshToken = createRefreshToken(userId)

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun refreshToken(refreshTokenValue: String): TokenPair {
        val existingRefreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
            ?: throw UnauthorizedException("유효하지 않은 리프레시 토큰입니다. token: $refreshTokenValue")

        if (existingRefreshToken.isExpired()) {
            throw UnauthorizedException("리프레시 토큰이 만료되었습니다. expiresAt: ${existingRefreshToken.expiresAt}")
        }

        if (existingRefreshToken.used) {
            throw UnauthorizedException("이미 사용된 리프레시 토큰입니다. token: $refreshTokenValue")
        }

        refreshTokenRepository.markAsUsed(refreshTokenValue)

        return issueTokenPair(existingRefreshToken.userId)
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

    private fun createRefreshToken(userId: UserId): RefreshToken {
        val tokenValue = generateTokenValue()
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(REFRESH_TOKEN_EXPIRY_DAYS)

        val refreshToken = RefreshToken(
            id = RefreshTokenId(0L),
            tokenValue = tokenValue,
            userId = userId,
            expiresAt = expiresAt,
            used = false,
            createdAt = now,
        )

        return refreshTokenRepository.save(refreshToken)
    }

    private fun generateTokenValue(): String {
        val bytes = ByteArray(TOKEN_LENGTH)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    companion object {
        private const val TOKEN_EXPIRY_DAYS = 30L
        private const val REFRESH_TOKEN_EXPIRY_DAYS = 90L
        private const val TOKEN_LENGTH = 32
    }
}
