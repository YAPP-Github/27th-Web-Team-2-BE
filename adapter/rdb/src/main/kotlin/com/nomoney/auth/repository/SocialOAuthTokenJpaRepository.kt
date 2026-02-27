package com.nomoney.auth.repository

import com.nomoney.auth.entity.SocialOAuthTokenJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SocialOAuthTokenJpaRepository : JpaRepository<SocialOAuthTokenJpaEntity, Long> {
    fun findByUserIdAndProvider(userId: Long, provider: String): SocialOAuthTokenJpaEntity?
}
