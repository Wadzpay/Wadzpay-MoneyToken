package com.degpeg.live.a

import javax.crypto.spec.SecretKeySpec

object A {
    fun getKey(key: String): SecretKeySpec {
        return SecretKeySpec(key.toByteArray(), B.getB())
    }
}
