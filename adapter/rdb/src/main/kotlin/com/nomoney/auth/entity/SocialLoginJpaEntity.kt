package com.nomoney.auth.entity

import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "social_logins")
class SocialLoginJpaEntity : BaseJpaEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_login_id")
    var socialLoginId: Long = 0

    @Column(name = "user_id", nullable = true)
    var userId: Long? = null

    @Column(name = "social_provider", length = 50, nullable = false)
    lateinit var socialProvider: String

    @Column(name = "social_id", nullable = false)
    lateinit var socialId: String

    @Column(name = "email")
    var email: String? = null

    @Column(name = "name", length = 100)
    var name: String? = null

    @Column(name = "profile_image_url", length = 512)
    var profileImageUrl: String? = null

    companion object {
        fun of(
            userId: Long?,
            socialProvider: String,
            socialId: String,
            email: String? = null,
            name: String? = null,
            profileImageUrl: String? = null,
        ): SocialLoginJpaEntity {
            return SocialLoginJpaEntity().apply {
                this.userId = userId
                this.socialProvider = socialProvider
                this.socialId = socialId
                this.email = email
                this.name = name
                this.profileImageUrl = profileImageUrl
            }
        }
    }
}
