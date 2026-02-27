package com.nomoney.auth.adapter

import com.nomoney.auth.domain.SocialOAuthToken
import com.nomoney.auth.domain.SocialOAuthTokenId
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.SocialOAuthTokenJpaEntity
import com.nomoney.auth.port.SocialOAuthTokenRepository
import com.nomoney.auth.repository.SocialOAuthTokenJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SocialOAuthTokenAdapter(
    private val socialOAuthTokenJpaRepository: SocialOAuthTokenJpaRepository,
) : SocialOAuthTokenRepository {

    @Transactional(readOnly = true)
    override fun findByUserIdAndProvider(userId: UserId, provider: SocialProvider): SocialOAuthToken? {
        return socialOAuthTokenJpaRepository.findByUserIdAndProvider(userId.value, provider.name)
            ?.toDomain()
    }

    @Transactional
    override fun upsert(token: SocialOAuthToken): SocialOAuthToken {
        val existing = socialOAuthTokenJpaRepository.findByUserIdAndProvider(
            userId = token.userId.value,
            provider = token.provider.name,
        )

        val entity = existing?.apply {
            this.accessToken = token.accessToken
            this.refreshToken = token.refreshToken
            this.accessTokenExpiresAt = token.accessTokenExpiresAt
            this.refreshTokenExpiresAt = token.refreshTokenExpiresAt
            this.scope = token.scope
        } ?: token.toEntity()

        return socialOAuthTokenJpaRepository.save(entity).toDomain()
    }

    private fun SocialOAuthToken.toEntity(): SocialOAuthTokenJpaEntity {
        return SocialOAuthTokenJpaEntity.of(
            socialOAuthTokenId = this.id.value,
            userId = this.userId.value,
            provider = this.provider.name,
            accessToken = this.accessToken,
            refreshToken = this.refreshToken,
            accessTokenExpiresAt = this.accessTokenExpiresAt,
            refreshTokenExpiresAt = this.refreshTokenExpiresAt,
            scope = this.scope,
        )
    }

    private fun SocialOAuthTokenJpaEntity.toDomain(): SocialOAuthToken {
        return SocialOAuthToken(
            id = SocialOAuthTokenId(this.socialOAuthTokenId),
            userId = UserId(this.userId),
            provider = SocialProvider.from(this.provider),
            accessToken = this.accessToken,
            refreshToken = this.refreshToken,
            accessTokenExpiresAt = this.accessTokenExpiresAt,
            refreshTokenExpiresAt = this.refreshTokenExpiresAt,
            scope = this.scope,
            createdAt = this.createdAt,
        )
    }
}
