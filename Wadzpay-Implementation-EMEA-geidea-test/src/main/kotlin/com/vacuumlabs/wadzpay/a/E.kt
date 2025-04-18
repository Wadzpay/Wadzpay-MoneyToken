package com.degpeg.live.a

import java.util.Base64

object E {
    fun getString(key: String, value: String): String {
        return Base64.getEncoder().encodeToString(F.getBytes(key, value))
    }
    fun getString(value: String): String {
        return Base64.getEncoder().encodeToString(F.getBytes(C.temp, value))
    }
}
