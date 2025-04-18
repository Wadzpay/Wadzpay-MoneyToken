package com.vacuumlabs.wadzpay.acquireradministration.model

data class SubMerchantAcquirerListing(
    val subMerchantAcquirer: SubMerchantAcquirer,
    val merchantAcquirer: MerchantAcquirer,
    val merchantGroup: MerchantGroup,
    val institution: Institution,
    val aggregator: Aggregator,
)
