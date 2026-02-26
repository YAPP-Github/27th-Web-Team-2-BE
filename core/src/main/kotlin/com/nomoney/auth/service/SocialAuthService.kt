package com.nomoney.auth.service

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.SocialLoginRepository
import com.nomoney.auth.port.UserRepository
import com.nomoney.exception.NoMoneyException
import com.nomoney.exception.SocialAuthException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialAuthService(
    private val userRepository: UserRepository,
    private val socialLoginRepository: SocialLoginRepository,
    private val authService: AuthService,
    private val socialOAuthClientRegistry: SocialOAuthClientRegistry,
) {
    fun loginWithSocialProvider(provider: SocialProvider, authorizationCode: String, state: String?): TokenPair {
        return try {
            val oauthClient = socialOAuthClientRegistry.getClient(provider)
            val accessToken = oauthClient.getAccessToken(authorizationCode, state)
            val socialUserInfo = oauthClient.getUserInfo(accessToken)

            val userId = getOrCreateUser(socialUserInfo)

            authService.issueTokenPair(userId)
        } catch (e: NoMoneyException) {
            throw e
        } catch (e: Exception) {
            throw SocialAuthException("소셜 로그인 처리 실패: ${e.message}", e)
        }
    }

    @Transactional
    fun getOrCreateUser(socialUserInfo: SocialUserInfo): UserId {
        val existingSocialLogin = socialLoginRepository.findByProviderAndSocialId(
            provider = socialUserInfo.provider,
            socialId = socialUserInfo.socialId,
        )

        val existingUserId = existingSocialLogin?.userId
        if (existingUserId != null) {
            return existingUserId
        }

        val userId = userRepository.saveUser(name = socialUserInfo.name)

        socialLoginRepository.save(
            userId = userId,
            provider = socialUserInfo.provider,
            socialId = socialUserInfo.socialId,
            email = socialUserInfo.email,
            name = socialUserInfo.name,
            profileImageUrl = socialUserInfo.profileImageUrl,
        )

        return userId
    }
}
