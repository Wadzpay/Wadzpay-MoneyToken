package com.vacuumlabs.wadzpay.acquireradministration.model

data class MerchantAcquirerListing(
    val merchantAcquirer: MerchantAcquirer,
    val merchantGroup: MerchantGroup,
    val institution: Institution,
    val aggregator: Aggregator,

)
