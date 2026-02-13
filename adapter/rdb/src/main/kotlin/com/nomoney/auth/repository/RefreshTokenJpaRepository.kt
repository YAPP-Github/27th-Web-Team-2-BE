package com.nomoney.auth.repository

import com.nomoney.auth.entity.RefreshTokenJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, Long> {
    fun findByTokenValue(tokenValue: String): RefreshTokenJpaEntity?
}
