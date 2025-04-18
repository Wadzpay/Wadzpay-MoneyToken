package com.vacuumlabs.wadzpay.usermanagement.dataclass

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** AllRequest and response data class for the department master.*/
@JsonIgnoreProperties(ignoreUnknown = true)
data class DepartmentRequest(
    val departmentName: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DepartmentData(
    val departmentId: Int,
    val departmentName: String,
    val status: Boolean?
)

enum class SendOTPVia() {
    EMAIL,
    MOBILE,
    BOTH
}
