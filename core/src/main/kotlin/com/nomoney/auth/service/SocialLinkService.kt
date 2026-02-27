package com.nomoney.auth.service

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.UserId
import com.nomoney.auth.port.SocialLoginRepository
import com.nomoney.exception.DuplicateContentException
import com.nomoney.exception.NotFoundException
import com.nomoney.meeting.port.MeetingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SocialLinkService(
    private val socialLoginRepository: SocialLoginRepository,
    private val meetingRepository: MeetingRepository,
    private val socialOAuthClientRegistry: SocialOAuthClientRegistry,
) {
    @Transactional
    fun linkSocialAccount(currentUserId: UserId, provider: SocialProvider, authorizationCode: String, redirectUri: String, state: String?) {
        val oauthClient = socialOAuthClientRegistry.getClient(provider)
        val accessToken = oauthClient.getAccessToken(authorizationCode, state, redirectUri)
        val socialUserInfo = oauthClient.getUserInfo(accessToken)

        val existingSocialLogin = socialLoginRepository.findByProviderAndSocialId(
            provider = socialUserInfo.provider,
            socialId = socialUserInfo.socialId,
        )

        when {
            existingSocialLogin == null -> {
                // 새로운 소셜 계정 연동
                socialLoginRepository.save(
                    userId = currentUserId,
                    provider = provider,
                    socialId = socialUserInfo.socialId,
                    email = socialUserInfo.email,
                    name = socialUserInfo.name,
                    profileImageUrl = socialUserInfo.profileImageUrl,
                )
            }
            existingSocialLogin.userId == currentUserId -> {
                // 이미 현재 유저에게 연동된 계정
                throw DuplicateContentException(
                    "이미 연동된 소셜 계정입니다.",
                    "provider: $provider, userId: ${currentUserId.value}",
                )
            }
            existingSocialLogin.userId != null -> {
                // 다른 유저에게 연동된 계정 → 계정 통합
                val otherUserId = existingSocialLogin.userId!!
                socialLoginRepository.reassignUserId(otherUserId, currentUserId)
                meetingRepository.reassignHostUserId(otherUserId, currentUserId)
            }
            else -> {
                // 소프트 삭제된 레코드 → 현재 유저로 재활성화
                socialLoginRepository.reactivate(existingSocialLogin.id, currentUserId)
            }
        }
    }

    @Transactional
    fun unlinkSocialAccount(currentUserId: UserId, provider: SocialProvider) {
        val socialLogin = socialLoginRepository.findByUserIdAndProvider(currentUserId, provider)
            ?: throw NotFoundException(
                "연동된 소셜 계정을 찾을 수 없습니다.",
                "provider: $provider, userId: ${currentUserId.value}",
            )

        socialLoginRepository.softDelete(socialLogin.id)
    }

    @Transactional(readOnly = true)
    fun getLinkedSocialAccounts(userId: UserId): List<SocialProvider> {
        return socialLoginRepository.findAllActiveByUserId(userId)
            .map { it.provider }
    }
}
