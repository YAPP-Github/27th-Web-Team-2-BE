package com.nomoney.auth.entity

import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.base.BaseJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserJpaEntity : BaseJpaEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long = 0

    @Column(name = "email")
    var email: String? = null

    @Column(name = "name", length = 100)
    var name: String? = null

    @Column(name = "profile_image_url", length = 512)
    var profileImageUrl: String? = null

    @Column(name = "social_provider", length = 50)
    var socialProvider: String? = null

    @Column(name = "social_id")
    var socialId: String? = null

    companion object {
        fun from(socialUserInfo: SocialUserInfo): UserJpaEntity {
            return UserJpaEntity().apply {
                email = socialUserInfo.email
                name = socialUserInfo.name
                profileImageUrl = socialUserInfo.profileImageUrl
                socialProvider = socialUserInfo.provider.name
                socialId = socialUserInfo.socialId
            }
        }
    }
}
