package com.vacuumlabs.wadzpay.services

import com.twilio.Twilio
import com.twilio.exception.ApiException
import com.twilio.rest.verify.v2.service.Verification
import com.twilio.rest.verify.v2.service.VerificationCheck
import com.twilio.type.PhoneNumber
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INCORRECT_CODE
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.INVALID_PHONE_NUMBER
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.UNKNOWN_TWILIO_ERROR
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.VERIFICATION_NOT_FOUND
import com.vacuumlabs.wadzpay.common.ForbiddenException
import com.vacuumlabs.wadzpay.common.ServiceUnavailableException
import com.vacuumlabs.wadzpay.configuration.TwilioConfiguration
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.utils.ApisLog
import com.vacuumlabs.wadzpay.utils.ApisLoggerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TwilioService(val twilioConfiguration: TwilioConfiguration, val userAccountService: UserAccountService, val apisLoggerRepository: ApisLoggerRepository) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    init {
        Twilio.init(twilioConfiguration.accountSid, twilioConfiguration.authToken)
    }

    fun sendPhoneOTPCode(phoneNumber: String): String {
        return sendOTPCode(phoneNumber, Verification.Channel.SMS, null)
    }

    fun sendEmailOTPCode(email: String): String {
        println("sendEmailOTPCode-@37")
        return sendOTPCode(email, Verification.Channel.EMAIL, null)
    }

    private fun sendOTPCode(target: String, channel: Verification.Channel, otpCode: String?): String {
        println("sendOTPCode-@42")
        val verification = try {
            println("sendOTPCode-@44 " + twilioConfiguration.verifyServiceSid + " " + target + " " + channel.toString())
            Verification.creator(twilioConfiguration.verifyServiceSid, target, channel.toString()).setCustomCode(otpCode).create()
        } catch (e: ApiException) {
            print("${e.message}  - e")
            apisLoggerRepository.save(ApisLog("TwilioSendOTOP", e.message.toString(), target, "Twilio Error Code:- " + e.code))
            logger.error(e.message, e)
            throw when (e.code) {
                // https://www.twilio.com/docs/api/errors/60200
                60200 -> BadRequestException(INVALID_PHONE_NUMBER)
                else -> ServiceUnavailableException(UNKNOWN_TWILIO_ERROR)
            }
        }
        return verification.sid
    }

    fun verifyOTPCode(target: String, code: String) {
        println("verifyOTPCode-@59")
        val verificationCheck = try {
            VerificationCheck.creator(twilioConfiguration.verifyServiceSid, code).setTo(target).create()
        } catch (e: ApiException) {
            print("${e.message}  - e")
            logger.error(e.message, e)
            if (e.statusCode == 404) {
                // code is either expired, already approved, max attempts to check code have been reached or verification is nonexisting
                throw EntityNotFoundException(VERIFICATION_NOT_FOUND)
            } else {
                throw throw ServiceUnavailableException(UNKNOWN_TWILIO_ERROR)
            }
        }

        when (verificationCheck.status) {
            "approved" -> return // verification succeeded
            "pending" -> throw ForbiddenException(INCORRECT_CODE) // it's permitted to try another code
            else -> throw ServiceUnavailableException(UNKNOWN_TWILIO_ERROR + "@76")
        }
    }

    fun sendMobileSMS(toMobileNumber: String, messageBody: String): String {
        // Twilio.init("ACfffa916e9fc1a98f25cdf7b2f28ec692", "11004e1285cf681c1bbcc3eafeb66194")
        val message: com.twilio.rest.api.v2010.account.Message
        try {
            message = com.twilio.rest.api.v2010.account.Message.creator(
                PhoneNumber(toMobileNumber),
                PhoneNumber("+16463623750"),
                messageBody
            ).create()
        } catch (e: ApiException) {
            //  apisLoggerRepository.save(ApisLog("TwilioSendSMSError", e.message.toString(), toMobileNumber, "Twilio Error Code:- " + e.code))
            print(e.message)
            throw when (e.code) {
                else -> ServiceUnavailableException(e.message.toString())
            }
        }
        return message.sid
    }

    fun sendPhoneOTPCodeWithDynamicToken(phoneNumber: String, otpCode: String?): String {
        return sendOTPCode(phoneNumber, Verification.Channel.SMS, otpCode)
    }

    fun sendEmailOTPCodeWithDynamicToken(email: String, otpCode: String?): String {
        println("sendEmailOTPCode-@37")
        return sendOTPCode(email, Verification.Channel.EMAIL, otpCode)
    }
}
