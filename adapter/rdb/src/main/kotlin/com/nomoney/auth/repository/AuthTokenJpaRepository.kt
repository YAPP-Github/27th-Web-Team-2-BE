package com.nomoney.auth.repository

import com.nomoney.auth.entity.AuthTokenJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AuthTokenJpaRepository : JpaRepository<AuthTokenJpaEntity, Long> {
    fun findByTokenValue(tokenValue: String): AuthTokenJpaEntity?
}
