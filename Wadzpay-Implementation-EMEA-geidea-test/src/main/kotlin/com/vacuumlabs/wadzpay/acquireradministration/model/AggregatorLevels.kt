package com.vacuumlabs.wadzpay.acquireradministration.model

data class AggregatorLevels(
    val aggregator: List<Aggregator>,
    val institution: List<Institution>,
    val merchantGroup: List<MerchantGroup>,
    val merchantAcquirer: List<MerchantAcquirer>,
    val subMerchantAcquirer: List<SubMerchantAcquirer>,
    val outlet: List<Outlet>

)
