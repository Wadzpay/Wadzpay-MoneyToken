package com.vacuumlabs.wadzpay.acquireradministration.model

import javax.validation.ConstraintViolation

data class CommonValidator(
    val violationsContact: Set<ConstraintViolation<EntityContactDetails>>,
    val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>>,
    val violationsBank: Set<ConstraintViolation<EntityBankDetails>>,
    val violationsAddress: Set<ConstraintViolation<EntityAddress>>,
    val violationsInfo: Set<ConstraintViolation<EntityInfo>>
)
