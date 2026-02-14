package com.nomoney.auth.adapter

import com.nomoney.auth.domain.RefreshToken
import com.nomoney.auth.domain.RefreshTokenId
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.RefreshTokenJpaEntity
import com.nomoney.auth.port.RefreshTokenRepository
import com.nomoney.auth.repository.RefreshTokenJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RefreshTokenAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {

    @Transactional
    override fun save(refreshToken: RefreshToken): RefreshToken {
        val entity = refreshToken.toEntity()
        val savedEntity = refreshTokenJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByTokenValue(tokenValue: String): RefreshToken? {
        val entity = refreshTokenJpaRepository.findByTokenValue(tokenValue)
            ?: return null
        return entity.toDomain()
    }

    @Transactional
    override fun markAsUsed(tokenValue: String) {
        val entity = refreshTokenJpaRepository.findByTokenValue(tokenValue)
            ?: return
        entity.used = true
        refreshTokenJpaRepository.save(entity)
    }

    private fun RefreshToken.toEntity(): RefreshTokenJpaEntity {
        return RefreshTokenJpaEntity.of(
            refreshTokenId = this.id.value,
            tokenValue = this.tokenValue,
            userId = this.userId.value,
            expiresAt = this.expiresAt,
            used = this.used,
        )
    }

    private fun RefreshTokenJpaEntity.toDomain(): RefreshToken {
        return RefreshToken(
            id = RefreshTokenId(this.refreshTokenId),
            tokenValue = this.tokenValue,
            userId = UserId(this.userId),
            expiresAt = this.expiresAt,
            used = this.used,
            createdAt = this.createdAt,
        )
    }
}
