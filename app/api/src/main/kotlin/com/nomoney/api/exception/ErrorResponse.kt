package com.nomoney.api.exception

data class ErrorResponse(
    val code: String,
    val message: String,
    val messageForDev: String? = null,
)
