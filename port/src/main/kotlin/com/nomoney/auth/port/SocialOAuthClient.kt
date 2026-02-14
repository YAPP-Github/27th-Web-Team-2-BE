package com.nomoney.auth.port

import com.nomoney.auth.domain.SocialProvider
import com.nomoney.auth.domain.SocialUserInfo

interface SocialOAuthClient {
    fun supports(provider: SocialProvider): Boolean
    fun getAccessToken(authorizationCode: String): String
    fun getUserInfo(accessToken: String): SocialUserInfo
}
