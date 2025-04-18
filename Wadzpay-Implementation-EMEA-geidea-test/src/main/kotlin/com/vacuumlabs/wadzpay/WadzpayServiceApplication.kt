package com.vacuumlabs.wadzpay

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerInitializerService
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator
import org.springframework.core.io.ClassPathResource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.springframework.web.multipart.support.MultipartFilter
import java.time.Instant
import javax.servlet.Filter

@OpenAPIDefinition(servers = [Server(url = "/")])
@SpringBootApplication
@ConfigurationPropertiesScan("com.vacuumlabs.wadzpay.configuration")
@EnableJpaRepositories("com.vacuumlabs.wadzpay", "com.vacuumlabs.vuba")
@EntityScan("com.vacuumlabs.wadzpay", "com.vacuumlabs.vuba")
@ComponentScan(
    "com.vacuumlabs.wadzpay",
    "com.vacuumlabs.vuba",
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator::class
)
@EnableScheduling
@EnableAsync
class WadzpayServiceApplication

fun main(args: Array<String>) {
    runApplication<WadzpayServiceApplication>(*args)
}

@Component
class OnStartupListener(
    val ledgerInitializerService: LedgerInitializerService,
    val apisLoggerRepository: ApisLoggerRepository
) :
    ApplicationListener<ApplicationReadyEvent> {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (ledgerInitializerService.isInitialized()) {
            logger.info("Skipping initialization of ledger assets and default accounts")
            if (ledgerInitializerService.isWTKAdded()) {
                logger.info("WTK Accounts Already Created!")
            } else {
                logger.info("Creating WTK Accounts! ")
                ledgerInitializerService.createAccountsFor(CurrencyUnit.WTK)
            }

            if (ledgerInitializerService.isUSDCAdded()) {
                logger.info("USDC Accounts Already Created!")
            } else {
                logger.info("Creating USDC Accounts!")
                ledgerInitializerService.createAccountsFor(CurrencyUnit.USDC)
            }
            // algorand Integration
            if (ledgerInitializerService.isALGOAdded()) {
                logger.info("ALGO Accounts Already Created!")
            } else {
                logger.info("Creating ALGO Accounts!")
                ledgerInitializerService.createAccountsFor(CurrencyUnit.ALGO)
            }

            // USDCa Integration
            if (ledgerInitializerService.isUSDCaAdded()) {
                logger.info("USDCA accounts already created!")
            } else {
                logger.info("Creating USDCA accounts!")
                ledgerInitializerService.createAccountsFor(CurrencyUnit.USDCA)
            }
            apisLoggerRepository.save(ApisLog("WadzpayServiceApplication", Instant.now().toString(), "NA", "NA"))
            // SAR
            if (ledgerInitializerService.isSARaAdded()) {
                logger.info("SAR Accounts Already Created!")
            } else {
                logger.info("Creating SAR Accounts! ")
                ledgerInitializerService.createAccountsFor(CurrencyUnit.SART)
            }
            return
        }

        logger.info("Initializing ledger assets and default accounts")
        ledgerInitializerService.createAssets()
        ledgerInitializerService.createAccounts()
    }

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val googleCredentials =
            GoogleCredentials.fromStream(ClassPathResource("wadzpaypilgrimwallet-firebase-adminsdk-362qu-ac48e6a198.json").inputStream)
        println("googleCredentials ==> " + googleCredentials)
        val options = FirebaseOptions.builder()
            .setCredentials(googleCredentials)
            .build()
        val app = FirebaseApp.initializeApp(options)
        println("options ==> " + options)
        return FirebaseMessaging.getInstance(app)
    }

    @Bean
    fun commonsMultipartResolver(): CommonsMultipartResolver {
        val commonsMultipartResolver = CommonsMultipartResolver()
        commonsMultipartResolver.setMaxUploadSize(-1)
        return commonsMultipartResolver
    }

    @Bean
    fun multipartFilterRegistrationBean(): FilterRegistrationBean<*> {
        val multipartFilter = MultipartFilter()
        val filterRegistrationBean: FilterRegistrationBean<*> = FilterRegistrationBean<Filter>(multipartFilter)
        filterRegistrationBean.addInitParameter("multipartResolverBeanName", "commonsMultipartResolver")
        return filterRegistrationBean
    }
}
