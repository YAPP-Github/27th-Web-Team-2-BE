package com.nomoney.auth.repository

import com.nomoney.auth.entity.SocialLoginJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SocialLoginJpaRepository : JpaRepository<SocialLoginJpaEntity, Long> {

    fun findBySocialProviderAndSocialId(socialProvider: String, socialId: String): SocialLoginJpaEntity?

    fun findByUserIdAndSocialProvider(userId: Long, socialProvider: String): SocialLoginJpaEntity?

    fun findAllByUserIdAndUserIdIsNotNull(userId: Long): List<SocialLoginJpaEntity>

    @Modifying
    @Query("UPDATE SocialLoginJpaEntity s SET s.userId = :toUserId WHERE s.userId = :fromUserId")
    fun updateUserIdByUserId(
        @Param("fromUserId") fromUserId: Long,
        @Param("toUserId") toUserId: Long,
    )
}
