package com.nomoney.auth.domain

enum class SocialProvider {
    GOOGLE,
    KAKAO,
    NAVER,
    ANONYMOUS,
    ;

    companion object {
        fun from(value: String): SocialProvider {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("지원하지 않는 소셜 로그인 제공자: $value")
        }
    }
}
