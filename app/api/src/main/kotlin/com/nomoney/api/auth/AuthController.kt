package com.nomoney.api.auth

import com.nomoney.api.auth.model.IssueTokenRequest
import com.nomoney.api.auth.model.IssueTokenResponse
import com.nomoney.api.auth.model.RefreshTokenCookieResponse
import com.nomoney.api.auth.model.RefreshTokenRequest
import com.nomoney.api.auth.model.RefreshTokenResponse
import com.nomoney.api.config.OAuthRedirectProperties
import com.nomoney.api.swagger.SwaggerApiOperation
import com.nomoney.api.swagger.SwaggerApiTag
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

@Tag(name = SwaggerApiTag.AUTH, description = SwaggerApiTag.AUTH_DESCRIPTION)
@RestController
class AuthController(
    private val authService: AuthService,
    private val socialAuthService: SocialAuthService,
    private val oauthRedirectProperties: OAuthRedirectProperties,
) {

    @Operation(
        summary = SwaggerApiOperation.Auth.ISSUE_TOKEN_SUMMARY,
        description = SwaggerApiOperation.Auth.ISSUE_TOKEN_DESCRIPTION,
    )
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

    @Operation(
        summary = SwaggerApiOperation.Auth.REFRESH_TOKEN_SUMMARY,
        description = SwaggerApiOperation.Auth.REFRESH_TOKEN_DESCRIPTION,
    )
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

    @Operation(
        summary = SwaggerApiOperation.Auth.GOOGLE_SOCIAL_LOGIN_SUMMARY,
        description = SwaggerApiOperation.Auth.GOOGLE_SOCIAL_LOGIN_DESCRIPTION,
    )
    @GetMapping("/api/v1/auth/oauth/google")
    fun googleLogin(
        @RequestParam code: String,
        response: HttpServletResponse,
    ) {
        try {
            val tokenPair = socialAuthService.loginWithSocialProvider(
                provider = SocialProvider.GOOGLE,
                authorizationCode = code,
            )

            setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

            response.sendRedirect(oauthRedirectProperties.successUrl)
        } catch (e: Exception) {
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    @Operation(
        summary = SwaggerApiOperation.Auth.REFRESH_TOKEN_WITH_COOKIE_SUMMARY,
        description = SwaggerApiOperation.Auth.REFRESH_TOKEN_WITH_COOKIE_DESCRIPTION,
    )
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
