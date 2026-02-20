package com.nomoney.auth.service

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.UserRepository
import org.springframework.stereotype.Service

@Service
class SocialAuthService(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val socialOAuthClientRegistry: SocialOAuthClientRegistry,
) {
    fun loginWithSocialProvider(provider: SocialProvider, authorizationCode: String, state: String?): TokenPair {
        return try {
            // 1. OAuth Client 조회
            val oauthClient = socialOAuthClientRegistry.getClient(provider)

            // 2. Authorization Code → Google Access Token
            val googleAccessToken = oauthClient.getAccessToken(authorizationCode, state)

            // 3. Google Access Token → 사용자 정보
            val socialUserInfo = oauthClient.getUserInfo(googleAccessToken)

            // 4. 회원가입 또는 로그인
            val userId = getOrCreateUser(socialUserInfo)

            // 5. 자체 토큰 발급
            authService.issueTokenPair(userId)
        } catch (e: Exception) {
            if (e is com.nomoney.exception.NoMoneyException) {
                throw e
            }
            throw com.nomoney.exception.SocialAuthException("소셜 로그인 처리 실패: ${e.message}")
        }
    }

    private fun getOrCreateUser(socialUserInfo: SocialUserInfo): UserId {
        // 기존 사용자 조회
        val existingUserId = userRepository.findUserIdBySocialProviderAndSocialId(
            provider = socialUserInfo.provider,
            socialId = socialUserInfo.socialId,
        )

        if (existingUserId != null) {
            return existingUserId
        }

        // 신규 사용자 생성
        return userRepository.save(socialUserInfo)
    }
}
