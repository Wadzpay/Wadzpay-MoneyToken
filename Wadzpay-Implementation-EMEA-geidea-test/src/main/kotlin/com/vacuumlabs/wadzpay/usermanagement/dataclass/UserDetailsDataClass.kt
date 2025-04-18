package com.vacuumlabs.wadzpay.usermanagement.dataclass

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleDataService
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsViewModel
import java.time.Instant
import java.util.Date
import javax.validation.constraints.PositiveOrZero

/** AllRequest and response data class for the users details.*/

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetUserDetailsListRequest(
    val currentLevel: Short? = null,
    val fromDate: Date? = null,
    var toDate: Date? = null,
    var userName: String? = null,
    var userPreferenceId: String? = null,
    var userEmail: String? = null,
    var assignedRole: String? = null,
    var requestedBy: String? = null,
    var actionBy: String? = null,
    val updatedAt: Instant? = null,
    val lastActiveAt: Instant? = null,
    var status: String? = null,
    val limit: Long = 10,
    val duration: Long = 0,
    @PositiveOrZero
    val page: Long? = null,
    val sortField: String? = null,
    val sortOrder: String? = null
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDetailsRequest(
    val currentLevel: Short,
    val userName: String,
    val userPreferenceId: String,
    var countryCode: String?,
    var mobileNo: String?,
    val emailId: String,
    val designation: String?,
    val departmentId: Int?,
    val roleId: Int,
    val comment: String?,
    val roleFromUserId: Long?
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class EditUserDetailsRequest(
    val userId: Long,
    val userName: String?,
    val userPreferenceId: String?,
    var countryCode: String?,
    var mobileNo: String?,
    val emailId: String?,
    val designation: String?,
    val departmentId: Int?,
    val roleId: Int?,
    val comment: String?,
    val roleFromUserId: Long?
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDetailsResponse(
    val userId: Long,
    val status: String?
)

data class UserDetailsDataResponse(
    val totalCount: Int? = 0,
    val userDetailList: List<UserDetailsViewModel>? = null,
    val pagination: RoleDataService.Pagination? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeactivateUserDetailsRequest(
    val userId: Long,
    val comment: String?
)
