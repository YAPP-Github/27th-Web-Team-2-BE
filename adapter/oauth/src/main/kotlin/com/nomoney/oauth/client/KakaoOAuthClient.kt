package com.nomoney.oauth.client

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo
import com.nomoney.auth.port.SocialOAuthClient
import com.nomoney.oauth.config.KakaoOAuthProperties
import com.nomoney.oauth.dto.KakaoTokenResponse
import com.nomoney.oauth.dto.KakaoUserInfoResponse
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
) : SocialOAuthClient {

    override fun supports(provider: SocialProvider): Boolean =
        provider == SocialProvider.KAKAO

    override fun getAccessToken(authorizationCode: String, state: String?): String {
        val url = "https://kauth.kakao.com/oauth/token"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", properties.clientId)
            add("client_secret", properties.clientSecret)
            add("redirect_uri", properties.redirectUri)
            add("code", authorizationCode)
            if (state != null) add("state", state)
        }

        val response = try {
            restTemplate.postForObject(url, HttpEntity(body, headers), KakaoTokenResponse::class.java)
        } catch (e: Exception) {
            throw RuntimeException("카카오 액세스 토큰 발급 실패: ${e.message}", e)
        }

        return response?.accessToken
            ?: throw RuntimeException("카카오 액세스 토큰 발급 실패: 응답이 없습니다")
    }

    override fun getUserInfo(accessToken: String): SocialUserInfo {
        val url = "https://kapi.kakao.com/v2/user/me"
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }

        val response = try {
            restTemplate.exchange(
                url,
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
}
