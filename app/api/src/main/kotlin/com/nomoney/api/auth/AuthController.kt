package com.nomoney.api.auth

import com.nomoney.api.auth.model.IssueTokenRequest
import com.nomoney.api.auth.model.IssueTokenResponse
import com.nomoney.api.auth.model.RefreshTokenCookieResponse
import com.nomoney.api.auth.model.RefreshTokenRequest
import com.nomoney.api.auth.model.RefreshTokenResponse
import com.nomoney.api.config.OAuthRedirectProperties
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.service.AuthService
import com.nomoney.auth.service.SocialAuthService
import com.nomoney.exception.UnauthorizedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증", description = "인증 관련 API")
@RestController
class AuthController(
    private val authService: AuthService,
    private val socialAuthService: SocialAuthService,
    private val oauthRedirectProperties: OAuthRedirectProperties,
) {

    @Operation(summary = "토큰 발급", description = "사용자의 액세스 토큰과 리프레시 토큰을 발급합니다 (임시 API)")
    @PostMapping("/api/v1/auth/token")
    fun issueToken(
        @RequestBody request: IssueTokenRequest,
    ): IssueTokenResponse {
        val tokenPair = authService.issueTokenPair(
            userId = UserId(request.userId),
        )

        return IssueTokenResponse(
            accessToken = tokenPair.accessToken.tokenValue,
            accessTokenExpiresAt = tokenPair.accessToken.expiresAt,
            refreshToken = tokenPair.refreshToken.tokenValue,
            refreshTokenExpiresAt = tokenPair.refreshToken.expiresAt,
        )
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다")
    @PostMapping("/api/v1/auth/refresh")
    fun refreshToken(
        @RequestBody request: RefreshTokenRequest,
    ): RefreshTokenResponse {
        val tokenPair = authService.refreshToken(
            refreshTokenValue = request.refreshToken,
        )

        return RefreshTokenResponse(
            accessToken = tokenPair.accessToken.tokenValue,
            accessTokenExpiresAt = tokenPair.accessToken.expiresAt,
            refreshToken = tokenPair.refreshToken.tokenValue,
            refreshTokenExpiresAt = tokenPair.refreshToken.expiresAt,
        )
    }

    @Operation(summary = "구글 소셜 로그인", description = "구글 OAuth 인증 코드를 사용하여 로그인합니다. 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키로 설정하고 프론트엔드 URL로 리다이렉트합니다. state 값이 있으면 리다이렉트 URL에 포함됩니다.")
    @GetMapping("/api/v1/auth/oauth/google")
    fun googleLogin(
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        response: HttpServletResponse,
    ) {
        try {
            val tokenPair = socialAuthService.loginWithSocialProvider(
                provider = SocialProvider.GOOGLE,
                authorizationCode = code,
                state= state,
            )

            setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

            response.sendRedirect(buildSuccessRedirectUrl(state))
        } catch (e: Exception) {
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    @Operation(summary = "카카오 소셜 로그인", description = "카카오 OAuth 인증 코드를 사용하여 로그인합니다. 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키로 설정하고 프론트엔드 URL로 리다이렉트합니다. state 값이 있으면 리다이렉트 URL에 포함됩니다.")
    @GetMapping("/api/v1/auth/oauth/kakao")
    fun kakaoLogin(
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        response: HttpServletResponse,
    ) {
        try {
            val tokenPair = socialAuthService.loginWithSocialProvider(
                provider = SocialProvider.KAKAO,
                authorizationCode = code,
                state = state,
            )

            setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

            response.sendRedirect(buildSuccessRedirectUrl(state))
        } catch (e: Exception) {
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    private fun buildSuccessRedirectUrl(state: String?): String {
        val baseUrl = oauthRedirectProperties.successUrl
        return if (state != null) "$baseUrl?state=$state" else baseUrl
    }

    @Operation(summary = "쿠키 기반 토큰 갱신", description = "HttpOnly 쿠키에 저장된 리프레시 토큰을 사용하여 액세스 토큰과 리프레시 토큰을 갱신합니다.")
    @PostMapping("/api/v1/auth/refresh-cookie")
    fun refreshTokenWithCookie(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): RefreshTokenCookieResponse {
        val refreshTokenValue = extractRefreshTokenFromCookie(request)

        val tokenPair = authService.refreshToken(refreshTokenValue)

        setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

        return RefreshTokenCookieResponse()
    }

    private fun extractRefreshTokenFromCookie(request: HttpServletRequest): String {
        val cookies = request.cookies
            ?: throw UnauthorizedException("쿠키 정보가 필요합니다.")

        val refreshTokenCookie = cookies.firstOrNull { it.name == REFRESH_TOKEN_COOKIE_NAME }
            ?: throw UnauthorizedException("리프레시 토큰 쿠키가 필요합니다.")

        if (refreshTokenCookie.value.isBlank()) {
            throw UnauthorizedException("리프레시 토큰이 비어있습니다.")
        }

        return refreshTokenCookie.value
    }

    private fun setTokenCookies(
        response: HttpServletResponse,
        accessTokenValue: String,
        refreshTokenValue: String,
    ) {
        // HttpOnly 쿠키로 액세스 토큰 설정
        val accessTokenCookie = Cookie(ACCESS_TOKEN_COOKIE_NAME, accessTokenValue).apply {
            isHttpOnly = true
            secure = true // HTTPS에서만 전송
            path = "/"
            maxAge = Duration.ofDays(30).seconds.toInt()
        }
        response.addCookie(accessTokenCookie)

        // HttpOnly 쿠키로 리프레시 토큰 설정
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshTokenValue).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = Duration.ofDays(90).seconds.toInt()
        }
        response.addCookie(refreshTokenCookie)
    }

    companion object {
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
        private const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
    }
}
