package com.vacuumlabs.wadzpay.utils

import io.swagger.v3.core.util.Json
import org.apache.commons.logging.LogFactory
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import org.springframework.web.util.WebUtils
import java.io.UnsupportedEncodingException
import java.net.InetAddress
import java.nio.charset.Charset
import java.util.Scanner
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.math.min

@Configuration
class LoggableDispatcherServlet(val requestLoggerRepository: RequestLoggerRepository) : DispatcherServlet() {

    private val logger = LogFactory.getLog(javaClass)

    override fun doDispatch(request: HttpServletRequest, response: HttpServletResponse) {
        var request = request
        var response = response
        if (request !is ContentCachingRequestWrapper) {
            request = ContentCachingRequestWrapper(request)
        }

        if (response !is ContentCachingResponseWrapper) {
            response = ContentCachingResponseWrapper(response)
        }

        val handler = getHandler(request)
        try {
            super.doDispatch(request, response)
        } finally {
            log(request, response, handler)
            updateResponse(response)
        }
    }

    fun updateResponse(response: HttpServletResponse) {
        val responseWrapper = WebUtils.getNativeResponse(
            response,
            ContentCachingResponseWrapper::class.java
        )
        responseWrapper!!.copyBodyToResponse()
    }

    private fun log(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        handler: HandlerExecutionChain?
    ) {
        val localHost = InetAddress.getLocalHost()
        if (!localHost.hostName.contains("MacBook", true) && !request.requestURI.equals("/error", true)) {
            // Log
            requestLoggerRepository.save(
                RequestLogger(
                    localHost.hostName, localHost.canonicalHostName, localHost.hostAddress, response.status.toString(), request.localAddr, request.localName, request.locale.toString(),
                    request.localPort.toString(), request.method,
                    request.getHeader("Authorization"), request.requestURI, request.remoteAddr,
                    Json.pretty(request.parameterMap) + Scanner(request.inputStream), getResponsePayload(response).toString()
                )
            )
        }
    }

    fun getResponsePayload(response: HttpServletResponse): String? {
        val wrapper = WebUtils.getNativeResponse(
            response,
            ContentCachingResponseWrapper::class.java
        )
        if (wrapper != null) {
            val buf = wrapper.contentAsByteArray
            if (buf.isNotEmpty()) {
                val length = min(buf.size, 5120)
                try {
                    return String(buf, 0, length, Charset.forName(wrapper.characterEncoding))
                } catch (ex: UnsupportedEncodingException) {
                    // NOOP
                }
            }
        }
        return "[unknown]"
    }

    @Bean
    fun dispatcherRegistration(): ServletRegistrationBean<*>? {
        return ServletRegistrationBean(dispatcherServlet())
    }

    @Bean(name = [DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME])
    fun dispatcherServlet(): DispatcherServlet? {
        return LoggableDispatcherServlet(requestLoggerRepository)
    }
}
