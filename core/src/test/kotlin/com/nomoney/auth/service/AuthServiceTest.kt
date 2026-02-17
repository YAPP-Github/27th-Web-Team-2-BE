package com.nomoney.auth.service

import com.nomoney.auth.domain.AuthToken
import com.nomoney.auth.domain.RefreshToken
import com.nomoney.auth.domain.RefreshTokenId
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.AuthTokenRepository
import com.nomoney.auth.port.RefreshTokenRepository
import com.nomoney.exception.InvalidRefreshTokenException
import com.nomoney.exception.UnauthorizedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

class AuthServiceTest : DescribeSpec({

    val authTokenRepository = mockk<AuthTokenRepository>()
    val refreshTokenRepository = mockk<RefreshTokenRepository>()
    val authService = AuthService(authTokenRepository, refreshTokenRepository)

    describe("AuthService") {

        describe("issueTokenPair") {

            it("UserId로 AccessToken과 RefreshToken을 동시에 발급한다") {
                val userId = UserId(1L)
                val authTokenSlot = slot<AuthToken>()
                val refreshTokenSlot = slot<RefreshToken>()

                every { authTokenRepository.save(capture(authTokenSlot)) } answers {
                    authTokenSlot.captured
                }
                every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers {
                    refreshTokenSlot.captured.copy(id = RefreshTokenId(1L))
                }

                val tokenPair = authService.issueTokenPair(userId)

                tokenPair.accessToken shouldNotBe null
                tokenPair.accessToken.userId shouldBe userId
                tokenPair.accessToken.tokenValue.isNotBlank() shouldBe true

                tokenPair.refreshToken shouldNotBe null
                tokenPair.refreshToken.userId shouldBe userId
                tokenPair.refreshToken.tokenValue.isNotBlank() shouldBe true
                tokenPair.refreshToken.used shouldBe false
            }

            it("AccessToken의 만료기간은 30일이다") {
                val userId = UserId(1L)
                val authTokenSlot = slot<AuthToken>()
                val refreshTokenSlot = slot<RefreshToken>()
                val now = LocalDateTime.now()

                every { authTokenRepository.save(capture(authTokenSlot)) } answers {
                    authTokenSlot.captured
                }
                every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers {
                    refreshTokenSlot.captured.copy(id = RefreshTokenId(1L))
                }

                val tokenPair = authService.issueTokenPair(userId)

                val expectedExpiresAt = now.plusDays(30)
                tokenPair.accessToken.expiresAt.dayOfYear shouldBe expectedExpiresAt.dayOfYear
            }

            it("RefreshToken의 만료기간은 90일이다") {
                val userId = UserId(1L)
                val authTokenSlot = slot<AuthToken>()
                val refreshTokenSlot = slot<RefreshToken>()
                val now = LocalDateTime.now()

                every { authTokenRepository.save(capture(authTokenSlot)) } answers {
                    authTokenSlot.captured
                }
                every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers {
                    refreshTokenSlot.captured.copy(id = RefreshTokenId(1L))
                }

                val tokenPair = authService.issueTokenPair(userId)

                val expectedExpiresAt = now.plusDays(90)
                tokenPair.refreshToken.expiresAt.dayOfYear shouldBe expectedExpiresAt.dayOfYear
            }
        }

        describe("refreshToken") {

            it("유효한 RefreshToken으로 새 TokenPair를 발급한다") {
                val userId = UserId(1L)
                val refreshTokenValue = "valid-refresh-token"
                val existingRefreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = refreshTokenValue,
                    userId = userId,
                    expiresAt = LocalDateTime.now().plusDays(30),
                    used = false,
                    createdAt = LocalDateTime.now().minusDays(1),
                )

                val authTokenSlot = slot<AuthToken>()
                val newRefreshTokenSlot = slot<RefreshToken>()

                every { refreshTokenRepository.findByTokenValue(refreshTokenValue) } returns existingRefreshToken
                every { refreshTokenRepository.markAsUsed(refreshTokenValue) } returns Unit
                every { authTokenRepository.save(capture(authTokenSlot)) } answers {
                    authTokenSlot.captured
                }
                every { refreshTokenRepository.save(capture(newRefreshTokenSlot)) } answers {
                    newRefreshTokenSlot.captured.copy(id = RefreshTokenId(2L))
                }

                val tokenPair = authService.refreshToken(refreshTokenValue)

                tokenPair.accessToken shouldNotBe null
                tokenPair.accessToken.userId shouldBe userId
                tokenPair.refreshToken shouldNotBe null
                tokenPair.refreshToken.userId shouldBe userId
                tokenPair.refreshToken.used shouldBe false

                verify { refreshTokenRepository.markAsUsed(refreshTokenValue) }
            }

            it("유효하지 않은 RefreshToken으로 요청 시 InvalidRefreshTokenException이 발생한다") {
                val invalidTokenValue = "invalid-token"

                every { refreshTokenRepository.findByTokenValue(invalidTokenValue) } returns null

                shouldThrow<InvalidRefreshTokenException> {
                    authService.refreshToken(invalidTokenValue)
                }
            }

            it("만료된 RefreshToken으로 요청 시 InvalidRefreshTokenException이 발생한다") {
                val userId = UserId(1L)
                val expiredTokenValue = "expired-token"
                val expiredRefreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = expiredTokenValue,
                    userId = userId,
                    expiresAt = LocalDateTime.now().minusDays(1),
                    used = false,
                    createdAt = LocalDateTime.now().minusDays(100),
                )

                every { refreshTokenRepository.findByTokenValue(expiredTokenValue) } returns expiredRefreshToken

                shouldThrow<InvalidRefreshTokenException> {
                    authService.refreshToken(expiredTokenValue)
                }
            }

            it("이미 사용된 RefreshToken으로 요청 시 InvalidRefreshTokenException이 발생한다 (RTR)") {
                val userId = UserId(1L)
                val usedTokenValue = "used-token"
                val usedRefreshToken = RefreshToken(
                    id = RefreshTokenId(1L),
                    tokenValue = usedTokenValue,
                    userId = userId,
                    expiresAt = LocalDateTime.now().plusDays(30),
                    used = true,
                    createdAt = LocalDateTime.now().minusDays(1),
                )

                every { refreshTokenRepository.findByTokenValue(usedTokenValue) } returns usedRefreshToken

                shouldThrow<InvalidRefreshTokenException> {
                    authService.refreshToken(usedTokenValue)
                }
            }
        }

        describe("issueToken") {

            it("UserId로 AuthToken을 발급한다") {
                val userId = UserId(1L)
                val authTokenSlot = slot<AuthToken>()

                every { authTokenRepository.save(capture(authTokenSlot)) } answers {
                    authTokenSlot.captured
                }

                val authToken = authService.issueToken(userId)

                authToken.userId shouldBe userId
                authToken.tokenValue.isNotBlank() shouldBe true
            }
        }

        describe("validateToken") {

            it("유효한 토큰으로 User를 반환한다") {
                val tokenValue = "valid-token"
                val userId = UserId(1L)
                val authToken = AuthToken(
                    tokenValue = tokenValue,
                    userId = userId,
                    expiresAt = LocalDateTime.now().plusDays(1),
                    createdAt = LocalDateTime.now(),
                )

                every { authTokenRepository.findByTokenValue(tokenValue) } returns authToken

                val user = authService.validateToken(tokenValue)

                user.id shouldBe userId
            }

            it("유효하지 않은 토큰으로 요청 시 UnauthorizedException이 발생한다") {
                val invalidTokenValue = "invalid-token"

                every { authTokenRepository.findByTokenValue(invalidTokenValue) } returns null

                shouldThrow<UnauthorizedException> {
                    authService.validateToken(invalidTokenValue)
                }
            }

            it("만료된 토큰으로 요청 시 UnauthorizedException이 발생한다") {
                val expiredTokenValue = "expired-token"
                val authToken = AuthToken(
                    tokenValue = expiredTokenValue,
                    userId = UserId(1L),
                    expiresAt = LocalDateTime.now().minusDays(1),
                    createdAt = LocalDateTime.now().minusDays(31),
                )

                every { authTokenRepository.findByTokenValue(expiredTokenValue) } returns authToken

                shouldThrow<UnauthorizedException> {
                    authService.validateToken(expiredTokenValue)
                }
            }
        }
    }
},)
