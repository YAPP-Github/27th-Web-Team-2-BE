package com.nomoney.auth.domain

data class SocialUserInfo(
    val socialId: String,
    val provider: SocialProvider,
    val email: String?,
    val name: String?,
    val profileImageUrl: String?,
)
