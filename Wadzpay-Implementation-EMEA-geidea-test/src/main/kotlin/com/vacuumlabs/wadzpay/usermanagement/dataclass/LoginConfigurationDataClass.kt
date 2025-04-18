package com.vacuumlabs.wadzpay.usermanagement.dataclass

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** AllRequest and response data class for the User Login and configuration.*/
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginConfigurationData(
    val userLoginConfigId: Long = 0,
    val aggregatorId: Long? = null,
    val noOfFailedLoginAttempts: Short? = null,
    val otpValidTimeInSeconds: Short? = null,
    val otpLength: Short? = null,
    val resendOtpLinkInSeconds: Short? = null,
    val resendOtpNoOfTimes: Short? = null,
    val sendOtpVia: String? = null,
    val isMultifactorEnable: Boolean? = null,
    val status: Boolean = true,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateLoginConfigurationRequest(
    val aggregatorId: Long,
    val noOfFailedLoginAttempts: Short? = null,
    val otpValidTimeInSeconds: Short? = null,
    val otpLength: Short? = null,
    val resendOtpLinkInSeconds: Short? = null,
    val resendOtpNoOfTimes: Short? = null,
    val sendOtpVia: SendOTPVia = SendOTPVia.EMAIL,
    val isMultifactorEnable: Boolean? = null,
    val status: Boolean = true,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateLoginConfigurationRequest(
    val userLoginConfigId: Long = 0,
    val aggregatorId: Long? = null,
    val noOfFailedLoginAttempts: Short? = null,
    val otpValidTimeInSeconds: Short? = null,
    val otpLength: Short? = null,
    val resendOtpLinkInSeconds: Short? = null,
    val resendOtpNoOfTimes: Short? = null,
    val sendOtpVia: SendOTPVia = SendOTPVia.EMAIL,
    val isMultifactorEnable: Boolean? = null,
    val status: Boolean = true,
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserLoginConfiguration(
    val otpLength: Short? = null,
    val resendOtpLinkInSeconds: Short? = null,
    val resendOtpNoOfTimes: Short? = null,
    val sendOtpVia: String? = null,
    val isMultifactorEnable: Boolean? = null
)
