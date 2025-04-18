package com.vacuumlabs.wadzpay.acquireradministration.model

data class PosListing(
    val pos: Pos,
    val outlet: Outlet,
    val subMerchantAcquirer: SubMerchantAcquirer,
    val merchantAcquirer: MerchantAcquirer,
    val merchantGroup: MerchantGroup,
    val institution: Institution,
    val aggregator: Aggregator,
)
