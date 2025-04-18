package com.degpeg.live.a

import javax.crypto.Cipher

object G {
    fun getString(key: String, value: ByteArray): String {
        val cipher = Cipher.getInstance(C.getC())
        cipher.init(2, A.getKey(key))
        return String(cipher.doFinal(value))
    }
}
