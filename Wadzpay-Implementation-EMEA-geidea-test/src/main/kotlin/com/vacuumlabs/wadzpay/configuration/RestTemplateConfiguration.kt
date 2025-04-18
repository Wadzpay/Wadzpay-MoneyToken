package com.vacuumlabs.wadzpay.configuration

import com.vacuumlabs.wadzpay.comply.ComplyAdvantageConfiguration
import com.vacuumlabs.wadzpay.gap600.Gap600Configuration
import com.vacuumlabs.wadzpay.kyc.JUMIOConfiguration
import com.vacuumlabs.wadzpay.paymentPoc.VePayConfiguration
import org.apache.http.client.HttpClient
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.ssl.SSLContexts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.retry.annotation.EnableRetry
import org.springframework.web.client.RestTemplate
import java.io.FileInputStream
import java.security.KeyStore
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@EnableRetry
@Configuration
class RestTemplateConfiguration(
    val sslConfiguration: SslConfiguration,
    val webhooksIOConfiguration: WebhooksIOConfiguration,
    val seonConfiguration: SeonConfiguration,
    val bitGoConfiguration: BitGoConfiguration,
    val jumioConfiguration: JUMIOConfiguration,
    val complyAdvantageConfiguration: ComplyAdvantageConfiguration,
    val vePayConfiguration: VePayConfiguration,
    val gap600Configuration: Gap600Configuration,
    val algoCustomTokenConfiguration: AlgoCustomTokenConfiguration
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @Qualifier("Seon")
    fun seonRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor("X-Api-Key", seonConfiguration.apiKey, "Content-Type", "application/json")
        )
    }

    @Bean
    @Qualifier("BitGo")
    fun bitGoRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor(HttpHeaders.AUTHORIZATION, "Bearer ${bitGoConfiguration.token}")
        )
    }

    @Bean
    @Qualifier("JUMIO")
    fun jumioRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor(
                HttpHeaders.AUTHORIZATION,
                "Basic " + HttpHeaders.encodeBasicAuth(
                    jumioConfiguration.apiToken, jumioConfiguration.secret,
                    null
                ),
                HttpHeaders.USER_AGENT, "wadzpay-test"
            )
        )
    }

    @Bean
    @Qualifier("COMPLYADVANTAGE")
    fun complyAdvantageRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor(HttpHeaders.AUTHORIZATION, "Token ${complyAdvantageConfiguration.api_key}")
        )
    }

    @Bean
    @Qualifier("VEPAY")
    fun vePayRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor(
                "mid", vePayConfiguration.merchant_ID,
                "password", vePayConfiguration.merchant_key
            )
        )
    }

    @Bean
    @Qualifier("WebhooksIO")
    fun webhooksIORestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor(HttpHeaders.AUTHORIZATION, "Bearer ${webhooksIOConfiguration.apiToken}")
        )
    }

    private fun headerModifiedRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        interceptor: HeaderModifierInterceptor
    ): RestTemplate {
        val requestFactory =
            getSslNoopRequestFactory() // TODO change this to getRequestFactory() before going to production
        return restTemplateBuilder
            .requestFactory { requestFactory }
            .interceptors(interceptor)
            .build()
    }

    private fun algoHeaderModifiedRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        interceptor: HeaderModifierInterceptor
    ): RestTemplate {
        val requestFactory =
            algoGetSslNoopRequestFactory() // TODO change this to getRequestFactory() before going to production
        return restTemplateBuilder
            .requestFactory { requestFactory }
            .interceptors(interceptor)
            .build()
    }

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        // default rest template
        return restTemplateBuilder.build()
    }

    @Bean
    @Qualifier("Gap600")
    fun gap600RestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return headerModifiedRestTemplate(
            restTemplateBuilder,
            HeaderModifierInterceptor("x-api-key", gap600Configuration.apikey, "accept", "application/json")
        )
    }

    private fun getRequestFactory(): ClientHttpRequestFactory {
        try {
            val keystore = KeyStore.getInstance(sslConfiguration.keystoreType)

            if (sslConfiguration.trustStorePath.isEmpty()) {
                keystore.load(javaClass.getResourceAsStream("/wadzpay-dev.jks"), sslConfiguration.trustStorePassword)
            } else {
                keystore.load(FileInputStream(sslConfiguration.trustStorePath), sslConfiguration.trustStorePassword)
            }

            val trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustMgrFactory.init(keystore)

            val keyMgrFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyMgrFactory.init(keystore, sslConfiguration.trustStorePassword)

            val sslContext: SSLContext = SSLContext.getInstance(sslConfiguration.protocol)
            sslContext.init(keyMgrFactory.keyManagers, trustMgrFactory.trustManagers, null)

            val httpClient: HttpClient = HttpClientBuilder
                .create()
                .setSSLContext(sslContext)
                .build()

            return HttpComponentsClientHttpRequestFactory(httpClient)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    private fun getSslNoopRequestFactory(): ClientHttpRequestFactory {
        val acceptingTrustStrategy = TrustStrategy { _, _ -> true }
        val sslContext: SSLContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        val sslsf = SSLConnectionSocketFactory(
            sslContext,
            NoopHostnameVerifier.INSTANCE
        )

        val socketFactoryRegistry: Registry<ConnectionSocketFactory> =
            RegistryBuilder.create<ConnectionSocketFactory>()
                .register("https", sslsf)
                .register("http", PlainConnectionSocketFactory())
                .build()

        val connectionManager = BasicHttpClientConnectionManager(socketFactoryRegistry)
        val httpClient: CloseableHttpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
            .setConnectionManager(connectionManager).build()

        return HttpComponentsClientHttpRequestFactory(httpClient)
    }

    private fun algoGetSslNoopRequestFactory(): ClientHttpRequestFactory {
        val acceptingTrustStrategy = TrustStrategy { _, _ -> true }
        val sslContext: SSLContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()
        val sslsf = SSLConnectionSocketFactory(
            sslContext,
            NoopHostnameVerifier.INSTANCE
        )

        /* val socketFactoryRegistry: Registry<ConnectionSocketFactory> =
             RegistryBuilder.create<ConnectionSocketFactory>()
                 .register("https", sslsf)
                 .register("http", PlainConnectionSocketFactory())
                 .build()*/

        // val connectionManager = BasicHttpClientConnectionManager(socketFactoryRegistry)
        val connectionManager = PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS)
        connectionManager.defaultMaxPerRoute = 10
        connectionManager.maxTotal = 30
        val httpClient: CloseableHttpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
            .setConnectionManager(connectionManager).build()

        return HttpComponentsClientHttpRequestFactory(httpClient)
    }
    @Bean
    @Qualifier("AlgoCustomToken")
    fun AlgoCustomTokenRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        // HeaderModifierInterceptor(HttpHeaders.AUTHORIZATION, "Bearer ${algoCustomTokenConfiguration.token}")
        return algoHeaderModifiedRestTemplate(
            restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(8)).setReadTimeout(Duration.ofSeconds(8)),
            HeaderModifierInterceptor(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${algoCustomTokenConfiguration.token}",
                "Content-Type",
                "application/json"
            )
        )
    }
}

class HeaderModifierInterceptor(
    private val headerName: String,
    private val headerValue: String,
    private val headerName2: String = "",
    private val headerValue2: String = ""
) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.add(headerName, headerValue)
        request.headers.add(headerName2, headerValue2)
        return execution.execute(request, body)
    }
}
