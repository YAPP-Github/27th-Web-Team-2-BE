package com.nomoney.oauth.dto

data class GoogleUserInfoResponse(
    val sub: String,
    val email: String?,
    val name: String?,
    val picture: String?,
)
