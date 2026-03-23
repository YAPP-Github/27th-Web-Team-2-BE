package com.nomoney.api.auth

import com.nomoney.api.auth.model.TokenAuthentication
import com.nomoney.auth.service.AuthService
import com.nomoney.exception.NoMoneyException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenAuthenticationFilter(
    private val authService: AuthService,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI.removePrefix(request.contextPath)
        return EXCLUDED_PATH_PATTERNS.any { pattern -> pathMatcher.match(pattern, requestPath) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val tokenCredential = resolveTokenCredential(request)
        if (tokenCredential == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val accessToken = tokenCredential.value

            val user = authService.validateToken(accessToken)

            MDC.put("userId", user.id.value.toString())

//            TODO 권한 조회
//            val authorities = authService.getAuthorities(memberId)

            SecurityContextHolder.getContext().authentication = TokenAuthentication(
                accessToken,
                user.id,
                emptyList(), // authorities.map { SimpleGrantedAuthority(it.name) },
            )
        } catch (e: NoMoneyException) {
            authLogger.warn(
                "Token validation failed. path={}, credentialSource={}, exceptionType={}",
                request.requestURI,
                tokenCredential.source,
                e::class.simpleName,
            )
            request.setAttribute(TOKEN_VALIDATION_ERROR_ATTR, e)
        } catch (e: RuntimeException) {
            authLogger.error(
                "Token validation infrastructure error. path={}, credentialSource={}, exceptionType={}",
                request.requestURI,
                tokenCredential.source,
                e::class.simpleName,
                e,
            )
            throw e
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveTokenCredential(request: HttpServletRequest): TokenCredential? {
        val authHeader = request.getHeader(HEADER_AUTHORIZATION)
        if (authHeader != null) {
            val headerData = authHeader.split(' ')
            if (headerData.size == 2 && headerData[0].lowercase() == AUTHORIZATION_METHOD && headerData[1].isNotBlank()) {
                return TokenCredential(headerData[1], CredentialSource.HEADER)
            }
        }

        return request.cookies
            ?.firstOrNull { it.name == COOKIE_ACCESS_TOKEN }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?.let { TokenCredential(it, CredentialSource.COOKIE) }
    }

    private data class TokenCredential(
        val value: String,
        val source: CredentialSource,
    )

    private enum class CredentialSource {
        HEADER,
        COOKIE,
    }

    companion object {
        private val authLogger = LoggerFactory.getLogger(TokenAuthenticationFilter::class.java)
        private val pathMatcher = AntPathMatcher()
        private val EXCLUDED_PATH_PATTERNS = listOf(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",
            "/favicon.ico",
        )
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val AUTHORIZATION_METHOD = "bearer"
        private const val COOKIE_ACCESS_TOKEN = "access_token"
    }
}
