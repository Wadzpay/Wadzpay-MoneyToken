package com.vacuumlabs.wadzpay.rolemanagement.model

data class RoleListing(
    val role: Role,
    val roleModule: RoleModule,
    val count: Long
)

data class RoleListingV2(
    val role: Role,
    val roleModule: RoleModule,
    val users: Long
)
data class RoleListingModule(
    var userId: Long?,
    var userPreferenceId: String?,
    var userName: String?,
    val roleId: Int?,
    val roleName: String?,
    var roleModuleList: List<ModuleData>?
)

data class ModuleData(
    val moduleName: String?,
    val moduleId: Short
)
