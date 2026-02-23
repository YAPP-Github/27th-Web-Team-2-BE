package com.nomoney.api.auth

import com.nomoney.api.auth.model.TokenAuthentication
import com.nomoney.auth.domain.UserId
import com.nomoney.exception.NoMoneyException
import com.nomoney.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

internal const val TOKEN_VALIDATION_ERROR_ATTR = "TOKEN_VALIDATION_ERROR"

fun getSecurityUserId(): UserId? {
    val authentication = SecurityContextHolder.getContext()?.authentication as? TokenAuthentication
    return authentication?.principal
}

fun getSecurityUserIdOrThrow(): UserId {
    return getSecurityUserId() ?: run {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        val tokenError = request?.getAttribute(TOKEN_VALIDATION_ERROR_ATTR) as? NoMoneyException
        throw tokenError ?: UnauthorizedException()
    }
}
