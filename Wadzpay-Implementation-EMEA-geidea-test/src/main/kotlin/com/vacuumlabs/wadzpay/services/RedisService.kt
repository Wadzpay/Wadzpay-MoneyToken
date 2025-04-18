package com.vacuumlabs.wadzpay.services

import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * This service provides methods for caching the phone and email and for checking the status of their verification.
 * We are creating several keys in redis for this and all are prefixed with registration:
 * registration:{phone number} (e.g. registration:+420123456789) - the value is verification status
 * registration:{phone number}:{email} (e.g. registration:+420123456789:name@gmail.com) - the value is verification status
 * registration:{phone number}:{email}:pass (e.g. registration:+420123456789:name@gmail.com:pass) - the value is password (we need to change this)
 */
@Service
class RedisService(val stringRedisTemplate: StringRedisTemplate) {

    companion object {
        const val REGISTRATION = "registration"
        const val PASSWORD = "password"
        const val REQUESTED = "requested"
        const val VERIFIED = "verified"

        const val EXCHANGE = "exchange"
        const val PRESENT = "present"

        const val INVITATION = "invitation"
        const val OTP_VIA = "otpvia"
        const val OTP_CODE = "otpcode"
    }

    fun queryExchangeRates(from: FiatCurrencyUnit, to: CurrencyUnit): BigDecimal {
        return stringRedisTemplate.opsForValue().get("$EXCHANGE:$from:$to")!!.toBigDecimal()
    }

    fun setExchangeRatesPresent(from: FiatCurrencyUnit) {
        stringRedisTemplate.opsForValue().set("$EXCHANGE:$from", PRESENT, 10, TimeUnit.SECONDS)
    }

    fun storeExchangeRates(from: FiatCurrencyUnit, to: CurrencyUnit, rate: BigDecimal) {
        stringRedisTemplate.opsForValue().set("$EXCHANGE:$from:$to", rate.toString())
    }

    fun getExchangeRatesPresent(from: FiatCurrencyUnit): Boolean {
        return stringRedisTemplate.opsForValue().get("$EXCHANGE:$from") != null
    }

    fun savePhone(phone: String) {
        stringRedisTemplate.opsForValue().set("$REGISTRATION:$phone", REQUESTED, 1, TimeUnit.HOURS)
    }

    fun setPhoneVerified(phone: String) {
        stringRedisTemplate.opsForValue().set("$REGISTRATION:$phone", VERIFIED, 1, TimeUnit.HOURS)
    }

    fun saveEmailForPhone(email: String, phone: String) {
        stringRedisTemplate.delete("$REGISTRATION:$phone:*")
        stringRedisTemplate.opsForValue().set("$REGISTRATION:$phone:$email", REQUESTED, 1, TimeUnit.HOURS)
    }

    fun setEmailForPhoneVerified(email: String, phone: String) {
        stringRedisTemplate.opsForValue().set("$REGISTRATION:$phone:$email", VERIFIED, 1, TimeUnit.HOURS)
    }

    fun getPhoneVerified(phone: String): String? {
        return stringRedisTemplate.opsForValue().get("$REGISTRATION:$phone")
    }

    fun getEmailVerified(email: String): String? {
        return stringRedisTemplate.opsForValue().get("$REGISTRATION:*:$email")
    }

    fun getEmailVerifiedForPhone(email: String, phone: String): String? {
        return stringRedisTemplate.opsForValue().get("$REGISTRATION:$phone:$email")
    }

    fun deleteRegistrationEntity(email: String?, phone: String?) {
        stringRedisTemplate.delete("$REGISTRATION:$phone:$email:$PASSWORD")
        stringRedisTemplate.delete("$REGISTRATION:$phone:$email")
        stringRedisTemplate.delete("$REGISTRATION:$phone")
    }

    fun inviteUser(email: String, role: Role, merchantId: Long) {
        stringRedisTemplate.opsForValue().set("$INVITATION:$email", "$merchantId,$role", 7, TimeUnit.DAYS)
    }

    fun getInvitation(email: String): String? {
        return stringRedisTemplate.opsForValue().get("$INVITATION:$email")
    }

    fun getInvitations(merchantId: Long): ArrayList<InvitedMerchants> {
        val allKeys = stringRedisTemplate.keys("$INVITATION*")
        val invitedList = ArrayList<InvitedMerchants>()
        for (v in allKeys) {
            val invite = stringRedisTemplate.opsForValue().get(v)
            if (invite != null) {
                val id = invite.substringBeforeLast(',').toLong()
                val role = Role.valueOf(invite.substringAfterLast(','))
                if (merchantId == id) {
                    invitedList.add(InvitedMerchants(v.removePrefix("$INVITATION:"), role))
                }
            }
        }

        return invitedList
    }
    fun setBankDetailsInCache(userId: String, transactionType: String, bankAccountNumber: String) {
        stringRedisTemplate.opsForValue().set("$userId:$transactionType", bankAccountNumber)
    }

    fun getBankDetailsInCache(userId: String, transactionType: String): String? {
        return stringRedisTemplate.opsForValue().get("$userId:$transactionType")
    }

    fun deleteBankDetailsInCache(userId: String, transactionType: String) {
        stringRedisTemplate.delete("$userId:$transactionType")
    }

    fun saveOTPWithEmailPhoneNumber(requestedBy: String, otpCode: String?, expirationTime: Long) {
        stringRedisTemplate.opsForValue().set("$OTP_VIA:$requestedBy:$OTP_CODE:$otpCode", REQUESTED, expirationTime, TimeUnit.SECONDS)
    }
}

data class InvitedMerchants(val email: String, val role: Role)
