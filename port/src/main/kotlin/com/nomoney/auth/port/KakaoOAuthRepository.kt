package com.nomoney.auth.port

import com.nomoney.auth.domain.KakaoOAuthToken

interface KakaoOAuthRepository {
    fun getOAuthToken(authorizationCode: String, state: String?, redirectUri: String): KakaoOAuthToken
    fun refreshOAuthToken(refreshToken: String): KakaoOAuthToken
}
