package com.vacuumlabs.wadzpay.utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class EncryptService {
    val encoder = BCryptPasswordEncoder(16)
    val decoder = BCryptPasswordEncoder(16)

    fun getEncodedString(string: String): String {
        return encoder.encode(string)
    }
    fun isDecodedStringMatched(raw: String, encodedSting: String): Boolean {
        return encoder.matches(raw, encodedSting)
    }
}
