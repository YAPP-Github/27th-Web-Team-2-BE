package com.nomoney.auth.adapter

import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.entity.UserJpaEntity
import com.nomoney.auth.port.UserRepository
import com.nomoney.auth.repository.UserJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {

    @Transactional
    override fun saveUser(name: String?): UserId {
        val entity = UserJpaEntity.of(name = name)
        val savedEntity = userJpaRepository.save(entity)
        return UserId(savedEntity.userId)
    }

    @Transactional(readOnly = true)
    override fun existsById(userId: UserId): Boolean {
        return userJpaRepository.existsById(userId.value)
    }

    @Transactional(readOnly = true)
    override fun findById(userId: UserId): User? {
        return userJpaRepository.findById(userId.value)
            .orElse(null)
            ?.let { User(id = UserId(it.userId), name = it.name) }
    }
}
