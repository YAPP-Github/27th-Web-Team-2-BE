package com.nomoney.auth.adapter

import com.nomoney.auth.domain.AuthToken
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.AuthTokenJpaEntity
import com.nomoney.auth.port.AuthTokenRepository
import com.nomoney.auth.repository.AuthTokenJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuthTokenAdapter(
    private val authTokenJpaRepository: AuthTokenJpaRepository,
) : AuthTokenRepository {

    @Transactional
    override fun save(authToken: AuthToken): AuthToken {
        val entity = authToken.toEntity()
        val savedEntity = authTokenJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByTokenValue(tokenValue: String): AuthToken? {
        val entity = authTokenJpaRepository.findByTokenValue(tokenValue)
            ?: return null
        return entity.toDomain()
    }

    private fun AuthToken.toEntity(): AuthTokenJpaEntity {
        return AuthTokenJpaEntity.of(
            tokenValue = this.tokenValue,
            userId = this.userId.value,
            expiresAt = this.expiresAt,
        )
    }

    private fun AuthTokenJpaEntity.toDomain(): AuthToken {
        return AuthToken(
            tokenValue = this.tokenValue,
            userId = UserId(this.userId),
            expiresAt = this.expiresAt,
            createdAt = this.createdAt,
        )
    }
}
