package com.vacuumlabs.wadzpay.rolemanagement.model

import java.math.BigDecimal
import java.time.Instant

class ModuleDataService {
    data class ModuleDataUpdate(
        var moduleId: Short = 0,
        var moduleName: String?,
        var moduleType: String?,
        var moduleUrl: String?,
        var imageUrl: String?,
        var parent: Short?,
        var activate: Boolean? = null,
        var sorting: BigDecimal = BigDecimal("4"),
        var createdAt: Instant? = Instant.now(),
        var createdBy: Long? = 91,
        var updatedAt: Instant? = Instant.now(),
        var updatedBy: Long? = 91,
        var parentName: String?,
        var status: Boolean?

    )
}
