package com.nomoney.auth.service

import com.nomoney.auth.domain.User
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.UserRepository
import com.nomoney.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUserInfo(userId: UserId): User {
        return userRepository.findById(userId)
            ?: throw NotFoundException("사용자를 찾을 수 없습니다.", "ID: ${userId.value}")
    }
}
