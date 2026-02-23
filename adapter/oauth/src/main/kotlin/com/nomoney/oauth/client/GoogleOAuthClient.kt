package com.nomoney.oauth.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.port.SocialOAuthClient
import com.nomoney.oauth.config.GoogleOAuthProperties
import com.nomoney.oauth.dto.GoogleTokenResponse
import com.nomoney.oauth.dto.GoogleUserInfoResponse
import com.nomoney.support.logging.logger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GoogleOAuthClient(
    private val restTemplate: RestTemplate,
    private val properties: GoogleOAuthProperties,
    private val objectMapper: ObjectMapper,
) : SocialOAuthClient {

    private val logger = logger()

    override fun supports(provider: SocialProvider): Boolean =
        provider == SocialProvider.GOOGLE

    override fun getAccessToken(authorizationCode: String, state: String?): String {
        val url = "https://oauth2.googleapis.com/token"
        val request = buildMap {
            put("code", authorizationCode)
            put("client_id", properties.clientId)
            put("client_secret", properties.clientSecret)
            put("redirect_uri", properties.redirectUri)
            put("grant_type", "authorization_code")
            if (state != null) put("state", state)
        }

        logger.info(objectMapper.writeValueAsString(request))

        val response = try {
            restTemplate.postForObject(url, request, GoogleTokenResponse::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Google 액세스 토큰 발급 실패: ${e.message}", e)
        }

        return response?.accessToken
            ?: throw RuntimeException("Google 액세스 토큰 발급 실패: 응답이 없습니다")
    }

    override fun getUserInfo(accessToken: String): SocialUserInfo {
        val url = "https://www.googleapis.com/oauth2/v3/userinfo"
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }

        val response = try {
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<Unit>(headers),
                GoogleUserInfoResponse::class.java,
            ).body
        } catch (e: Exception) {
            throw RuntimeException("Google 사용자 정보 조회 실패: ${e.message}", e)
        }

        return response?.let {
            SocialUserInfo(
                socialId = it.sub,
                provider = SocialProvider.GOOGLE,
                email = it.email,
                name = it.name,
                profileImageUrl = it.picture,
            )
        } ?: throw RuntimeException("Google 사용자 정보 조회 실패: 응답이 없습니다")
    }
}
