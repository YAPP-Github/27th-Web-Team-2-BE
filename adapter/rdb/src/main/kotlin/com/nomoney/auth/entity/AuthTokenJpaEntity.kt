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
@Table(name = "auth_tokens")
class AuthTokenJpaEntity : BaseJpaEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    var tokenId: Long = 0

    @Column(name = "token_value", length = 64, nullable = false, unique = true)
    lateinit var tokenValue: String

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0

    @Column(name = "expires_at", nullable = false)
    lateinit var expiresAt: LocalDateTime

    companion object {
        fun of(
            tokenId: Long,
            tokenValue: String,
            userId: Long,
            expiresAt: LocalDateTime,
        ): AuthTokenJpaEntity {
            return AuthTokenJpaEntity().apply {
                this.tokenId = tokenId
                this.tokenValue = tokenValue
                this.userId = userId
                this.expiresAt = expiresAt
            }
        }
    }
}
