package com.nomoney.api.auth.resolver

import com.nomoney.auth.domain.User
import com.nomoney.auth.service.AuthService
import com.nomoney.exception.UnauthorizedException
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
        val authHeader = webRequest.getHeader(AUTHORIZATION_HEADER)
            ?: throw UnauthorizedException("Authorization 헤더가 필요합니다.")

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            throw UnauthorizedException("Bearer 토큰 형식이 필요합니다.")
        }

        val token = authHeader.substring(BEARER_PREFIX.length)
        if (token.isBlank()) {
            throw UnauthorizedException("토큰이 비어있습니다.")
        }

        return authService.validateToken(token)
    }
}
