package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.merchant.MerchantDashboardService
import com.vacuumlabs.wadzpay.merchant.MerchantService
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.services.SeonService
import com.vacuumlabs.wadzpay.services.TwilioService
import org.springframework.stereotype.Service

@Service
class RegistrationService(
    val cognitoService: CognitoService,
    val twilioService: TwilioService,
    val redisService: RedisService,
    val seonService: SeonService,
    val userAccountService: UserAccountService,
    val merchantService: MerchantService,
    val merchantDashboardService: MerchantDashboardService,
    val appConfig: AppConfig,
    val issuanceWalletService: IssuanceWalletService
) {
    fun startRegistrationForPhoneNumber(phoneNumber: String) {
        // check if the number is not taken by an existing account, we don't check redis, because this could be a resend request
        if (!cognitoService.isPhoneAvailable(phoneNumber)) {
            throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
        }
        /*if (appConfig.production) {
            twilioService.sendPhoneOTPCode(phoneNumber)
        }*/
        twilioService.sendPhoneOTPCode(phoneNumber)
        redisService.savePhone(phoneNumber)
    }

    fun verifyPhoneNumber(phoneNumber: String, code: String) {
        val phoneVerified: String = redisService.getPhoneVerified(phoneNumber)
            // check if we already have an OTP request for given number
            ?: throw EntityNotFoundException(ErrorCodes.PHONE_NUMBER_DOES_NOT_EXISTS)

        // check if phone number is not already verified
        if (phoneVerified == RedisService.VERIFIED) {
            return
        }

        // check if phone number is not already taken by an existing account
        if (!cognitoService.isPhoneAvailable(phoneNumber)) {
            throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
        }
        /*if (appConfig.production) {
            twilioService.verifyOTPCode(phoneNumber, code)
        }*/
        twilioService.verifyOTPCode(phoneNumber, code)
        redisService.setPhoneVerified(phoneNumber)
    }

    fun preRegister(email: String, phoneNumber: String, password: String, ipAddress: String) {
        println("preRegister-2")
        // check if provided phone is verified
        if (redisService.getPhoneVerified(phoneNumber) == null ||
            redisService.getPhoneVerified(phoneNumber).equals(RedisService.REQUESTED)
        ) {
            throw BadRequestException(ErrorCodes.UNVERIFIED_PHONE_NUMBER)
        }

        // if we already have a registration with this phone and email pair, we do nothing
        if (redisService.getEmailVerifiedForPhone(email, phoneNumber).equals(RedisService.REQUESTED)) {
            println("sendEmailOTPCode-@72")
            twilioService.sendEmailOTPCode(email)
            if (appConfig.production) {
//                twilioService.sendEmailOTPCode(email)
            } else {
                redisService.saveEmailForPhone(email, phoneNumber)
            }
            return
        }

        // check if there is no other account with this email
        if (redisService.getEmailVerified(email) != null || !cognitoService.isEmailAvailable(email)) {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }

        // TODO: users with REVIEW result should be marked as such and manually reviewed
        seonService.checkStatus(phoneNumber, email, ipAddress)

        redisService.saveEmailForPhone(email, phoneNumber)
        /*if (appConfig.production) {
            twilioService.sendEmailOTPCode(email)
        }*/
        twilioService.sendEmailOTPCode(email)
    }

    fun finishRegistration(
        email: String,
        phoneNumber: String,
        code: String,
        password: String,
        isMerchantAdmin: Boolean,
        institutionId: String?
    ): UserAccount {
        // check if the phone number is present and verified
        if (redisService.getPhoneVerified(phoneNumber) == null ||
            redisService.getPhoneVerified(phoneNumber).equals(RedisService.REQUESTED)
        ) {
            throw BadRequestException(ErrorCodes.UNVERIFIED_PHONE_NUMBER)
        }

        // check if email is present
        if (redisService.getEmailVerifiedForPhone(email, phoneNumber) == null) {
            throw EntityNotFoundException(ErrorCodes.EMAIL_DOES_NOT_EXISTS)
        }

        // check if there is no other account with this email
        if (!cognitoService.isEmailAvailable(email)) {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }

        val emailVerifiedStatus = redisService.getEmailVerifiedForPhone(email, phoneNumber)

        if (emailVerifiedStatus != RedisService.VERIFIED) {
            /*if (appConfig.production) {
                twilioService.verifyOTPCode(email, code)
            }*/
            twilioService.verifyOTPCode(email, code)
            redisService.setEmailForPhoneVerified(email, phoneNumber)
        }

        val cognitoUsername = cognitoService.register(email, phoneNumber, password, false)

        redisService.deleteRegistrationEntity(email, phoneNumber)
        val userAccount = userAccountService.createUserAccount(cognitoUsername, email, phoneNumber)
        if (!institutionId.isNullOrEmpty()) {
            issuanceWalletService.mappingWithInstitution(institutionId, userAccount)
        }
        merchantDashboardService.getInvitation(email)?.let {
            cognitoService.addToGroup(userAccount, it.role)
            userAccount.merchant = merchantService.getMerchantByName(it.merchantName)
            userAccountService.userAccountRepository.save(userAccount)
        }

        if (isMerchantAdmin) {
            cognitoService.addToGroup(userAccount, Role.MERCHANT_ADMIN)
        }
        return userAccount
    }

    fun finishFakeRegistration(email: String, phoneNumber: String, code: String, password: String, isMerchantAdmin: Boolean): UserAccount {
        // check if the phone number is present and verified
        if (redisService.getPhoneVerified(phoneNumber) == null ||
            redisService.getPhoneVerified(phoneNumber).equals(RedisService.REQUESTED)
        ) {
            //  throw BadRequestException(ErrorCodes.UNVERIFIED_PHONE_NUMBER)
        }

        // check if email is present
        if (redisService.getEmailVerifiedForPhone(email, phoneNumber) == null) {
            // throw EntityNotFoundException(ErrorCodes.EMAIL_DOES_NOT_EXISTS)
        }

        // check if there is no other account with this email
        if (!cognitoService.isEmailAvailable(email)) {
            //   throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }

        val emailVerifiedStatus = redisService.getEmailVerifiedForPhone(email, phoneNumber)

        if (emailVerifiedStatus != RedisService.VERIFIED) {
            if (appConfig.production) {
                // twilioService.verifyOTPCode(email, code)
            }

            // redisService.setEmailForPhoneVerified(email, phoneNumber)
        }

        val cognitoUsername = cognitoService.register(email, phoneNumber, password, false)

        //  redisService.deleteRegistrationEntity(email, phoneNumber)
        val userAccount = userAccountService.createUserAccount(cognitoUsername, email, phoneNumber)

        merchantDashboardService.getInvitation(email)?.let {
            cognitoService.addToGroup(userAccount, it.role)
            userAccount.merchant = merchantService.getMerchantByName(it.merchantName)
            userAccountService.userAccountRepository.save(userAccount)
        }

        if (isMerchantAdmin) {
            cognitoService.addToGroup(userAccount, Role.MERCHANT_ADMIN)
        }

        return userAccount
    }

    fun sendOTP(email: String?, phoneNumber: String?) {
        if (phoneNumber != null) {
            twilioService.sendPhoneOTPCode(phoneNumber)
            redisService.savePhone(phoneNumber)
        }
        if (email != null) {
            twilioService.sendEmailOTPCode(email)
        }
    }

    fun verifyOTP(email: String?, emailCode: String?, phoneNumber: String?, phoneNumberCode: String?) {
        if (phoneNumber != null && phoneNumberCode != null) {
            twilioService.verifyOTPCode(phoneNumber, phoneNumberCode)
            redisService.setPhoneVerified(phoneNumber)
        }
        if (email != null && emailCode != null) {
            twilioService.verifyOTPCode(email, emailCode)
        }
    }

    fun sendOTPWithDynamicExpirationTime(email: String?, phoneNumber: String?, otpCode: String?, expirationTime: Long) {
        try {
            if (phoneNumber != null) {
                twilioService.sendPhoneOTPCodeWithDynamicToken(phoneNumber, otpCode)
                redisService.saveOTPWithEmailPhoneNumber(phoneNumber, otpCode, expirationTime)
            }
            if (email != null) {
                twilioService.sendEmailOTPCodeWithDynamicToken(email, otpCode)
                redisService.saveOTPWithEmailPhoneNumber(email, otpCode, expirationTime)
            }
        } catch (e: Exception) {
            println("Exception ==> $e")
        }
    }
}
