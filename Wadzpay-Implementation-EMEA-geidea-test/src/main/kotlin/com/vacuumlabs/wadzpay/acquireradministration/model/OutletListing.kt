package com.vacuumlabs.wadzpay.acquireradministration.model

data class OutletListing(
    val outlet: Outlet,
    val subMerchantAcquirer: SubMerchantAcquirer,
    val merchantAcquirer: MerchantAcquirer,
    val merchantGroup: MerchantGroup,
    val institution: Institution,
    val aggregator: Aggregator,
)
