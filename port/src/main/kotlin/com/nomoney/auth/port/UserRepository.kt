package com.nomoney.auth.port

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.domain.UserId

interface UserRepository {
    fun save(socialUserInfo: SocialUserInfo): UserId
    fun existsById(userId: UserId): Boolean
    fun findUserIdBySocialProviderAndSocialId(provider: SocialProvider, socialId: String): UserId?
}
