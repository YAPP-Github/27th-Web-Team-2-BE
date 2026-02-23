package com.nomoney.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,

    @JsonProperty("token_type")
    val tokenType: String,
)
