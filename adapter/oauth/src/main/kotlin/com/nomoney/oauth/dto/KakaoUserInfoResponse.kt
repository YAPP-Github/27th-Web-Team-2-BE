package com.nomoney.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    val id: Long,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?,
) {
    data class KakaoAccount(
        val email: String?,
        val profile: Profile?,
    ) {
        data class Profile(
            val nickname: String?,

            @JsonProperty("profile_image_url")
            val profileImageUrl: String?,
        )
    }
}
