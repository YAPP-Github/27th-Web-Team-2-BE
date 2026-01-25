package com.nomoney.api.auth

import com.nomoney.api.auth.model.IssueTokenRequest
import com.nomoney.api.auth.model.RefreshTokenRequest
import com.nomoney.auth.domain.AuthToken
import com.nomoney.auth.domain.RefreshToken
import com.nomoney.auth.domain.RefreshTokenId
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.service.AuthService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime

class AuthControllerTest : DescribeSpec({

    val authService = mockk<AuthService>()
    val authController = AuthController(authService)

    describe("AuthController") {

        describe("POST /api/v1/auth/token") {

            it("사용자 ID로 요청하면 TokenPair(accessToken + refreshToken)를 반환한다") {
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val accessTokenExpiresAt = now.plusDays(30)
                val refreshTokenExpiresAt = now.plusDays(90)

                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "access-token-value",
                        userId = userId,
                        expiresAt = accessTokenExpiresAt,
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "refresh-token-value",
                        userId = userId,
                        expiresAt = refreshTokenExpiresAt,
                        used = false,
                        createdAt = now,
                    ),
                )

                every { authService.issueTokenPair(userId) } returns tokenPair

                val request = IssueTokenRequest(userId = 1L)
                val response = authController.issueToken(request)

                response.accessToken shouldBe "access-token-value"
                response.accessTokenExpiresAt shouldBe accessTokenExpiresAt
                response.refreshToken shouldBe "refresh-token-value"
                response.refreshTokenExpiresAt shouldBe refreshTokenExpiresAt
            }
        }

        describe("POST /api/v1/auth/refresh") {

            it("유효한 리프레시 토큰으로 요청하면 새로운 TokenPair를 반환한다") {
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val accessTokenExpiresAt = now.plusDays(30)
                val refreshTokenExpiresAt = now.plusDays(90)
                val oldRefreshTokenValue = "old-refresh-token"

                val newTokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "new-access-token-value",
                        userId = userId,
                        expiresAt = accessTokenExpiresAt,
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(2L),
                        tokenValue = "new-refresh-token-value",
                        userId = userId,
                        expiresAt = refreshTokenExpiresAt,
                        used = false,
                        createdAt = now,
                    ),
                )

                every { authService.refreshToken(oldRefreshTokenValue) } returns newTokenPair

                val request = RefreshTokenRequest(refreshToken = oldRefreshTokenValue)
                val response = authController.refreshToken(request)

                response.accessToken shouldBe "new-access-token-value"
                response.accessTokenExpiresAt shouldBe accessTokenExpiresAt
                response.refreshToken shouldBe "new-refresh-token-value"
                response.refreshTokenExpiresAt shouldBe refreshTokenExpiresAt
            }
        }
    }
},)
