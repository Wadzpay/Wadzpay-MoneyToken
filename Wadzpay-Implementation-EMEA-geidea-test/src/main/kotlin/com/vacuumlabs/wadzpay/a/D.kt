package com.degpeg.live.a

import java.util.Base64

object D {
    fun getString(key: String, value: String): String {
        return G.getString(key, Base64.getDecoder().decode(value))
    }
}
