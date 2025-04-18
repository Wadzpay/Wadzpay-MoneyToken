package com.vacuumlabs.wadzpay.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration(val swaggerOauthConfiguration: SwaggerOauthConfiguration) {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI().components(
            Components().addSecuritySchemes(
                "jwt_token",
                SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
            ).addSecuritySchemes(
                "basic auth",
                SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
            ).addSecuritySchemes(
                "cognito",
                SecurityScheme().type(SecurityScheme.Type.OAUTH2).scheme("bearer").bearerFormat("JWT").flows(
                    OAuthFlows().authorizationCode(
                        OAuthFlow()
                            .authorizationUrl(swaggerOauthConfiguration.authorizationUrl)
                            .tokenUrl(swaggerOauthConfiguration.tokenUrl)
                            .scopes(Scopes().addString("openid", "openid token"))
                    )
                )
            )
        ).addSecurityItem(SecurityRequirement().addList("basic auth").addList("jwt_token").addList("cognito"))
    }
}

@Configuration
@ConfigurationProperties(prefix = "springdoc.oauth-flow")
data class SwaggerOauthConfiguration(var authorizationUrl: String = "", var tokenUrl: String = "")
