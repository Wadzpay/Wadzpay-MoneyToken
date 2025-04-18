package com.vacuumlabs.wadzpay.configuration

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.jose.util.ResourceRetriever
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.vacuumlabs.wadzpay.cognito.AwsCognitoIdTokenProcessor
import com.vacuumlabs.wadzpay.cognito.AwsCognitoJwtAuthenticationFilter
import com.vacuumlabs.wadzpay.cognito.JwtAuthenticationProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
@ConditionalOnClass(AwsCognitoJwtAuthenticationFilter::class, AwsCognitoIdTokenProcessor::class)
class JwtAutoConfiguration(val jwtConfiguration: JwtConfiguration) {

    @Bean
    fun awsCognitoIdTokenProcessor(): AwsCognitoIdTokenProcessor {
        return AwsCognitoIdTokenProcessor()
    }

    @Bean
    fun jwtAuthenticationProvider(): JwtAuthenticationProvider {
        return JwtAuthenticationProvider()
    }

    @Bean
    fun awsCognitoJwtAuthenticationFilter(): AwsCognitoJwtAuthenticationFilter {
        return AwsCognitoJwtAuthenticationFilter(awsCognitoIdTokenProcessor())
    }

    @Bean
    fun configurableJWTProcessor(): ConfigurableJWTProcessor<SecurityContext> {
        val resourceRetriever: ResourceRetriever = DefaultResourceRetriever(jwtConfiguration.connectionTimeout, jwtConfiguration.readTimeout)
        val jwkSetURL = URL(jwtConfiguration.getJwkUrl())
        val keySource: JWKSource<SecurityContext> = RemoteJWKSet(jwkSetURL, resourceRetriever)
        val jwtProcessor: ConfigurableJWTProcessor<SecurityContext> = DefaultJWTProcessor()
        val keySelector = JWSVerificationKeySelector(JWSAlgorithm.RS256, keySource)
        jwtProcessor.jwsKeySelector = keySelector
        return jwtProcessor
    }
}
