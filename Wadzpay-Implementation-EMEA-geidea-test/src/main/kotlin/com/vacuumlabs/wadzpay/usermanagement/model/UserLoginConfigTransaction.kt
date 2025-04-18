package com.vacuumlabs.wadzpay.usermanagement.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
@Entity
@Table(name = "user_login_config_transaction")
data class UserLoginConfigTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userLoginConfigTransactionId: Long = 0,

    val userLoginConfigId: Long = 0,
    val aggregatorId: Long? = null,
    val noOfFailedLoginAttempts: Short? = null,
    val otpValidTimeInSeconds: Short? = null,
    val otpLength: Short? = null,
    val resendOtpLinkInSeconds: Short? = null,
    val resendOtpNoOfTimes: Short? = null,
    val sendOtpVia: String? = null,
    val isMultifactorEnable: Boolean? = null,
    var createdUpdatedBy: Long?,
    var createdUpdatedAt: Instant = Instant.now(),
    val status: Boolean = true,
)
@Repository
interface UserLoginConfigTransactionRepository : CrudRepository<UserLoginConfigTransaction, Long>
