package com.nomoney.api.auth

import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.service.AuthService
import com.nomoney.exception.UnauthorizedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class TokenAuthenticationFilterTest : DescribeSpec({
    val authService = mockk<AuthService>()
    val filter = TokenAuthenticationFilter(authService)

    beforeTest {
        clearMocks(authService)
        SecurityContextHolder.clearContext()
    }

    afterTest {
        SecurityContextHolder.clearContext()
    }

    describe("TokenAuthenticationFilter") {
        describe("문서/진단 경로 우회") {
            it("Swagger api-docs 요청은 access_token 쿠키가 있어도 토큰 검증을 하지 않는다") {
                val request = mockk<HttpServletRequest>(relaxed = true)
                val response = MockHttpServletResponse()
                val filterChain = mockk<MockFilterChain>(relaxed = true)

                every { request.requestURI } returns "/v3/api-docs"
                every { request.contextPath } returns ""
                every { request.cookies } returns arrayOf(Cookie("access_token", "cookie-token"))

                filter.doFilter(request, response, filterChain)

                verify(exactly = 0) { authService.validateToken(any()) }
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }

            it("Prometheus 요청은 Authorization 헤더가 있어도 토큰 검증을 하지 않는다") {
                val request = MockHttpServletRequest("GET", "/actuator/prometheus").apply {
                    addHeader("Authorization", "Bearer header-token")
                }
                val response = MockHttpServletResponse()

                filter.doFilter(request, response, MockFilterChain())

                verify(exactly = 0) { authService.validateToken(any()) }
            }
        }

        describe("비즈니스 API 인증") {
            it("비즈니스 API 요청은 기존처럼 토큰 검증을 수행한다") {
                val request = MockHttpServletRequest("GET", "/api/v1/users/me").apply {
                    addHeader("Authorization", "Bearer header-token")
                }
                val response = MockHttpServletResponse()
                every { authService.validateToken("header-token") } returns User(UserId(1L))

                filter.doFilter(request, response, MockFilterChain())

                verify(exactly = 1) { authService.validateToken("header-token") }
                getSecurityUserId() shouldBe UserId(1L)
            }

            it("유효하지 않은 토큰은 요청 속성에 저장하고 다음 체인으로 전달한다") {
                val request = MockHttpServletRequest("GET", "/api/v1/users/me").apply {
                    addHeader("Authorization", "Bearer invalid-token")
                }
                val response = MockHttpServletResponse()
                val exception = UnauthorizedException("invalid")
                every { authService.validateToken("invalid-token") } throws exception

                filter.doFilter(request, response, MockFilterChain())

                verify(exactly = 1) { authService.validateToken("invalid-token") }
                request.getAttribute(TOKEN_VALIDATION_ERROR_ATTR) shouldBe exception
            }

            it("인프라 예외는 그대로 전파한다") {
                val request = MockHttpServletRequest("GET", "/api/v1/users/me").apply {
                    addHeader("Authorization", "Bearer broken-token")
                }
                val response = MockHttpServletResponse()
                every { authService.validateToken("broken-token") } throws IllegalStateException("db down")

                shouldThrow<IllegalStateException> {
                    filter.doFilter(request, response, MockFilterChain())
                }

                verify(exactly = 1) { authService.validateToken("broken-token") }
            }
        }
    }
},)
