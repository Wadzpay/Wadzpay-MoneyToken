package com.degpeg.live.a

import java.util.Base64

object N {
    fun getA(): String {
        return G.getString(C.temp, Base64.getDecoder().decode(C.temp))
    }
}
