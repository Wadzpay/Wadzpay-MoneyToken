package com.vacuumlabs.wadzpay.cognito

import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.vacuumlabs.wadzpay.configuration.JwtConfiguration
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.utils.EncryptService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Component
class AwsCognitoIdTokenProcessor {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val BEARER_PREFIX = "Bearer "
    }

    @Autowired
    private lateinit var jwtConfiguration: JwtConfiguration
    @Autowired
    private lateinit var userAccountService: UserAccountService
    @Autowired
    private lateinit var encryptService: EncryptService
    @Autowired
    private lateinit var httpSession: HttpSession

    @Autowired
    private lateinit var configurableJWTProcessor: ConfigurableJWTProcessor<SecurityContext>

    fun getAuthentication(request: HttpServletRequest): Authentication? {
        val idToken = request.getHeader(jwtConfiguration.httpHeader)
        if (idToken != null && idToken.startsWith(BEARER_PREFIX)) {
            val bearerToken = stripBearerToken(idToken)
            // encryptService.getDecodedString(bearerToken,)
            val claimsSet = configurableJWTProcessor.process(bearerToken, null)

            if (!isIssuedCorrectly(claimsSet)) {
                throw Exception(String.format("Issuer %s in JWT token doesn't match cognito idp %s", claimsSet.issuer, jwtConfiguration.getCognitoIdentityPoolUrl()))
            }

            if (!isIdToken(claimsSet)) {
                throw Exception("JWT Token doesn't seem to be an ID Token")
            }

            val userEmail = claimsSet.claims[jwtConfiguration.emailField].toString()
            // val user=userAccountService.findByEmail(userEmail)
            @Suppress("UNCHECKED_CAST")
            val groups = claimsSet.claims[jwtConfiguration.groupsField] as? List<String>

            val authentication = JwtUserAuthentication(userEmail, groups ?: listOf())
            var userInSession: String = ""
            var sessionUser = httpSession.getAttribute("currentUser")
            if (sessionUser != null) {
                userInSession = sessionUser.toString()
            }
/*
            if(userInSession.isNotEmpty()&&encryptService.isDecodedStringMatched(userInSession, userEmail!!)) {
*/
            authentication.isAuthenticated = true
                /*}
                else{
                authentication.isAuthenticated=false
                println("something went wrong")
                throw Exception("JWT Token doesn't seem to be valid for the user")

            }*/
            return authentication
        }

        return null
    }

    fun stripBearerToken(token: String): String {
        return token.substring(BEARER_PREFIX.length)
    }

    fun isIssuedCorrectly(claimsSet: JWTClaimsSet): Boolean {
        return claimsSet.issuer.equals(jwtConfiguration.getCognitoIdentityPoolUrl())
    }

    fun isIdToken(claimsSet: JWTClaimsSet): Boolean {
        return claimsSet.getClaim("token_use").equals("id")
    }
}
