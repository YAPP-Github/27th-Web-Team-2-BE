package com.nomoney.auth.adapter

import com.nomoney.auth.domain.SocialLogin
import com.nomoney.auth.domain.SocialLoginId
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.SocialLoginJpaEntity
import com.nomoney.auth.port.SocialLoginRepository
import com.nomoney.auth.repository.SocialLoginJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SocialLoginAdapter(
    private val socialLoginJpaRepository: SocialLoginJpaRepository,
) : SocialLoginRepository {

    @Transactional
    override fun save(userId: UserId, provider: SocialProvider, socialId: String, email: String?, name: String?, profileImageUrl: String?): SocialLogin {
        val entity = SocialLoginJpaEntity.of(
            userId = userId.value,
            socialProvider = provider.name,
            socialId = socialId,
            email = email,
            name = name,
            profileImageUrl = profileImageUrl,
        )
        return socialLoginJpaRepository.save(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByProviderAndSocialId(provider: SocialProvider, socialId: String): SocialLogin? {
        return socialLoginJpaRepository.findBySocialProviderAndSocialId(provider.name, socialId)
            ?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByUserIdAndProvider(userId: UserId, provider: SocialProvider): SocialLogin? {
        return socialLoginJpaRepository.findByUserIdAndSocialProvider(userId.value, provider.name)
            ?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAllActiveByUserId(userId: UserId): List<SocialLogin> {
        return socialLoginJpaRepository.findAllByUserIdAndUserIdIsNotNull(userId.value)
            .map { it.toDomain() }
    }

    @Transactional
    override fun softDelete(socialLoginId: SocialLoginId) {
        val entity = socialLoginJpaRepository.findById(socialLoginId.value).orElse(null) ?: return
        entity.userId = null
        socialLoginJpaRepository.save(entity)
    }

    @Transactional
    override fun reactivate(socialLoginId: SocialLoginId, userId: UserId) {
        val entity = socialLoginJpaRepository.findById(socialLoginId.value).orElse(null) ?: return
        entity.userId = userId.value
        socialLoginJpaRepository.save(entity)
    }

    @Transactional
    override fun reassignUserId(fromUserId: UserId, toUserId: UserId) {
        socialLoginJpaRepository.updateUserIdByUserId(fromUserId.value, toUserId.value)
    }

    private fun SocialLoginJpaEntity.toDomain(): SocialLogin {
        return SocialLogin(
            id = SocialLoginId(this.socialLoginId),
            userId = this.userId?.let(::UserId),
            provider = SocialProvider.from(this.socialProvider),
            socialId = this.socialId,
        )
    }
}
