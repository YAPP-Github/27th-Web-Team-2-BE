package com.nomoney.auth.port

import com.nomoney.auth.domain.SocialLogin
import com.nomoney.auth.domain.SocialLoginId
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId

interface SocialLoginRepository {
    fun save(userId: UserId, provider: SocialProvider, socialId: String, email: String? = null, name: String? = null, profileImageUrl: String? = null): SocialLogin
    fun findByProviderAndSocialId(provider: SocialProvider, socialId: String): SocialLogin?
    fun findByUserIdAndProvider(userId: UserId, provider: SocialProvider): SocialLogin?
    fun findAllActiveByUserId(userId: UserId): List<SocialLogin>
    fun softDelete(socialLoginId: SocialLoginId)
    fun reactivate(socialLoginId: SocialLoginId, userId: UserId)
    fun reassignUserId(fromUserId: UserId, toUserId: UserId)
}
