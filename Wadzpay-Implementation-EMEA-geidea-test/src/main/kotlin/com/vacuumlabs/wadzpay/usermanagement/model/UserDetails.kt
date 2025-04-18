package com.vacuumlabs.wadzpay.usermanagement.model

import com.vacuumlabs.wadzpay.rolemanagement.model.Role
import com.vacuumlabs.wadzpay.usermanagement.dataclass.LoginConfigurationData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.StatusEnum
import org.springframework.data.jpa.repository.Query
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
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "user_details")
data class UserDetails(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long = 0,
    var name: String,
    var userPreferenceId: String,
    var countryCode: String?,
    var mobileNo: String?,
    var emailId: String,
    var designation: String?,
    @ManyToOne
    @JoinColumn(name = "department_id")
    var departmentId: Department?,
    @ManyToOne
    @JoinColumn(name = "role_id")
    var roleId: Role?,
    @Column
    @Enumerated(EnumType.STRING)
    var status: StatusEnum,
    var lastSuccessLogin: Instant? = null,
    var password: String?,
    var failedAttempts: Int = 0,
    @ManyToOne
    @JoinColumn(name = "created_by")
    var createdBy: UserDetails?,
    var createdAt: Instant? = null,
    @ManyToOne
    @JoinColumn(name = "updated_by")
    var updatedBy: UserDetails?,
    var updatedAt: Instant? = null,

    var roleFromUserId: Long?,
    var passwordUuid: String? = null,
    var otp: String? = null,
    var otpValidTill: Instant? = null
)
fun UserDetails.toViewModelUserDetails(): UserDetailsDataViewModel {
    return UserDetailsDataViewModel(
        userId,
        name,
        userPreferenceId,
        emailId,
        countryCode,
        mobileNo,
        designation,
        departmentId?.departmentId,
        departmentId?.departmentName,
        roleId?.roleId,
        roleId?.roleName,
        lastSuccessLogin,
        status.statusType,
        null
    )
}
data class UserDetailsDataViewModel(
    val userId: Long,
    val userName: String,
    val userPreferenceId: String?,
    val userEmail: String?,
    val countryCode: String?,
    val userMobile: String?,
    val designation: String?,
    val departmentId: Int?,
    val department: String?,
    val assignedRoleId: Int?,
    val assignedRole: String?,
    val lastActiveAt: Instant?,
    val status: String?,
    var loginConfiguration: LoginConfigurationData?
)
@Repository
interface UserDetailsRepository : CrudRepository<UserDetails, Long> {
    fun getByEmailIdIgnoreCase(emailId: String): List<UserDetails>?

    fun getByMobileNo(mobileNo: String?): List<UserDetails>?

    fun getByUserPreferenceId(userPreferenceId: String): List<UserDetails>?

    fun getByUserId(userId: Long): UserDetails?

    fun getByRoleId(role: Role): List<UserDetails>?
    @Query("from UserDetails u where u.roleId=:roleId and u.status !=:status")
    fun getByRoleIdAndStatus(roleId: Role, status: StatusEnum): List<UserDetails>?

    fun getByEmailId(emailId: String): UserDetails?
    fun getByPasswordUuid(passwordUuid: String): UserDetails?

    fun getUserDetailsByEmailIdAndPasswordAndStatus(emailId: String, password: String, status: StatusEnum): UserDetails?
}
