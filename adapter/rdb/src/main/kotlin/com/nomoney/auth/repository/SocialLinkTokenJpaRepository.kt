package com.nomoney.auth.repository

import com.nomoney.auth.entity.SocialLinkTokenJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SocialLinkTokenJpaRepository : JpaRepository<SocialLinkTokenJpaEntity, Long> {
    fun findByTokenValue(tokenValue: String): SocialLinkTokenJpaEntity?

    @Modifying
    @Query("UPDATE SocialLinkTokenJpaEntity s SET s.used = true WHERE s.tokenValue = :tokenValue")
    fun markAsUsed(tokenValue: String)
}
