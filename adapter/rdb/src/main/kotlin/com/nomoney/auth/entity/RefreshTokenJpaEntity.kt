package com.nomoney.auth.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity : BaseJpaEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    var refreshTokenId: Long = 0

    @Column(name = "token_value", length = 64, nullable = false, unique = true)
    lateinit var tokenValue: String

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0

    @Column(name = "expires_at", nullable = false)
    lateinit var expiresAt: LocalDateTime

    @Column(name = "used", nullable = false)
    var used: Boolean = false

    companion object {
        fun of(
            refreshTokenId: Long = 0,
            tokenValue: String,
            userId: Long,
            expiresAt: LocalDateTime,
            used: Boolean = false,
        ): RefreshTokenJpaEntity {
            return RefreshTokenJpaEntity().apply {
                this.refreshTokenId = refreshTokenId
                this.tokenValue = tokenValue
                this.userId = userId
                this.expiresAt = expiresAt
                this.used = used
            }
        }
    }
}
