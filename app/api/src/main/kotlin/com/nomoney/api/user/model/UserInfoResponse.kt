package com.nomoney.api.user.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 정보 응답")
data class UserInfoResponse(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,

    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String?,
)
