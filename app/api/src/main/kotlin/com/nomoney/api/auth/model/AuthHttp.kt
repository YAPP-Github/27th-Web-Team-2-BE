package com.nomoney.api.auth.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "토큰 발급 요청")
data class IssueTokenRequest(
    @Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
)

@Schema(description = "토큰 발급 응답")
data class IssueTokenResponse(
    @Schema(description = "발급된 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val token: String,

    @Schema(description = "토큰 만료 시간")
    val expiresAt: LocalDateTime,
)
