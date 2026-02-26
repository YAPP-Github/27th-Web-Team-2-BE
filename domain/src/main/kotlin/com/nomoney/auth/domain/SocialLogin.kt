package com.nomoney.auth.domain

@JvmInline
value class SocialLoginId(val value: Long)

data class SocialLogin(
    val id: SocialLoginId,
    val userId: UserId?,
    val provider: SocialProvider,
    val socialId: String,
)
