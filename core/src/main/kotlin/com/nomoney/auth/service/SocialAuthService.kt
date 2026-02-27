package com.nomoney.auth.service

import com.nomoney.auth.domain.KakaoOAuthToken
import com.nomoney.auth.domain.SocialOAuthToken
import com.nomoney.auth.domain.SocialOAuthTokenId
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.domain.TokenPair
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.KakaoOAuthRepository
import com.nomoney.auth.port.SocialLoginRepository
import com.nomoney.auth.port.SocialOAuthTokenRepository
import com.nomoney.auth.port.UserRepository
import com.nomoney.exception.NoMoneyException
import com.nomoney.exception.SocialAuthException
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialAuthService(
    private val userRepository: UserRepository,
    private val socialLoginRepository: SocialLoginRepository,
    private val socialOAuthTokenRepository: SocialOAuthTokenRepository,
    private val kakaoOAuthRepository: KakaoOAuthRepository,
    private val authService: AuthService,
    private val socialOAuthClientRegistry: SocialOAuthClientRegistry,
) {
    fun loginWithSocialProvider(provider: SocialProvider, authorizationCode: String, state: String?, redirectUri: String): TokenPair {
        return try {
            when (provider) {
                SocialProvider.KAKAO -> loginWithKakao(authorizationCode, state, redirectUri)
                else -> loginWithDefaultProvider(provider, authorizationCode, state, redirectUri)
            }
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

    private fun loginWithDefaultProvider(
        provider: SocialProvider,
        authorizationCode: String,
        state: String?,
        redirectUri: String,
    ): TokenPair {
        val oauthClient = socialOAuthClientRegistry.getClient(provider)
        val accessToken = oauthClient.getAccessToken(authorizationCode, state, redirectUri)
        val socialUserInfo = oauthClient.getUserInfo(accessToken)
        val userId = getOrCreateUser(socialUserInfo)

        return authService.issueTokenPair(userId)
    }

    private fun loginWithKakao(
        authorizationCode: String,
        state: String?,
        redirectUri: String,
    ): TokenPair {
        val kakaoToken = kakaoOAuthRepository.getOAuthToken(authorizationCode, state, redirectUri)
        val oauthClient = socialOAuthClientRegistry.getClient(SocialProvider.KAKAO)
        val socialUserInfo = oauthClient.getUserInfo(kakaoToken.accessToken)
        val userId = getOrCreateUser(socialUserInfo)

        saveKakaoOAuthToken(userId, kakaoToken)

        return authService.issueTokenPair(userId)
    }

    private fun saveKakaoOAuthToken(userId: UserId, kakaoToken: KakaoOAuthToken) {
        val existingToken = socialOAuthTokenRepository.findByUserIdAndProvider(userId, SocialProvider.KAKAO)

        val token = SocialOAuthToken(
            id = existingToken?.id ?: SocialOAuthTokenId(0L),
            userId = userId,
            provider = SocialProvider.KAKAO,
            accessToken = kakaoToken.accessToken,
            refreshToken = kakaoToken.refreshToken ?: existingToken?.refreshToken,
            accessTokenExpiresAt = kakaoToken.accessTokenExpiresAt,
            refreshTokenExpiresAt = kakaoToken.refreshTokenExpiresAt ?: existingToken?.refreshTokenExpiresAt,
            scope = kakaoToken.scope ?: existingToken?.scope,
            createdAt = existingToken?.createdAt ?: LocalDateTime.now(),
        )

        socialOAuthTokenRepository.upsert(token)
    }
}
