package com.nomoney.auth.service

import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.port.UserRepository
import org.springframework.stereotype.Service

@Service
class AnonymousAuthService(
    private val userRepository: UserRepository,
    private val authService: AuthService,
) {
    fun loginAnonymously(): TokenPair {
        val userId = userRepository.saveUser(name = null)
        return authService.issueTokenPair(userId)
    }
}
