package com.nomoney.auth.domain

@JvmInline
value class UserId(val value: Long)

data class User(
    val id: UserId,
)
