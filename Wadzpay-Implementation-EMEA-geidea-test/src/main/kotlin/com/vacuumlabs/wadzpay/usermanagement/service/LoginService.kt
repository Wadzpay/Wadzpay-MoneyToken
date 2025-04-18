package com.vacuumlabs.wadzpay.usermanagement.service

import com.vacuumlabs.PASSCODE_PASSPHRASE
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ForbiddenException
import com.vacuumlabs.wadzpay.common.UnauthorizedException
import com.vacuumlabs.wadzpay.services.EncryptionService
import com.vacuumlabs.wadzpay.user.RegistrationService
import com.vacuumlabs.wadzpay.usermanagement.dataclass.ErrorResponse
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginConfigurationData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginUserDetailsData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginVerifyOTPRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.SendOTPVia
import com.vacuumlabs.wadzpay.usermanagement.dataclass.SetPasswordRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.StatusEnum
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UserLoginConfiguration
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetails
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsDataViewModel
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsRepository
import com.vacuumlabs.wadzpay.usermanagement.model.toViewModelUserDetails
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlin.math.pow
import kotlin.random.Random

@Service
class LoginService(
    val userDetailsService: UserDetailsService,
    val userDetailsRepository: UserDetailsRepository,
    val loginConfigurationService: LoginConfigurationService,
    val encryptionService: EncryptionService,
    val registrationService: RegistrationService
) {
    fun getUserByUUID(userUUID: String): UserDetailsDataViewModel {
        val userData = userDetailsRepository.getByPasswordUuid(userUUID)
        if (userData != null) {
            return userData.toViewModelUserDetails()
        }
        throw ForbiddenException(ErrorCodes.EXPIRED_OR_ALREADY_USED)
    }

    fun getUserConfigByEmail(email: String): LoginConfigurationData {
        val isUserExist = userDetailsService.getUserDetailsByEmail(email)
        if (isUserExist) {
            val loginConfigData = loginConfigurationService.getConfigDataByAggregatorId(1)
            if (loginConfigData.toList().isNotEmpty()) {
                return loginConfigData.toList()[0]
            }
        }
        throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun login(loginRequest: LoginRequest): Any {
        var userData = userDetailsRepository.getByEmailId(loginRequest.emailId)
        if (userData != null) {
            if (userData.status == StatusEnum.LOCKED) {
                throw ForbiddenException(ErrorCodes.USER_LOCKED)
            }
            val encryptedPassword = createPassword(loginRequest.password, userData)
            val loginConfigData = loginConfigurationService.getConfigDataByAggregatorId(1)
            val userPasswordData = userDetailsService.getUserDetailsByEmailAndPassword(loginRequest.emailId, encryptedPassword)
            if (userPasswordData != null) {
                userPasswordData.lastSuccessLogin = Instant.now()
                userData.failedAttempts = 0
                userDetailsRepository.save(userPasswordData)
                val userLoginData = userPasswordData.toViewModelUserDetails()
                val loginUserDetailsData = LoginUserDetailsData(
                    userEmail = userPasswordData.emailId,
                    userMobile = userPasswordData.countryCode + userPasswordData.mobileNo,
                    loginConfiguration = null,
                    token = null
                )
                if (loginConfigData.toList().isNotEmpty()) {
                    val loginConfiguration = loginConfigData.toList()[0]
                    userLoginData.loginConfiguration = loginConfiguration
                    /*Sending OTP IF TWO FACTOR enabled*/
                    sendOTP(userPasswordData, loginConfiguration)
                    loginUserDetailsData.loginConfiguration = UserLoginConfiguration(
                        otpLength = loginConfiguration.otpLength,
                        resendOtpLinkInSeconds = loginConfiguration.resendOtpLinkInSeconds,
                        resendOtpNoOfTimes = loginConfiguration.resendOtpNoOfTimes,
                        sendOtpVia = loginConfiguration.sendOtpVia,
                        isMultifactorEnable = loginConfiguration.isMultifactorEnable
                    )
                }
                return loginUserDetailsData
            } else {
                if (loginConfigData.toList().isNotEmpty()) {
                    val loginConfiguration = loginConfigData.toList()[0]
                    if (loginConfiguration.noOfFailedLoginAttempts != null && loginConfiguration.noOfFailedLoginAttempts > userData.failedAttempts) {
                        userData.failedAttempts += 1
                        userData = userDetailsRepository.save(userData)
                        val attemptRemaining = loginConfiguration.noOfFailedLoginAttempts.minus(userData.failedAttempts)
                        return if (userData.failedAttempts == 0) {
                            ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), ErrorCodes.USER_LOCKED, attemptRemaining.toString()))
                        } else {
                            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(
                                    ErrorResponse(
                                        HttpStatus.UNAUTHORIZED.value(),
                                        "Incorrect Password - {$attemptRemaining} times left", attemptRemaining.toString()
                                    )
                                )
                        }
                    } else {
                        userData.status = StatusEnum.LOCKED
                        userData.password = null
                        userData.failedAttempts = 0
                        userDetailsRepository.save(userData)
                        throw ForbiddenException(ErrorCodes.USER_LOCKED)
                    }
                }
                throw UnauthorizedException(ErrorCodes.INVALID_PASSWORD)
            }
        }
        throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun setPassword(passwordRequest: SetPasswordRequest): Boolean {
        val userData = userDetailsRepository.getByPasswordUuid(passwordRequest.userUUID)
        if (userData != null) {
            val encryptedPassword = createPassword(passwordRequest.password, userData)
            userData.status = StatusEnum.ACTIVE
            userData.password = encryptedPassword
            userData.passwordUuid = null
            userDetailsRepository.save(userData)
            return true
        }
        throw EntityNotFoundException(ErrorCodes.INVALID_ID)
    }

    fun createPassword(passcodeHash: String, userData: UserDetails): String {
        if (passcodeHash.split("::").size === 3) {
            val decryptPasscode = encryptionService.decrypt(
                passcodeHash.split("::")[1],
                passcodeHash.split("::")[0],
                PASSCODE_PASSPHRASE,
                passcodeHash.split("::")[2]
            )
            val sDecryptPasscode = String(decryptPasscode, StandardCharsets.UTF_8)
            val saltKey = userDetailsService.getSaltKeyByUserId(userData)
            val encryptedHash = encryptionService.generatePwd(sDecryptPasscode, saltKey)
            if (encryptedHash != null) {
                return encryptedHash
            }
        }
        throw EntityNotFoundException(ErrorCodes.SALT_KEY_NOT_FOUND)
    }

    fun resetPasswordLinkByEmail(email: String): Boolean {
        val userData = userDetailsRepository.getByEmailId(email)
        if (userData?.status != null && userData.status != StatusEnum.PENDING_APPROVAL) {
            userDetailsService.sendPasswordResetLink(userData, true)
            return true
        }
        throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun sendOTP(userLoginData: UserDetails, loginConfiguration: LoginConfigurationData?): Boolean {
        /* Checking login config data and checking two factor enabled */
        if (loginConfiguration!!.isMultifactorEnable == true) {
            val loginConfigData = loginConfigurationService.getByUserLoginConfigId(loginConfiguration.userLoginConfigId)
            if (loginConfigData != null) {
                /* Settling OTP expiration time */
                var expirationTime: Long = 120
                var randomNumber: String? = null
                if (loginConfigData.otpLength != null) {
                    randomNumber = createDynamicRandomNumber(loginConfigData.otpLength!!.toInt())
                }
                if (loginConfigData.otpValidTimeInSeconds != null) {
                    expirationTime = loginConfigData.otpValidTimeInSeconds!!.toLong()
                }
                val currentInstant = Instant.now() // Get the current instant
                val otpValidTill = currentInstant.plusSeconds(expirationTime)

                /* Update OTP and otp valid till in user details table */
                userLoginData.otp = randomNumber
                userLoginData.otpValidTill = otpValidTill
                userDetailsRepository.save(userLoginData)
                /* Sending OTP according the otp sending via configuration. */
                if (loginConfigData.sendOtpVia == SendOTPVia.EMAIL.toString()) {
                    registrationService.sendOTPWithDynamicExpirationTime(userLoginData.emailId, null, randomNumber, expirationTime)
                } else if (userLoginData.mobileNo != null && loginConfigData.sendOtpVia == SendOTPVia.MOBILE.toString()) {
                    val mobileNumber = userLoginData.countryCode + userLoginData.mobileNo
                    registrationService.sendOTPWithDynamicExpirationTime(null, mobileNumber, randomNumber, expirationTime)
                } else {
                    registrationService.sendOTPWithDynamicExpirationTime(
                        userLoginData.emailId,
                        null,
                        randomNumber,
                        expirationTime
                    )
                    if (userLoginData.mobileNo != null) {
                        val mobileNumber = userLoginData.countryCode + userLoginData.mobileNo
                        registrationService.sendOTPWithDynamicExpirationTime(
                            null,
                            mobileNumber,
                            randomNumber,
                            expirationTime
                        )
                    }
                }
            }
        }
        return true
    }

    private fun createDynamicRandomNumber(otpLength: Int): String {
        /* Generating OTP according the otp length configuration. */
        if (otpLength < 4 || otpLength > 10) throw IllegalArgumentException("Length must be at least 1")
        val lowerBound = 10.0.pow((otpLength - 1).toDouble()).toLong()
        val upperBound = 10.0.pow(otpLength.toDouble()).toLong() - 1
        return Random.nextLong(lowerBound, upperBound + 1).toString()
    }

    fun resendOTPByEmail(email: String): Boolean {
        val userData = userDetailsRepository.getByEmailId(email)
        if (userData?.status != null && userData.status == StatusEnum.ACTIVE) {
            val loginConfigData = loginConfigurationService.getConfigDataByAggregatorId(1)
            val loginConfiguration = loginConfigData.toList()[0]
            /*Sending OTP IF TWO FACTOR enabled*/
            return sendOTP(userData, loginConfiguration)
        }
        throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun verifyOTPByEmail(verifyOTPRequest: LoginVerifyOTPRequest): Any {
        val userData = userDetailsRepository.getByEmailId(verifyOTPRequest.emailId)
        if (userData?.status != null && userData.status == StatusEnum.ACTIVE) {
            val loginConfigData = loginConfigurationService.getConfigDataByAggregatorId(1)
            val loginConfiguration = loginConfigData.toList()[0]
            /*Verifying OTP IF TWO FACTOR enabled*/
            return verifyOTP(userData, loginConfiguration, verifyOTPRequest)
        }
        throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun verifyOTP(
        userLoginData: UserDetails,
        loginConfiguration: LoginConfigurationData?,
        verifyOTPRequest: LoginVerifyOTPRequest
    ): Any {
        /* Checking login config data and checking two factor enabled */
        if (loginConfiguration!!.isMultifactorEnable == true) {
            val loginConfigData = loginConfigurationService.getByUserLoginConfigId(loginConfiguration.userLoginConfigId)
            if (loginConfigData != null) {
                val currentInstant = Instant.now() // Get the current instant
                val otpValidTill = userLoginData.otpValidTill
                if (otpValidTill != null && otpValidTill <= currentInstant) {
                    return ResponseEntity.status(HttpStatus.GONE)
                        .body(ErrorResponse(HttpStatus.GONE.value(), ErrorCodes.EXPIRED_OR_ALREADY_USED, "OTP has expired"))
                }
                if (userLoginData.otp != null && verifyOTPRequest.otp != userLoginData.otp) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ErrorCodes.OTP_NOT_MATCH, "OTP does not match"))
                }
                userLoginData.otp = null
                userLoginData.otpValidTill = null
                userDetailsRepository.save(userLoginData)
                /* Verifying OTP according the otp sending via configuration. */
                if (loginConfigData.sendOtpVia == SendOTPVia.EMAIL.toString()) {
                    registrationService.verifyOTP(userLoginData.emailId, verifyOTPRequest.otp, null, null)
                } else if (userLoginData.mobileNo != null && loginConfigData.sendOtpVia == SendOTPVia.MOBILE.toString()) {
                    val mobileNumber = userLoginData.countryCode + userLoginData.mobileNo
                    registrationService.verifyOTP(null, null, mobileNumber, verifyOTPRequest.otp)
                } else {
                    registrationService.verifyOTP(
                        userLoginData.emailId,
                        verifyOTPRequest.otp,
                        null,
                        null
                    )
                    if (userLoginData.mobileNo != null) {
                        val mobileNumber = userLoginData.countryCode + userLoginData.mobileNo
                        registrationService.verifyOTP(
                            null,
                            null,
                            mobileNumber,
                            verifyOTPRequest.otp
                        )
                    }
                }
            }
        }
        return true
    }
}
