package com.vacuumlabs.wadzpay.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@Primary
@ConfigurationProperties(prefix = "aws")
class JwtConfiguration {

    companion object {
        const val COGNITO_IDENTITY_POOL_URL = "https://cognito-idp.%s.amazonaws.com/%s"
        const val JSON_WEB_TOKEN_SET_URL_SUFFIX = "/.well-known/jwks.json"
    }

    var userPoolId: String = ""
    var region: String = ""
    var accessKeyId: String = ""
    var secretAccessKey: String = ""
    var awsUserPoolsWebClientId: String = ""

    // do we want jwkUrl variable?

    val userNameField = "cognito:username"
    val groupsField = "cognito:groups"
    val emailField = "email"
    val connectionTimeout = 2000
    val readTimeout = 2000
    val httpHeader = "Authorization"

    fun getJwkUrl(): String {
        return String.format("${COGNITO_IDENTITY_POOL_URL}$JSON_WEB_TOKEN_SET_URL_SUFFIX", region, userPoolId)
    }

    fun getCognitoIdentityPoolUrl(): String {
        return String.format(COGNITO_IDENTITY_POOL_URL, region, userPoolId)
    }
}
