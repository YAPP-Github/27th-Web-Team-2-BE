package com.nomoney.auth.service

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.port.UserRepository
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class AnonymousAuthService(
    private val userRepository: UserRepository,
    private val authService: AuthService,
) {
    fun loginAnonymously(): TokenPair {
        val anonymousId = UUID.randomUUID().toString()
        val socialUserInfo = SocialUserInfo(
            socialId = anonymousId,
            provider = SocialProvider.ANONYMOUS,
            email = null,
            name = null,
            profileImageUrl = null,
        )
        val userId = userRepository.save(socialUserInfo)
        return authService.issueTokenPair(userId)
    }
}
