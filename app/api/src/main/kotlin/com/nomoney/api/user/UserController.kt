package com.nomoney.api.user

import com.nomoney.api.auth.getSecurityUserId
import com.nomoney.api.auth.getSecurityUserIdOrThrow
import com.nomoney.api.swagger.SwaggerApiTag
import com.nomoney.api.user.model.UserInfoResponse
import com.nomoney.auth.domain.User
import com.nomoney.auth.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerApiTag.USER, description = "사용자 관련 API")
@RestController
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 ID와 이름을 조회합니다.")
    @GetMapping("/api/v1/users/me")
    fun getMyInfo(): UserInfoResponse {
        val userId = getSecurityUserIdOrThrow()
        val userInfo = userService.getUserInfo(userId)
        return UserInfoResponse(
            id = userInfo.id.value,
            name = userInfo.name,
        )
    }
}
