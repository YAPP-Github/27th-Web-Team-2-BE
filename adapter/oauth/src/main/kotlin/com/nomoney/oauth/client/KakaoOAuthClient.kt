package com.nomoney.oauth.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.nomoney.auth.domain.KakaoOAuthToken
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.port.KakaoOAuthRepository
import com.nomoney.auth.port.SocialOAuthClient
import com.nomoney.oauth.config.KakaoOAuthProperties
import com.nomoney.oauth.dto.KakaoTokenResponse
import com.nomoney.oauth.dto.KakaoUserInfoResponse
import com.nomoney.support.logging.logger
import java.time.LocalDateTime
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class KakaoOAuthClient(
    private val restTemplate: RestTemplate,
    private val properties: KakaoOAuthProperties,
    private val objectMapper: ObjectMapper,
) : SocialOAuthClient, KakaoOAuthRepository {
    private val logger = logger()

    override fun supports(provider: SocialProvider): Boolean =
        provider == SocialProvider.KAKAO

    override fun getAccessToken(authorizationCode: String, state: String?, redirectUri: String): String {
        return getOAuthToken(authorizationCode, state, redirectUri).accessToken
    }

    override fun getOAuthToken(authorizationCode: String, state: String?, redirectUri: String): KakaoOAuthToken {
        val request = buildMap {
            put("code", authorizationCode)
            put("client_id", properties.clientId)
            put("client_secret", properties.clientSecret)
            put("redirect_uri", redirectUri)
            put("grant_type", "authorization_code")
            if (state != null) put("state", state)
        }

        logger.info(objectMapper.writeValueAsString(request))

        val response = requestToken(
            request = request,
            errorMessage = "카카오 액세스 토큰 발급 실패",
        )

        return response.toDomain()
    }

    override fun refreshOAuthToken(refreshToken: String): KakaoOAuthToken {
        val request = buildMap {
            put("refresh_token", refreshToken)
            put("client_id", properties.clientId)
            put("client_secret", properties.clientSecret)
            put("grant_type", "refresh_token")
        }

        logger.info(objectMapper.writeValueAsString(request))

        val response = requestToken(
            request = request,
            errorMessage = "카카오 액세스 토큰 재발급 실패",
        )

        return response.toDomain()
    }

    private fun requestToken(
        request: Map<String, String>,
        errorMessage: String,
    ): KakaoTokenResponse {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
        val body = LinkedMultiValueMap<String, String>().apply {
            request.forEach { (key, value) ->
                add(key, value)
            }
        }

        return try {
            restTemplate.postForObject(TOKEN_URL, HttpEntity(body, headers), KakaoTokenResponse::class.java)
        } catch (e: Exception) {
            throw RuntimeException("$errorMessage: ${e.message}", e)
        }
            ?: throw RuntimeException("$errorMessage: 응답이 없습니다")
    }

    private fun KakaoTokenResponse.toDomain(now: LocalDateTime = LocalDateTime.now()): KakaoOAuthToken {
        return KakaoOAuthToken(
            accessToken = this.accessToken,
            accessTokenExpiresAt = now.plusSeconds(this.expiresIn),
            refreshToken = this.refreshToken,
            refreshTokenExpiresAt = this.refreshTokenExpiresIn?.let { now.plusSeconds(it) },
            scope = this.scope,
        )
    }

    override fun getUserInfo(accessToken: String): SocialUserInfo {
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }

        val response = try {
            restTemplate.exchange(
                USER_INFO_URL,
                HttpMethod.GET,
                HttpEntity<Unit>(headers),
                KakaoUserInfoResponse::class.java,
            ).body
        } catch (e: Exception) {
            throw RuntimeException("카카오 사용자 정보 조회 실패: ${e.message}", e)
        }

        return response?.let {
            SocialUserInfo(
                socialId = it.id.toString(),
                provider = SocialProvider.KAKAO,
                email = it.kakaoAccount?.email,
                name = it.kakaoAccount?.profile?.nickname,
                profileImageUrl = it.kakaoAccount?.profile?.profileImageUrl,
            )
        } ?: throw RuntimeException("카카오 사용자 정보 조회 실패: 응답이 없습니다")
    }

    companion object {
        private const val TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
    }
}
