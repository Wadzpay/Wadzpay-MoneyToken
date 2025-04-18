package com.vacuumlabs.wadzpay.auth

enum class Role {
    USER,
    ADMIN,
    MERCHANT,
    MERCHANT_ADMIN,
    MERCHANT_READER,
    WADZPAY_ADMIN,
    MERCHANT_MERCHANT,
    MERCHANT_SUPERVISOR,
    MERCHANT_POSOPERATOR,
    ISSUANCE_ADMIN,
    ISSUANCE_USER;

    fun toAuthority(): String = "ROLE_${this.name}"

    fun isInvitable(): Boolean {
        return this == MERCHANT_ADMIN || this == MERCHANT_READER || this == MERCHANT_MERCHANT || this == MERCHANT_SUPERVISOR || this == MERCHANT_POSOPERATOR || this == ISSUANCE_USER || this == ISSUANCE_ADMIN
    }
}
