package com.vacuumlabs.wadzpay.auth

import com.vacuumlabs.wadzpay.merchant.MerchantCredentials
import com.vacuumlabs.wadzpay.merchant.model.MerchantApiKeyRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.SecureRandom
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object encoders {
    val bcrypt = BCryptPasswordEncoder(-1, SecureRandom())
}

class BasicAuthSecurityFilter(private val authenticationManager: AuthenticationManager) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        authenticateApiKey(request)
        filterChain.doFilter(request, response)
    }

    private fun authenticateApiKey(request: HttpServletRequest) {
        val apiKey: String? = request.getHeader("Authorization")
        if (apiKey.isNullOrEmpty() || !apiKey.startsWith("Basic")) {
            return
        }

        try {
            val credentials = MerchantCredentials.fromToken(apiKey)
            val basicAuthenticationToken = MerchantBasicAuthenticationToken(credentials)
            SecurityContextHolder.getContext().authentication = authenticationManager.authenticate(basicAuthenticationToken)
        } catch (ex: Exception) {
            logger.error("ApiKey Authentication exception: ", ex)
        }
    }
}

@Component
class BasicAuthenticationProvider(val merchantApiKeyRepository: MerchantApiKeyRepository) : AuthenticationProvider {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun authenticate(authentication: Authentication?): Authentication {
        val basicAuthenticationToken = authentication as MerchantBasicAuthenticationToken
        val credentials = basicAuthenticationToken.details as MerchantCredentials
        val merchantApiKey = merchantApiKeyRepository.findAll().find { it.apiKeyId() == credentials.apiKeyId }
        if (merchantApiKey == null ||
            !merchantApiKey.valid ||
            !encoders.bcrypt.matches(credentials.apiKeySecret, merchantApiKey.apiKeySecretHash)
        ) {
            logger.error("Basic ApiKey for merchant ${credentials.getMerchantName()} is not valid anymore!")
            throw BadCredentialsException("Invalid Basic API Key")
        }

        basicAuthenticationToken.isAuthenticated = true
        return basicAuthenticationToken
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication == MerchantBasicAuthenticationToken::class.java
    }
}

// TODO we need to enhance the authorities
class MerchantBasicAuthenticationToken(private val apiKey: MerchantCredentials) :
    AbstractAuthenticationToken(arrayListOf(SimpleGrantedAuthority(Role.MERCHANT.toAuthority()))) {

    override fun getCredentials(): Any {
        return this.authorities
    }

    override fun getDetails(): Any {
        return apiKey
    }

    override fun getPrincipal(): String {
        return apiKey.getMerchantName()
    }
}
