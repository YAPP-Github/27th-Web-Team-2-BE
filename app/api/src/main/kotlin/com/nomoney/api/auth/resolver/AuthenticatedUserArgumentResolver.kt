package com.nomoney.api.auth.resolver

import com.nomoney.auth.domain.User
import com.nomoney.auth.service.AuthService
import com.nomoney.exception.UnauthorizedException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticatedUserArgumentResolver(
    private val authService: AuthService,
) : HandlerMethodArgumentResolver {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ACCESS_TOKEN_COOKIE_NAME = "access_token"
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == User::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): User {
        // 1. Authorization 헤더에서 토큰 추출 시도
        val authHeader = webRequest.getHeader(AUTHORIZATION_HEADER)
        val token = when {
            authHeader != null -> extractTokenFromHeader(authHeader)
            else -> extractTokenFromCookie(webRequest)
        }

        return authService.validateToken(token)
    }

    private fun extractTokenFromHeader(authHeader: String): String {
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            throw UnauthorizedException("Bearer 토큰 형식이 필요합니다.")
        }

        val token = authHeader.substring(BEARER_PREFIX.length)
        if (token.isBlank()) {
            throw UnauthorizedException("토큰이 비어있습니다.")
        }

        return token
    }

    private fun extractTokenFromCookie(webRequest: NativeWebRequest): String {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw UnauthorizedException("요청 정보를 가져올 수 없습니다.")

        val cookies = request.cookies ?: throw UnauthorizedException("인증 정보가 필요합니다.")

        val tokenCookie = cookies.firstOrNull { it.name == ACCESS_TOKEN_COOKIE_NAME }
            ?: throw UnauthorizedException("액세스 토큰 쿠키가 필요합니다.")

        if (tokenCookie.value.isBlank()) {
            throw UnauthorizedException("액세스 토큰이 비어있습니다.")
        }

        return tokenCookie.value
    }
}
