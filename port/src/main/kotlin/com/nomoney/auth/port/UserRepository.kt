package com.nomoney.auth.port

import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId

interface UserRepository {
    fun saveUser(name: String?): UserId
    fun existsById(userId: UserId): Boolean
    fun findById(userId: UserId): User?
}
