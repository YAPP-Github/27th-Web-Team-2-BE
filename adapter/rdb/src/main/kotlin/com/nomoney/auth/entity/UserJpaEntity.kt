package com.nomoney.auth.entity

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

    @Column(name = "name", length = 100)
    var name: String? = null

    companion object {
        fun of(
            name: String? = null,
        ): UserJpaEntity {
            return UserJpaEntity().apply {
                this.name = name
            }
        }
    }
}
