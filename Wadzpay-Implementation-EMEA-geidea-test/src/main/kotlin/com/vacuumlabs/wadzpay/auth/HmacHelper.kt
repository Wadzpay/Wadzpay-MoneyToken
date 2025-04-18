package com.vacuumlabs.wadzpay.auth

import com.vacuumlabs.wadzpay.configuration.OnramperConfiguration
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class HmacHelper(val onramperConfiguration: OnramperConfiguration) {

    fun sign(requestBody: String): String {
        val sha256HMAC = Mac.getInstance(onramperConfiguration.hashingAlgorithm)
        val secretKey = SecretKeySpec(
            onramperConfiguration.secret.toByteArray(),
            onramperConfiguration.hashingAlgorithm
        )
        sha256HMAC.init(secretKey)
        return Hex.encodeHexString(sha256HMAC.doFinal(requestBody.toByteArray()))
    }

    fun verifySignature(requestBody: String, clientSignature: String): Boolean {
        val serverSignature = sign(requestBody)

        return serverSignature == clientSignature
    }
}
