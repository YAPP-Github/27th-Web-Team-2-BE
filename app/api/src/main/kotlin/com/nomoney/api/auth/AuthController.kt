package com.nomoney.api.auth

import com.nomoney.api.auth.model.IssueTokenRequest
import com.nomoney.api.auth.model.IssueTokenResponse
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증", description = "인증 관련 API")
@RestController
class AuthController(
    private val authService: AuthService,
) {

    @Operation(summary = "토큰 발급", description = "사용자의 인증 토큰을 발급합니다 (임시 API)")
    @PostMapping("/api/v1/auth/token")
    fun issueToken(
        @RequestBody request: IssueTokenRequest,
    ): IssueTokenResponse {
        val authToken = authService.issueToken(
            userId = UserId(request.userId),
        )

        return IssueTokenResponse(
            token = authToken.tokenValue,
            expiresAt = authToken.expiresAt,
        )
    }
}
