package com.nomoney.api.auth

import com.nomoney.api.auth.model.IssueTokenRequest
import com.nomoney.api.auth.model.IssueTokenResponse
import com.nomoney.api.auth.model.RefreshTokenRequest
import com.nomoney.api.auth.model.RefreshTokenResponse
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증", description = "인증 관련 API")
@RestController
class AuthController(
    private val authService: AuthService,
) {

    @Operation(summary = "토큰 발급", description = "사용자의 액세스 토큰과 리프레시 토큰을 발급합니다 (임시 API)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
        ],
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

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰"),
        ],
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
}
