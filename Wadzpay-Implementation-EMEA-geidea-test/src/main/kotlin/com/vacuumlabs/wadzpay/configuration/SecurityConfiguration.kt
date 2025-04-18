package com.vacuumlabs.wadzpay.configuration

import com.vacuumlabs.API_VERSION
import com.vacuumlabs.wadzpay.auth.BasicAuthSecurityFilter
import com.vacuumlabs.wadzpay.auth.BasicAuthenticationProvider
import com.vacuumlabs.wadzpay.auth.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
    val apiKeyAuthenticationProvider: BasicAuthenticationProvider,
    val jwtAutoConfiguration: JwtAutoConfiguration,
    val appConfig: AppConfig
) : WebSecurityConfigurerAdapter() {
    /*override fun configure(web: WebSecurity) {
        val firewall = StrictHttpFirewall()
        firewall.setAllowedHostnames(Predicate { hostname: String -> (hostname.contains("wadzpay.com") || hostname.contains("abc")) })
        web
            .httpFirewall(firewall)
    }*/

    override fun configure(http: HttpSecurity) {

        http {
            cors { }
            csrf { disable() }
            authorizeRequests {
                // Whitelist endpoints needed for registration
                authorize(HttpMethod.POST, "/user/registration/**", permitAll)
                authorize(HttpMethod.POST, "/webhook/**", permitAll)
                authorize(HttpMethod.GET, "/webhook/**", permitAll)
                authorize(HttpMethod.POST, "/v1/merchant", permitAll) // TODO this should be available only to admin
                authorize(HttpMethod.POST, "/ramp/onrampOrder", permitAll)
                authorize(HttpMethod.GET, "/v1/config/cryptos", permitAll)
                if (!appConfig.production) {
                    authorize("/v1/merchant/test-webhook", permitAll)
                }
                authorize(HttpMethod.GET, "/merchantDashboard/invite", permitAll)

                // Whitelist public endpoints
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/actuator/**", permitAll)

                authorize("/v1/merchant/**", hasAnyRole(Role.MERCHANT.name))
                authorize("/user/**", hasAnyRole(Role.USER.name, Role.MERCHANT_ADMIN.name, Role.MERCHANT_READER.name))

                authorize("/merchantDashboard/admin/**", hasAnyRole(Role.MERCHANT_ADMIN.name))
                authorize("/merchantDashboard/**", hasAnyRole(Role.MERCHANT_ADMIN.name, Role.MERCHANT_READER.name, Role.MERCHANT_POSOPERATOR.name, Role.MERCHANT_SUPERVISOR.name, Role.MERCHANT_MERCHANT.name))
                authorize("/merchantDashboard/admin/**", hasAnyRole(Role.MERCHANT_ADMIN.name))

                authorize("/merchant/admin/**", hasAnyRole(Role.MERCHANT_ADMIN.name))
                authorize("/merchant/**", hasAnyRole(Role.MERCHANT_ADMIN.name, Role.MERCHANT_READER.name, Role.MERCHANT_POSOPERATOR.name, Role.MERCHANT_SUPERVISOR.name, Role.MERCHANT_MERCHANT.name))
                authorize("/merchant/admin/**", hasAnyRole(Role.MERCHANT_ADMIN.name))

                // These exist only in dev and test

                if (!appConfig.environment.equals("prod", true)) {
                    authorize("/writeReports", permitAll)
                    authorize("/addFakeUserData", permitAll)
                    authorize("/removeFakeUserData", permitAll)
                    authorize("/fakeApis", permitAll)
                }

                // POS endpoints
                authorize(
                    "/pos/**",
                    hasAnyRole(Role.MERCHANT_ADMIN.name, Role.MERCHANT_READER.name, Role.MERCHANT_POSOPERATOR.name, Role.MERCHANT_MERCHANT.name, Role.MERCHANT_SUPERVISOR.name)
                )

                authorize("/api/$API_VERSION/**", permitAll)

                // All other requests must be authenticated by default
                authorize(anyRequest, authenticated)
            }
            addFilterBefore(
                jwtAutoConfiguration.awsCognitoJwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            )
            addFilterBefore(
                BasicAuthSecurityFilter(authenticationManager()),
                UsernamePasswordAuthenticationFilter::class.java
            )
        }
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(apiKeyAuthenticationProvider)
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer? {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedHeaders("*")
                    .allowedMethods("*")
                    .allowedOrigins("*")
            }
        }
    }
}
