package com.nomoney.auth.repository

import com.nomoney.auth.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findBySocialProviderAndSocialId(socialProvider: String, socialId: String): UserJpaEntity?
}
