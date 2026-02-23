package com.nomoney.api.auth

import com.nomoney.api.auth.model.TokenAuthentication
import com.nomoney.auth.service.AuthService
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
            val authHeader = request.getHeader(HEADER_AUTHORIZATION) ?: return
            val headerData = authHeader.split(' ')

            if (headerData.size != 2) return
            if (headerData[0].lowercase() != AUTHORIZATION_METHOD) return
            if (headerData[1].isBlank()) return

            val accessToken = headerData[1]

            val user = authService.validateToken(accessToken) ?: return

            MDC.put("userId", user.id.value.toString())

//            TODO 권한 조회
//            val authorities = authService.getAuthorities(memberId)

            SecurityContextHolder.getContext().authentication = TokenAuthentication(
                accessToken,
                user.id,
                emptyList(), // authorities.map { SimpleGrantedAuthority(it.name) },
            )
        } finally {
            filterChain.doFilter(request, response)
        }
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val AUTHORIZATION_METHOD = "bearer"
    }
}
