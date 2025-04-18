package com.vacuumlabs.wadzpay.usermanagement.service

import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.usermanagement.dataclass.CreateLoginConfigurationRequest
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginConfigurationData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.UpdateLoginConfigurationRequest
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetails
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserLoginConfig
import com.vacuumlabs.wadzpay.usermanagement.model.UserLoginConfigRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserLoginConfigTransaction
import com.vacuumlabs.wadzpay.usermanagement.model.UserLoginConfigTransactionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class LoginConfigurationService(
    val userLoginConfigRepository: UserLoginConfigRepository,
    val userLoginConfigTransactionRepository: UserLoginConfigTransactionRepository,
    val userDetailsRepository: UserDetailsRepository
) {
    /** Create a new department in the database, or throw a [DuplicateEntityException] if it already exists.
     *  Check for existence before update the department, if department id not exist then send error code [NoSuchElementException]
     *  Fetch all department with active status and send back as mutable list.
     */

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getConfigDataByAggregatorId(aggregatorId: Long): MutableIterable<LoginConfigurationData> {
        val loginConfigurationData = mutableListOf<LoginConfigurationData>()
        userLoginConfigRepository.getByAggregatorId(aggregatorId)?.forEach { data ->
            loginConfigurationData.add(
                setLoginConfigResponse(data)
            )
        }
        return loginConfigurationData
    }

    private fun setLoginConfigResponse(userLoginConfig: UserLoginConfig): LoginConfigurationData {
        return LoginConfigurationData(
            userLoginConfigId = userLoginConfig.userLoginConfigId,
            aggregatorId = userLoginConfig.aggregatorId,
            noOfFailedLoginAttempts = userLoginConfig.noOfFailedLoginAttempts,
            otpValidTimeInSeconds = userLoginConfig.otpValidTimeInSeconds,
            otpLength = userLoginConfig.otpLength,
            resendOtpLinkInSeconds = userLoginConfig.resendOtpLinkInSeconds,
            resendOtpNoOfTimes = userLoginConfig.resendOtpNoOfTimes,
            sendOtpVia = userLoginConfig.sendOtpVia,
            isMultifactorEnable = userLoginConfig.isMultifactorEnable,
            status = userLoginConfig.status
        )
    }

    fun createLoginConfiguration(userAccount: UserAccount, loginConfigurationRequest: CreateLoginConfigurationRequest): LoginConfigurationData {
        val configAlreadyExist = userLoginConfigRepository.getByAggregatorId(loginConfigurationRequest.aggregatorId)
        if (!configAlreadyExist.isNullOrEmpty()) {
            throw DuplicateEntityException("Login Configuration already exist for ${loginConfigurationRequest.aggregatorId}")
        }
        if (loginConfigurationRequest.otpLength != null && (loginConfigurationRequest.otpLength.toInt() < 4 || loginConfigurationRequest.otpLength.toInt() > 10)) throw IllegalArgumentException("Length must be between 4 to 10")
        var lastUserDetails: UserDetails? = null
        if (userDetailsRepository.count() > 0) {
            lastUserDetails = userDetailsRepository.findAll().toList().last()
        }
        val userLoginConfig = UserLoginConfig(
            aggregatorId = loginConfigurationRequest.aggregatorId,
            noOfFailedLoginAttempts = loginConfigurationRequest.noOfFailedLoginAttempts,
            otpValidTimeInSeconds = loginConfigurationRequest.otpValidTimeInSeconds,
            otpLength = loginConfigurationRequest.otpLength,
            resendOtpLinkInSeconds = loginConfigurationRequest.resendOtpLinkInSeconds,
            resendOtpNoOfTimes = loginConfigurationRequest.resendOtpNoOfTimes,
            sendOtpVia = loginConfigurationRequest.sendOtpVia.toString(),
            isMultifactorEnable = loginConfigurationRequest.isMultifactorEnable,
            status = loginConfigurationRequest.status,
            createdBy = lastUserDetails?.userId,
            updatedBy = lastUserDetails?.userId,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val userLoginConfigSaved = userLoginConfigRepository.save(userLoginConfig)
        createLoginConfigTransaction(userLoginConfigSaved)
        return setLoginConfigResponse(userLoginConfigSaved)
    }

    private fun createLoginConfigTransaction(userLoginConfig: UserLoginConfig) {
        val userLoginConfigTransaction = UserLoginConfigTransaction(
            userLoginConfigId = userLoginConfig.userLoginConfigId,
            createdUpdatedAt = userLoginConfig.updatedAt,
            createdUpdatedBy = userLoginConfig.updatedBy,
            status = userLoginConfig.status,
            aggregatorId = userLoginConfig.aggregatorId,
            noOfFailedLoginAttempts = userLoginConfig.noOfFailedLoginAttempts,
            otpValidTimeInSeconds = userLoginConfig.otpValidTimeInSeconds,
            otpLength = userLoginConfig.otpLength,
            resendOtpLinkInSeconds = userLoginConfig.resendOtpLinkInSeconds,
            resendOtpNoOfTimes = userLoginConfig.resendOtpNoOfTimes,
            sendOtpVia = userLoginConfig.sendOtpVia.toString(),
            isMultifactorEnable = userLoginConfig.isMultifactorEnable
        )
        userLoginConfigTransactionRepository.save(userLoginConfigTransaction)
    }

    fun updateLoginConfiguration(userAccount: UserAccount, updateLoginConfigurationRequest: UpdateLoginConfigurationRequest): LoginConfigurationData {
        val userLoginConfigDb = userLoginConfigRepository.getByUserLoginConfigId(updateLoginConfigurationRequest.userLoginConfigId)
        try {
            var lastUserDetails: UserDetails? = null
            if (userDetailsRepository.count() > 0) {
                lastUserDetails = userDetailsRepository.findAll().toList().last()
            }
            if (userLoginConfigDb != null) {
                userLoginConfigDb.noOfFailedLoginAttempts = updateLoginConfigurationRequest.noOfFailedLoginAttempts
                userLoginConfigDb.otpValidTimeInSeconds = updateLoginConfigurationRequest.otpValidTimeInSeconds
                userLoginConfigDb.otpLength = updateLoginConfigurationRequest.otpLength
                userLoginConfigDb.resendOtpLinkInSeconds = updateLoginConfigurationRequest.resendOtpLinkInSeconds
                userLoginConfigDb.resendOtpNoOfTimes = updateLoginConfigurationRequest.resendOtpNoOfTimes
                userLoginConfigDb.sendOtpVia = updateLoginConfigurationRequest.sendOtpVia.toString()
                userLoginConfigDb.isMultifactorEnable = updateLoginConfigurationRequest.isMultifactorEnable
                userLoginConfigDb.status = updateLoginConfigurationRequest.status
                userLoginConfigDb.updatedAt = Instant.now()
                userLoginConfigDb.updatedBy = lastUserDetails?.userId
                val userLoginConfigSaved = userLoginConfigRepository.save(userLoginConfigDb)
                createLoginConfigTransaction(userLoginConfigSaved)
                return setLoginConfigResponse(userLoginConfigSaved)
            }
            throw NoSuchElementException("No Configuration found with Id ${updateLoginConfigurationRequest.userLoginConfigId}")
        } catch (ex: NoSuchElementException) {
            throw NoSuchElementException("No Configuration found with Id ${updateLoginConfigurationRequest.userLoginConfigId}")
        }
    }

    fun getByUserLoginConfigId(userLoginConfigId: Long): UserLoginConfig? {
        return userLoginConfigRepository.getByUserLoginConfigId(userLoginConfigId)
    }
}
