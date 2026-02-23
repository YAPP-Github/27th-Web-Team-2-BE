package com.nomoney.api.auth

import com.nomoney.api.auth.model.TokenAuthentication
import com.nomoney.auth.service.AuthService
import com.nomoney.exception.NoMoneyException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenAuthenticationFilter(
    private val authService: AuthService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val accessToken = resolveToken(request) ?: return

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
            request.setAttribute(TOKEN_VALIDATION_ERROR_ATTR, e)
        } finally {
            filterChain.doFilter(request, response)
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HEADER_AUTHORIZATION)
        if (authHeader != null) {
            val headerData = authHeader.split(' ')
            if (headerData.size == 2 && headerData[0].lowercase() == AUTHORIZATION_METHOD && headerData[1].isNotBlank()) {
                return headerData[1]
            }
        }

        return request.cookies
            ?.firstOrNull { it.name == COOKIE_ACCESS_TOKEN }
            ?.value
            ?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val AUTHORIZATION_METHOD = "bearer"
        private const val COOKIE_ACCESS_TOKEN = "access_token"
    }
}
