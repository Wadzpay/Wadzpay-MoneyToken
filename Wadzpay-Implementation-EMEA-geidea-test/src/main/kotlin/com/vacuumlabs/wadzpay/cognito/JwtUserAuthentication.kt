package com.vacuumlabs.wadzpay.cognito

import com.vacuumlabs.wadzpay.auth.Role
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtUserAuthentication(
    private val principal: String,
    val groups: List<String>
) :
    AbstractAuthenticationToken(groupsToGrantedAuthorities(groups)) {

    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): String {
        return principal
    }
}

fun groupsToGrantedAuthorities(groups: List<String>): ArrayList<GrantedAuthority> {
    if (groups.isEmpty()) {
        return arrayListOf(SimpleGrantedAuthority(Role.USER.toAuthority()))
    }

    return groups.map { SimpleGrantedAuthority(Role.valueOf(it).toAuthority()) }.toCollection(ArrayList())
}
