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
@Table(name = "social_oauth_tokens")
class SocialOAuthTokenJpaEntity : BaseJpaEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_oauth_token_id")
    var socialOAuthTokenId: Long = 0

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0

    @Column(name = "provider", length = 50, nullable = false)
    lateinit var provider: String

    @Column(name = "access_token", length = 2048, nullable = false)
    lateinit var accessToken: String

    @Column(name = "refresh_token", length = 2048)
    var refreshToken: String? = null

    @Column(name = "access_token_expires_at", nullable = false)
    lateinit var accessTokenExpiresAt: LocalDateTime

    @Column(name = "refresh_token_expires_at")
    var refreshTokenExpiresAt: LocalDateTime? = null

    @Column(name = "scope", length = 1024)
    var scope: String? = null

    companion object {
        fun of(
            socialOAuthTokenId: Long = 0,
            userId: Long,
            provider: String,
            accessToken: String,
            refreshToken: String?,
            accessTokenExpiresAt: LocalDateTime,
            refreshTokenExpiresAt: LocalDateTime?,
            scope: String?,
        ): SocialOAuthTokenJpaEntity {
            return SocialOAuthTokenJpaEntity().apply {
                this.socialOAuthTokenId = socialOAuthTokenId
                this.userId = userId
                this.provider = provider
                this.accessToken = accessToken
                this.refreshToken = refreshToken
                this.accessTokenExpiresAt = accessTokenExpiresAt
                this.refreshTokenExpiresAt = refreshTokenExpiresAt
                this.scope = scope
            }
        }
    }
}
