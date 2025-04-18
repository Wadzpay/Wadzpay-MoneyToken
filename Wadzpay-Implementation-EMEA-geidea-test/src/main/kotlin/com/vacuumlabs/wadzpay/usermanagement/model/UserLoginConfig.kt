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
@Table(name = "user_login_config")
data class UserLoginConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userLoginConfigId: Long = 0,
    val aggregatorId: Long? = null,
    var noOfFailedLoginAttempts: Short? = null,
    var otpValidTimeInSeconds: Short? = null,
    var otpLength: Short? = null,
    var resendOtpLinkInSeconds: Short? = null,
    var resendOtpNoOfTimes: Short? = null,
    var sendOtpVia: String? = null,
    var isMultifactorEnable: Boolean? = null,
    var createdBy: Long?,
    var createdAt: Instant = Instant.now(),
    var updatedBy: Long?,
    var updatedAt: Instant = Instant.now(),
    var status: Boolean = true,
)
@Repository
interface UserLoginConfigRepository : CrudRepository<UserLoginConfig, Long> {
    fun getByAggregatorId(aggregatorId: Long): List<UserLoginConfig>?

    fun getByUserLoginConfigId(userLoginConfigId: Long): UserLoginConfig?
}
