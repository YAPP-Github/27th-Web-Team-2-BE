package com.nomoney.api.auth

import com.nomoney.api.config.OAuthRedirectProperties
import com.nomoney.auth.domain.AuthToken
import com.nomoney.auth.domain.RefreshToken
import com.nomoney.auth.domain.RefreshTokenId
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.service.AnonymousAuthService
import com.nomoney.auth.service.AuthService
import com.nomoney.auth.service.SocialAuthService
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import java.time.LocalDateTime

class AuthControllerTest : DescribeSpec({

    val authService = mockk<AuthService>()
    val socialAuthService = mockk<SocialAuthService>()
    val anonymousAuthService = mockk<AnonymousAuthService>()
    val oauthRedirectProperties = OAuthRedirectProperties(
        successUrl = "https://example.com/auth/callback",
        failureUrl = "https://example.com/auth/failure",
        domains = mapOf(
            "local" to "http://localhost:3000",
            "sandbox" to "https://dev.weddin.kr",
            "prod" to "https://weddin.kr",
        ),
    )
    val authController = AuthController(authService, socialAuthService, anonymousAuthService, oauthRedirectProperties)

    describe("AuthController") {

        describe("GET /api/v1/auth/oauth/kakao") {

            it("유효한 인증 코드로 요청 시 토큰 쿠키를 설정하고 성공 URL로 리다이렉트한다") {
                // given
                val code = "valid-kakao-auth-code"
                val state = "state"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "kakao-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "kakao-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.KAKAO, code, state) } returns tokenPair

                // when
                authController.kakaoLogin(code, state, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("https://example.com/auth/callback?state=$state") }
            }

            it("state 값이 있을 때 성공 URL에 state 쿼리 파라미터를 포함하여 리다이렉트한다") {
                // given
                val code = "valid-kakao-auth-code"
                val state = "random-csrf-token"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "kakao-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "kakao-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.KAKAO, code, state) } returns tokenPair

                // when
                authController.kakaoLogin(code, state, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("https://example.com/auth/callback?state=$state") }
            }

            it("state가 local|list여도 local 환경의 callback 경로로 리다이렉트한다") {
                // given
                val code = "valid-kakao-auth-code"
                val state = "local|list"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "kakao-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "kakao-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.KAKAO, code, state) } returns tokenPair

                // when
                authController.kakaoLogin(code, state, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("http://localhost:3000/auth/callback?state=$state") }
            }

            it("소셜 로그인 실패 시 실패 URL로 리다이렉트한다") {
                // given
                val code = "invalid-kakao-code"
                val state = "state"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)

                every {
                    socialAuthService.loginWithSocialProvider(SocialProvider.KAKAO, code, state)
                } throws RuntimeException("카카오 로그인 실패")

                // when
                authController.kakaoLogin(code, state, httpServletResponse)

                // then
                verify { httpServletResponse.sendRedirect("https://example.com/auth/failure") }
            }
        }

        describe("GET /api/v1/auth/oauth/google") {

            it("유효한 인증 코드로 요청 시 토큰 쿠키를 설정하고 성공 URL로 리다이렉트한다") {
                // given
                val code = "valid-google-auth-code"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "google-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "google-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.GOOGLE, code, null) } returns tokenPair

                // when
                authController.googleLogin(code, null, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("https://example.com/auth/callback") }
            }

            it("state 값이 있을 때 성공 URL에 state 쿼리 파라미터를 포함하여 리다이렉트한다") {
                // given
                val code = "valid-google-auth-code"
                val state = "random-csrf-token"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "google-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "google-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.GOOGLE, code, state) } returns tokenPair

                // when
                authController.googleLogin(code, state, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("https://example.com/auth/callback?state=$state") }
            }

            it("state가 sandbox이면 sandbox callback 경로로 리다이렉트한다") {
                // given
                val code = "valid-google-auth-code"
                val state = "sandbox"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "google-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "google-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.GOOGLE, code, state) } returns tokenPair

                // when
                authController.googleLogin(code, state, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("https://dev.weddin.kr/auth/callback?state=$state") }
            }

            it("state 환경이 prod인 경우 운영 callback 경로로 리다이렉트한다") {
                // given
                val code = "valid-google-auth-code"
                val state = "prod"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)
                val userId = UserId(1L)
                val now = LocalDateTime.now()
                val tokenPair = TokenPair(
                    accessToken = AuthToken(
                        tokenValue = "google-access-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(30),
                        createdAt = now,
                    ),
                    refreshToken = RefreshToken(
                        id = RefreshTokenId(1L),
                        tokenValue = "google-refresh-token-value",
                        userId = userId,
                        expiresAt = now.plusDays(90),
                        used = false,
                        createdAt = now,
                    ),
                )

                every { socialAuthService.loginWithSocialProvider(SocialProvider.GOOGLE, code, state) } returns tokenPair

                // when
                authController.googleLogin(code, state, httpServletResponse)

                // then
                verify(exactly = 2) { httpServletResponse.addCookie(any()) }
                verify { httpServletResponse.sendRedirect("https://weddin.kr/auth/callback?state=$state") }
            }

            it("소셜 로그인 실패 시 실패 URL로 리다이렉트한다") {
                // given
                val code = "invalid-google-code"
                val httpServletResponse = mockk<HttpServletResponse>(relaxed = true)

                every {
                    socialAuthService.loginWithSocialProvider(SocialProvider.GOOGLE, code, null)
                } throws RuntimeException("구글 로그인 실패")

                // when
                authController.googleLogin(code, null, httpServletResponse)

                // then
                verify { httpServletResponse.sendRedirect("https://example.com/auth/failure") }
            }
        }
    }
},)
