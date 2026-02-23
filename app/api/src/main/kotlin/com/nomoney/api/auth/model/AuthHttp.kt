package com.nomoney.api.auth.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "토큰 갱신 요청")
data class RefreshTokenRequest(
    @Schema(description = "리프레시 토큰", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...", required = true)
    val refreshToken: String,
)

@Schema(description = "토큰 갱신 응답")
data class RefreshTokenResponse(
    @Schema(description = "새로 발급된 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,

    @Schema(description = "액세스 토큰 만료 시간")
    val accessTokenExpiresAt: LocalDateTime,

    @Schema(description = "새로 발급된 리프레시 토큰", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
    val refreshToken: String,

    @Schema(description = "리프레시 토큰 만료 시간")
    val refreshTokenExpiresAt: LocalDateTime,
)

@Schema(description = "쿠키 기반 토큰 갱신 응답")
data class RefreshTokenCookieResponse(
    @Schema(description = "토큰 갱신 성공 여부")
    val success: Boolean = true,

    @Schema(description = "메시지")
    val message: String = "토큰이 갱신되었습니다.",
)
