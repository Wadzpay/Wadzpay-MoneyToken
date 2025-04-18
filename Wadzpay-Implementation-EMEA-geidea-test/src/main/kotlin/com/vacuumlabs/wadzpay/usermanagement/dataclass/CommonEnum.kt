package com.vacuumlabs.wadzpay.usermanagement.dataclass

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommonEnum(
    val departmentName: String
)
const val ADD_COMMENT = "Add User"
enum class StatusEnum(val statusType: String) {
    PENDING_APPROVAL("Pending Approval"),
    PENDING_ACTIVATION("Pending Activation"),
    ACTIVE("Active"),
    LOCKED("Locked"),
    REFERRED("Referred"),
    REJECTED("Rejected"),
    DEACTIVATED("Deactivate")
}
