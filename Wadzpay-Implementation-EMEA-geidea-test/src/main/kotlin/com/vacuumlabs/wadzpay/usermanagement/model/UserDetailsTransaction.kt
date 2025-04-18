package com.vacuumlabs.wadzpay.usermanagement.model

import com.vacuumlabs.wadzpay.usermanagement.dataclass.StatusEnum
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "user_details_transaction")
data class UserDetailsTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userDetailsTransactionId: Long = 0,
    var userId: Long?,
    var name: String,
    var userPreferenceId: String,
    var countryCode: String?,
    var mobileNo: String?,
    var emailId: String,
    var designation: String?,
    var departmentId: Int?,
    var roleId: Int?,
    @Column
    @Enumerated(EnumType.STRING)
    var status: StatusEnum,
    var lastSuccessLogin: Instant? = null,
    var password: String?,
    var failedAttempts: Int = 0,
    var createdUpdatedBy: Long?,
    var createdUpdatedAt: Instant = Instant.now(),
    var roleFromUserId: Long?,
    val passwordUuid: String? = null,
    val otp: String? = null,
    val otpValidTill: Instant? = null
)
@Repository
interface UserDetailsTransactionRepository : CrudRepository<UserDetailsTransaction, Long>
