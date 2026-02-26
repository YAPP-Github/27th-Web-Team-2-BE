package com.nomoney.api.auth

import com.nomoney.api.auth.model.IssueLinkTokenResponse
import com.nomoney.api.auth.model.RefreshTokenCookieResponse
import com.nomoney.api.auth.model.RefreshTokenRequest
import com.nomoney.api.auth.model.RefreshTokenResponse
import com.nomoney.api.config.OAuthRedirectProperties
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.service.AnonymousAuthService
import com.nomoney.auth.service.AuthService
import com.nomoney.auth.service.SocialAuthService
import com.nomoney.auth.service.SocialLinkService
import com.nomoney.auth.service.SocialLinkTokenService
import com.nomoney.exception.InvalidRequestException
import com.nomoney.exception.UnauthorizedException
import com.nomoney.support.logging.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
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
    private val socialLinkService: SocialLinkService,
    private val socialLinkTokenService: SocialLinkTokenService,
    private val anonymousAuthService: AnonymousAuthService,
    private val oauthRedirectProperties: OAuthRedirectProperties,
) {
    private val logger = logger()

    // =========================================================
    // 로그인
    // =========================================================

    @Operation(summary = "익명 로그인", description = "익명 사용자로 로그인합니다. 매 호출마다 새로운 익명 사용자가 생성됩니다.")
    @GetMapping("/api/v1/auth/anonymous")
    fun anonymousLogin(
        @RequestParam state: String?,
        response: HttpServletResponse,
    ) {
        val tokenPair = anonymousAuthService.loginAnonymously()

        setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

        response.sendRedirect(buildSuccessRedirectUrl(state))
    }

    @Operation(summary = "구글 소셜 로그인", description = "구글 OAuth 인증 코드를 사용하여 로그인합니다. 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키로 설정하고 프론트엔드 URL로 리다이렉트합니다. state 값이 있으면 리다이렉트 URL에 포함됩니다.")
    @GetMapping("/api/v1/auth/oauth/google")
    fun googleLogin(
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        try {
            val tokenPair = socialAuthService.loginWithSocialProvider(
                provider = SocialProvider.GOOGLE,
                authorizationCode = code,
                state = state,
                redirectUri = request.requestURL.toString(),
            )

            setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

            response.sendRedirect(buildSuccessRedirectUrl(state))
        } catch (e: Exception) {
            logger.error("구글 로그인 실패", e)
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    @Operation(summary = "카카오 소셜 로그인", description = "카카오 OAuth 인증 코드를 사용하여 로그인합니다. 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키로 설정하고 프론트엔드 URL로 리다이렉트합니다. state 값이 있으면 리다이렉트 URL에 포함됩니다.")
    @GetMapping("/api/v1/auth/oauth/kakao")
    fun kakaoLogin(
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        try {
            val tokenPair = socialAuthService.loginWithSocialProvider(
                provider = SocialProvider.KAKAO,
                authorizationCode = code,
                state = state,
                redirectUri = request.requestURL.toString(),
            )

            setTokenCookies(response, tokenPair.accessToken.tokenValue, tokenPair.refreshToken.tokenValue)

            response.sendRedirect(buildSuccessRedirectUrl(state))
        } catch (e: Exception) {
            logger.error("카카오 로그인 실패", e)
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    // =========================================================
    // 소셜 계정 연동
    // =========================================================

    @Operation(summary = "소셜 연동 토큰 발급", description = "소셜 계정 연동을 위한 일회성 토큰(UUID)을 발급합니다. 발급된 토큰은 10분간 유효하며 1회만 사용 가능합니다. 인증된 사용자만 호출 가능합니다.")
    @PostMapping("/api/v1/auth/link/token")
    fun issueLinkToken(): IssueLinkTokenResponse {
        val userId = getSecurityUserIdOrThrow()
        val linkToken = socialLinkTokenService.issueLinkToken(userId)
        return IssueLinkTokenResponse(linkToken = linkToken)
    }

    @Operation(summary = "구글 소셜 연동", description = "구글 OAuth 인증 코드를 사용하여 기존 계정에 구글 소셜 로그인을 연동합니다. state 파라미터에 {environmentKey}|{linkToken} 형식으로 전달해야 합니다.")
    @GetMapping("/api/v1/auth/oauth/google/link")
    fun googleLink(
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        try {
            val linkState = parseLinkRedirectState(state)

            val userId = socialLinkTokenService.consumeLinkToken(linkState.linkToken)
            socialLinkService.linkSocialAccount(userId, SocialProvider.GOOGLE, code, request.requestURL.toString(), state)

            response.sendRedirect(buildSuccessRedirectUrl(state))
        } catch (e: Exception) {
            logger.error("구글 연동 실패", e)
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    @Operation(summary = "카카오 소셜 연동", description = "카카오 OAuth 인증 코드를 사용하여 기존 계정에 카카오 소셜 로그인을 연동합니다. state 파라미터에 {environmentKey}|{linkToken} 형식으로 전달해야 합니다.")
    @GetMapping("/api/v1/auth/oauth/kakao/link")
    fun kakaoLink(
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        try {
            val linkState = parseLinkRedirectState(state)

            val userId = socialLinkTokenService.consumeLinkToken(linkState.linkToken)
            socialLinkService.linkSocialAccount(userId, SocialProvider.KAKAO, code, request.requestURL.toString(), state)

            response.sendRedirect(buildSuccessRedirectUrl(state))
        } catch (e: Exception) {
            logger.error("카카오 연동 실패", e)
            response.sendRedirect(oauthRedirectProperties.failureUrl)
        }
    }

    // =========================================================
    // 토큰 갱신
    // =========================================================

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

    // =========================================================
    // Private helpers
    // =========================================================

    private fun buildSuccessRedirectUrl(state: String?): String {
        return buildRedirectUrl(state, oauthRedirectProperties.successUrl)
    }

    private fun buildRedirectUrl(state: String?, configuredUrl: String): String {
        val redirectState = state?.let(::parseRedirectState)
        val environmentBaseUrl = redirectState?.environmentKey
            ?.lowercase()
            ?.let { oauthRedirectProperties.domains[it] }
        val redirectUrl = environmentBaseUrl
            ?.let { appendPath(it, extractPath(configuredUrl)) }
            ?: configuredUrl

        return appendStateQueryParam(redirectUrl, state)
    }

    private fun extractPath(url: String): String {
        return runCatching { URI(url).path }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_SUCCESS_PATH
    }

    private fun appendPath(baseUrl: String, path: String): String {
        val normalizedBase = baseUrl.trimEnd('/')
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return normalizedBase + normalizedPath
    }

    private fun appendStateQueryParam(url: String, state: String?): String {
        val stateValue = state?.takeIf { it.isNotBlank() } ?: return url
        val delimiter = if (url.contains("?")) "&" else "?"
        return "$url$delimiter" + "state=$stateValue"
    }

    private fun parseRedirectState(stateValue: String): RedirectState? {
        val trimmed = stateValue.trim()
        if (trimmed.isEmpty()) {
            return null
        }

        val environmentKey = trimmed.substringBefore('|').trim()
            .takeIf { it.isNotBlank() }
            ?: return null

        return RedirectState(environmentKey = environmentKey)
    }

    private fun parseLinkRedirectState(state: String?): LinkRedirectState {
        val trimmed = state?.trim()
        if (trimmed.isNullOrEmpty()) {
            throw InvalidRequestException("연동 요청에 state가 필요합니다.", "state: null")
        }

        val parts = trimmed.split('|', limit = 2)
        val environmentKey = parts[0].trim().takeIf { it.isNotBlank() }
        val linkToken = parts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
            ?: throw InvalidRequestException("state에 연동 토큰이 누락되었습니다.", "state: $trimmed")

        return LinkRedirectState(environmentKey = environmentKey, linkToken = linkToken)
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
            setAttribute("SameSite", "NONE")
        }
        response.addCookie(accessTokenCookie)

        // HttpOnly 쿠키로 리프레시 토큰 설정
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshTokenValue).apply {
            isHttpOnly = true
            secure = true
            path = "/api/v1/auth/refresh-cookie"
            maxAge = Duration.ofDays(90).seconds.toInt()
            setAttribute("SameSite", "NONE")
        }
        response.addCookie(refreshTokenCookie)
    }

    companion object {
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
        private const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
        private const val DEFAULT_SUCCESS_PATH = "/auth/callback"
    }

    private data class RedirectState(
        val environmentKey: String,
    )

    private data class LinkRedirectState(
        val environmentKey: String?,
        val linkToken: String,
    )
}
