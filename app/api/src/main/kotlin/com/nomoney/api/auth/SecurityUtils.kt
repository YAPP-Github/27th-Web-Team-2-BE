package com.nomoney.api.auth

import com.nomoney.api.auth.model.TokenAuthentication
import com.nomoney.auth.domain.UserId
import com.nomoney.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

fun getSecurityMemberId(): UserId? {
    val authentication = SecurityContextHolder.getContext()?.authentication as? TokenAuthentication
    return authentication?.principal
}

fun getSecurityMemberIdOrThrow(): UserId {
    return getSecurityMemberId() ?: throw UnauthorizedException()
}
