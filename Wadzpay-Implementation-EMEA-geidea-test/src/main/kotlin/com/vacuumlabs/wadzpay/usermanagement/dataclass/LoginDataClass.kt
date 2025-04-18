package com.vacuumlabs.wadzpay.usermanagement.dataclass

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

class LoginDataClass

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginRequest(
    val emailId: String,
    val password: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SetPasswordRequest(
    val userUUID: String,
    val password: String
)

data class ErrorResponse(
    val status: Number,
    val message: String,
    val attemptRemaining: String?
)

data class LoginUserDetailsData(
    val userEmail: String?,
    val userMobile: String?,
    var loginConfiguration: UserLoginConfiguration?,
    val token: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginVerifyOTPRequest(
    val emailId: String,
    val otp: String
)
