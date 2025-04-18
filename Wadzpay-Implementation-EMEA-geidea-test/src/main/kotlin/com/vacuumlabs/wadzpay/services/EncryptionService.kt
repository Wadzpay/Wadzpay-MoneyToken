package com.vacuumlabs.wadzpay.services

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnauthorizedException
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import io.netty.handler.codec.DecoderException
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.spec.KeySpec
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService(
    val emailSMSSenderService: EmailSMSSenderService
) {
    var keySize = 128
    var iterationCount = 1000

    fun generatePwd(password: String, salt: String): String? {
        var generatedPassword: String? = null
        try {
            val strAlgoName = "SHA-512"
            val md: MessageDigest = MessageDigest.getInstance(strAlgoName)
            md.update(salt.toByteArray())
            val bytes: ByteArray =
                md.digest(password.toByteArray(StandardCharsets.UTF_8))
            val sb = StringBuilder()
            for (i in bytes.indices) {
                sb.append(java.lang.Integer.toString((bytes[i].toInt() and 0xff) + 0x100, 16).substring(1))
            }
            generatedPassword = sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return generatedPassword
    }

    @kotlin.Throws(NoSuchAlgorithmException::class)
    fun generateKey(salt: String, passphrase: String): SecretKey {
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec: KeySpec = PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize)
            return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } catch (e: Exception) {
            throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
        }
    }

    fun decrypt(
        salt: String,
        iv: String,
        passphrase: String,
        ciphertext: String
    ): ByteArray {
        try {
            val key: SecretKey = generateKey(salt, passphrase)
            return doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext))
        } catch (e: Exception) {
            throw UnauthorizedException(ErrorCodes.INVALID_PASSWORD)
        }
    }

    fun decryptMerchant(
        salt: String,
        iv: String,
        passphrase: String,
        ciphertext: String,
        emailId: String,
        phoneNumber: String,
        firstName: String,
        uuid: UUID?
    ): ByteArray {
        try {
            val key: SecretKey = generateKey(salt, passphrase)
            return doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext))
        } catch (e: Exception) {
            println("decrypt e  $e")
            sendInvalidPasscodeAlert(
                emailId,
                phoneNumber,
                firstName,
                uuid,
                Instant.now()
            )
            throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
        }
    }

    fun sendInvalidPasscodeAlert(
        emailId: String,
        mobileNumber: String,
        name: String,
        uuid: UUID?,
        alertDate: Instant
    ) {
        println("sendInvalidPasscodeAlert@111")
        sendEmailAlert(emailId, mobileNumber, name, uuid, alertDate)
        sendSMSAlert(emailId, mobileNumber, name, uuid, alertDate)
    }

    fun sendEmailAlert(
        emailId: String,
        mobileNumber: String,
        name: String,
        uuid: UUID?,
        alertDate: Instant
    ) {
        println("sendEmailAlert@145")
        if (emailId != null) {
            emailSMSSenderService.sendEmail(
                "Customer Offline Invalid Passcode Alert",
                emailSMSSenderService.userOfflineAlertEmailBody(
                    name,
                    mobileNumber,
                    uuid,
                    alertDate
                ),
                emailId,
                "contact@wadzpay.com"
            )
        }
    }

    fun sendSMSAlert(
        emailId: String,
        mobileNumber: String,
        name: String,
        uuid: UUID?,
        alertDate: Instant
    ) {
        println("sendSMSAlert@145")
        val strSmsText: String = "Dear Customer" + ",\n" +
            "We regret to inform you that your Passcode Invalid Attempt with below details \n" +
            "UUID - " + uuid + ",\n" +
            "Date: " + formattingDate(alertDate!!)

        if (mobileNumber != null) {
            emailSMSSenderService.sendMobileSMS(
                mobileNumber,
                strSmsText
            )
        }
    }

    fun formattingDate(date: Instant): String? {
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(date)
    }

    fun base64(str: String): ByteArray {
        return Base64.decodeBase64(str)
    }

    fun doFinal(encryptMode: Int, key: SecretKey, iv: String, bytes: ByteArray): ByteArray {
        try {
            var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(encryptMode, key, IvParameterSpec(hex(iv)))
            return cipher.doFinal(bytes)
        } catch (e: Exception) {
            throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
        }
    }

    fun hex(str: String): ByteArray? {
        return try {
            Hex.decodeHex(str.toCharArray())
        } catch (e: DecoderException) {
            throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
        }
    }
}
