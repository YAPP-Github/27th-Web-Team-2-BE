package com.nomoney.api.auth.model

import com.nomoney.auth.domain.UserId
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class TokenAuthentication(
    private val token: String,
    private val userId: UserId,
    private val authorities: Collection<GrantedAuthority> = emptyList(),
) : Authentication {
    private var isAuthenticated = true

    override fun getName(): String = userId.value.toString()

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getCredentials(): String = token

    override fun getDetails(): Any? = null

    override fun getPrincipal(): UserId = userId

    override fun isAuthenticated(): Boolean = isAuthenticated

    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.isAuthenticated = isAuthenticated
    }
}
