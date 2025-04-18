package com.vacuumlabs.wadzpay.cognito

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AwsCognitoJwtAuthenticationFilter(private val awsCognitoIdTokenProcessor: AwsCognitoIdTokenProcessor) : OncePerRequestFilter() {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        verifyToken(request)
        filterChain.doFilter(request, response)
    }

    private fun verifyToken(request: HttpServletRequest) {

        try {
            val authentication = awsCognitoIdTokenProcessor.getAuthentication(request)

            if (authentication != null) {
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: Exception) {
            logger.info("Error occured while processing Cognito ID token ${ex.message}")
        }
    }
}
