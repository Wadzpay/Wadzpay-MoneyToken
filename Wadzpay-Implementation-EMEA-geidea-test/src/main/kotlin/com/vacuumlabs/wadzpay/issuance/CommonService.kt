package com.vacuumlabs.wadzpay.issuance

import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

@Service
data class CommonService(
    val emailSMSSenderService: EmailSMSSenderService,
    val appConfig: AppConfig
) {
    fun checkEmail(username: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return username.matches(emailRegex.toRegex())
    }
/*UUID with 16 char length*/
    fun hashUuid(): String {
        val uuid = UUID.randomUUID().toString()
        val algorithm: String = "MD5"
        val bytes = MessageDigest.getInstance(algorithm).digest(uuid.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.substring(0, 16) // 16 characters hash
    }

    /*UUID with 22 char length*/
    fun toBase64(): String {
        val uuid = UUID.randomUUID()
        val uuidBytes = ByteArray(16)
        val buffer = java.nio.ByteBuffer.wrap(uuidBytes)
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes)
    }

    fun sendEmail(email: String, passwordUUID: String) {
        // https://merchant.privatechain-dev.wadzpay.com/reset-password/{uuid}
        val webUrl = "https://merchant.privatechain-dev.wadzpay.com/reset-password/$passwordUUID"
        // val webUrl = "https://issuance.${appConfig.environment}.wadzpay.com/accept-invite?email=$email"
        try {
            emailSMSSenderService.sendEmailWadzpay(
                "Signup",
                emailSMSSenderService.getIssuanceEmailBody(
                    email,
                    webUrl
                ),
                email,
                "contact@wadzpay.com"
            )
        } catch (e: Exception) {
            println(e)
        }
    }
}
