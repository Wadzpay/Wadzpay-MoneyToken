package com.vacuumlabs.wadzpay.a

import com.degpeg.live.a.D
import com.degpeg.live.a.E
import java.math.BigInteger
import java.util.Base64
import javax.crypto.KeyGenerator

object EncoderUtil {

    fun getNewSaltKey(): String {
        val generator = KeyGenerator.getInstance("AES")
        generator.init(128)
        val key = generator.generateKey()
        val textKey = java.lang.String.format("%032X", BigInteger(+1, key.encoded))
        return Base64.getEncoder().encodeToString(textKey.encodeToByteArray())
    }

    fun getEncoded(saltKey: String, data: String): String {
        return E.getString(getSaltKey(saltKey), data)
    }

    fun getDecoded(saltKey: String, data: String): String {
        return D.getString(getSaltKey(saltKey), data)
    }

    private fun getSaltKey(saltKey: String): String {
        return Base64.getDecoder().decode(saltKey).decodeToString()
    }
}
