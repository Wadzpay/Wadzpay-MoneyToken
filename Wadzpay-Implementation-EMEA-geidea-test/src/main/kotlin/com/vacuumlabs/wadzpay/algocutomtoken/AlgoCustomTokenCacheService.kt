package com.vacuumlabs.wadzpay.algocutomtoken

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class AlgoCustomTokenCacheService(val stringRedisTemplate: StringRedisTemplate) {
    companion object {
        const val PvtNtToken = "pvtnttoken"
        const val Index = "index"
        const val decimals = "decimals"
        const val Loaded = "Loaded"
    }

    fun setCustomTokenCachedStatus(status: Boolean) {
        stringRedisTemplate.opsForValue().set("$PvtNtToken:$Loaded", status.toString())
    }

    fun getCustomTokenCachedStatus(): Boolean {
        try {
            return stringRedisTemplate.opsForValue().get("$PvtNtToken:$Loaded")!!.toBoolean()
        } catch (ex: Exception) {
            return false
        }
    }

    fun setCustomTokenName(tokenName: String) {
        stringRedisTemplate.opsForValue().set("$PvtNtToken:$tokenName", tokenName)
    }

    fun setCustomTokenIndex(tokenName: String, tokenIndex: String) {
        stringRedisTemplate.opsForValue().set("$PvtNtToken:$tokenName:$Index", tokenIndex)
    }

    fun getCustomTokenIndex(tokenName: String): String? {
        return stringRedisTemplate.opsForValue().get("$PvtNtToken:$tokenName:$Index")
    }

    fun setCustomTokenDecimals(tokenName: String, numOfDecimals: String) {
        stringRedisTemplate.opsForValue().set("$PvtNtToken:$tokenName:$Index:$decimals", numOfDecimals)
    }

    fun getCustomTokenDecimals(tokenName: String): String? {
        return stringRedisTemplate.opsForValue().get("$PvtNtToken:$tokenName:$Index:$decimals")
    }

    fun clearAssets(): Boolean? {
        stringRedisTemplate.delete(stringRedisTemplate.keys("$PvtNtToken:*"))
        return true
    }
}
