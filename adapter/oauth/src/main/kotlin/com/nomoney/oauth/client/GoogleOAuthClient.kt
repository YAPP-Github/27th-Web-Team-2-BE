package com.nomoney.oauth.client

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.port.SocialOAuthClient
import com.nomoney.oauth.config.GoogleOAuthProperties
import com.nomoney.oauth.dto.GoogleTokenResponse
import com.nomoney.oauth.dto.GoogleUserInfoResponse
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GoogleOAuthClient(
    private val restTemplate: RestTemplate,
    private val properties: GoogleOAuthProperties,
) : SocialOAuthClient {

    override fun supports(provider: SocialProvider): Boolean =
        provider == SocialProvider.GOOGLE

    override fun getAccessToken(authorizationCode: String): String {
        val url = "https://oauth2.googleapis.com/token"
        val request = mapOf(
            "code" to authorizationCode,
            "client_id" to properties.clientId,
            "client_secret" to properties.clientSecret,
            "redirect_uri" to properties.redirectUri,
            "grant_type" to "authorization_code",
        )

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
