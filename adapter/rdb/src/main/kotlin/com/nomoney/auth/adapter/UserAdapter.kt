package com.nomoney.auth.adapter

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.UserJpaEntity
import com.nomoney.auth.port.UserRepository
import com.nomoney.auth.repository.UserJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {

    @Transactional
    override fun save(socialUserInfo: SocialUserInfo): UserId {
        val entity = UserJpaEntity.from(socialUserInfo)
        val savedEntity = userJpaRepository.save(entity)
        return UserId(savedEntity.userId)
    }

    @Transactional(readOnly = true)
    override fun existsById(userId: UserId): Boolean {
        return userJpaRepository.existsById(userId.value)
    }

    @Transactional(readOnly = true)
    override fun findUserIdBySocialProviderAndSocialId(provider: SocialProvider, socialId: String): UserId? {
        return userJpaRepository.findBySocialProviderAndSocialId(provider.name, socialId)
            ?.let { UserId(it.userId) }
    }
}
