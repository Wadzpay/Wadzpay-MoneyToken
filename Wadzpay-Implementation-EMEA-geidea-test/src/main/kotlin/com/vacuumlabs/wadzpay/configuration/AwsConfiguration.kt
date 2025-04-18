package com.vacuumlabs.wadzpay.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.SetSmsAttributesRequest
import java.util.Properties
import javax.mail.Session

@Configuration
class AwsConfiguration(val jwtConfiguration: JwtConfiguration, val appConfig: AppConfig) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider? {
        return AwsCredentialsProviderChain.builder()
            .addCredentialsProvider(DefaultCredentialsProvider.create())
            .addCredentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        jwtConfiguration.accessKeyId,
                        jwtConfiguration.secretAccessKey
                    )
                )
            )
            .build()
    }

    @Bean
    fun cognitoClient(): CognitoIdentityProviderClient {
        return CognitoIdentityProviderClient.builder()
            .credentialsProvider(
                awsCredentialsProvider()
            )
            .region(Region.of(jwtConfiguration.region))
            .build()
    }

    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .region(Region.of(jwtConfiguration.region))
            .credentialsProvider(awsCredentialsProvider())
            .build()
    }

    @Bean
    fun snsClient(): SnsClient {
        val snsClient = SnsClient.builder().region(Region.of(jwtConfiguration.region))
            .credentialsProvider(awsCredentialsProvider())
            .build()

        snsClient.setSMSAttributes(
            SetSmsAttributesRequest.builder().attributes(
                mapOf(
                    Pair("DefaultSMSType", "Transactional"), Pair("DefaultSenderID", "WadzPay")
                )
            ).build()
        )

        return snsClient
    }

    fun getEmailSession(): EmailConfig {

        val host = "email-smtp." + if (appConfig.environment.equals("ddf", true) || appConfig.environment.equals("ddf-uat", true)) { "ap-southeast-1" } else { "eu-central-1" } + ".amazonaws.com" // TODO: Update when AWS sendbox is removed

        val props: Properties = System.getProperties()
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.port"] = 587
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.auth"] = "true"
        val session = Session.getDefaultInstance(props)
        return EmailConfig(session, host, getAuthDetails())
    }

    private fun getAuthDetails(): EmailConfig.AuthDetails {
        return if (appConfig.environment.equals("prod", true)) {
            EmailConfig.AuthDetails("AKIAQ2ZOXCORTVUIX2P7", "BJ7dn6Eo0Tbb6rbgF2ulTK0mVg4bJiyEcNR+ImsUketu")
        } else if (appConfig.environment.equals("test", true)) {
            EmailConfig.AuthDetails("AKIA3RSU2SV5Q6T4XXML", "BNGRqpuCCDPcLg2J2n5KeLwdND0K5+FhqD9U8IHUyVc7")
        } else if (appConfig.environment.equals("uat", true)) {
            EmailConfig.AuthDetails("AKIAZUMVA66L6KONU55Z", "BHBRpL7TskWSGHhhgNb4yfYfNlf/Ogkj3mm0GSxO/Kc5")
        } else if (appConfig.environment.equals("poc", true)) {
            EmailConfig.AuthDetails("AKIA2GUNIWRIYMBZQKDG", "BLRInJxoF3YG/rJU7joD15H5Vho10VHyZcfM+pK3IA6e")
        } else if (appConfig.environment.equals("ddf", true)) { // TODO: Update when AWS sendbox is removed
            EmailConfig.AuthDetails("AKIA3RSU2SV5Q6T4XXML", "BNGRqpuCCDPcLg2J2n5KeLwdND0K5+FhqD9U8IHUyVc7")
        } else if (appConfig.environment.equals("ddf-uat", true)) { // TODO: Update when AWS sendbox is removed
            EmailConfig.AuthDetails("AKIA3RSU2SV5Q6T4XXML", "BNGRqpuCCDPcLg2J2n5KeLwdND0K5+FhqD9U8IHUyVc7")
        } else if (appConfig.environment.equals("geidea-dev", true)) { // TODO: Update when AWS sendbox is removed
            EmailConfig.AuthDetails("AKIAWG6SXYEEXSMKPSMF", "BFZxg+RbMt7wz5Xyl/1KedXQ3oDN5PFgXz0k5uU3Aqa4")
        } else if (appConfig.environment.equals("geidea-test", true)) { // TODO: Update when AWS sendbox is removed
            EmailConfig.AuthDetails("AKIAR3T64BWVHSIBONR2", "BHvwJHRbO7MkSJtpAHByoxvNEhTHGrm6BaPEfg0sGPgZ")
        } else if (appConfig.environment.equals("privatechain-prod", true)) { // TODO: Update when AWS sendbox is removed
            EmailConfig.AuthDetails("AKIAR3T64BWVEPXRW2OU", "BBVsmqIXGT++6BAT4EGxh6Bx2TeP5dy0aIHSur074Hm6")
        } else {
            EmailConfig.AuthDetails("AKIATEDRF3XZ52VZXZV5", "BCHC1xJCv3KppqFSiBt0ZWDi/7kkafqOwu60qJuFf0qc")
        }
    }

    data class EmailConfig(val session: Session, val HOST: String, val authDetails: AuthDetails) {
        data class AuthDetails(val username: String, val password: String)
    }
}
