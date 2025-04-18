package com.degpeg.live.a

import javax.crypto.Cipher

object F {
    fun getBytes(key: String, value: String): ByteArray? {
        val cipher = Cipher.getInstance(C.getC())
        cipher.init(1, A.getKey(key))
        return cipher.doFinal(value.toByteArray())
    }
}
