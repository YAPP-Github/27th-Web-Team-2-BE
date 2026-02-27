package com.nomoney.auth.adapter

import com.nomoney.auth.domain.SocialLinkToken
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.SocialLinkTokenJpaEntity
import com.nomoney.auth.port.SocialLinkTokenRepository
import com.nomoney.auth.repository.SocialLinkTokenJpaRepository
import java.time.LocalDateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SocialLinkTokenAdapter(
    private val socialLinkTokenJpaRepository: SocialLinkTokenJpaRepository,
) : SocialLinkTokenRepository {

    @Transactional
    override fun save(userId: UserId, tokenValue: String, expiresAt: LocalDateTime): SocialLinkToken {
        val entity = SocialLinkTokenJpaEntity.of(
            tokenValue = tokenValue,
            userId = userId.value,
            expiresAt = expiresAt,
        )
        return socialLinkTokenJpaRepository.save(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByTokenValue(tokenValue: String): SocialLinkToken? {
        return socialLinkTokenJpaRepository.findByTokenValue(tokenValue)?.toDomain()
    }

    @Transactional
    override fun markAsUsed(tokenValue: String) {
        socialLinkTokenJpaRepository.markAsUsed(tokenValue)
    }

    private fun SocialLinkTokenJpaEntity.toDomain(): SocialLinkToken {
        return SocialLinkToken(
            tokenValue = this.tokenValue,
            userId = UserId(this.userId),
            expiresAt = this.expiresAt,
            used = this.used,
        )
    }
}
