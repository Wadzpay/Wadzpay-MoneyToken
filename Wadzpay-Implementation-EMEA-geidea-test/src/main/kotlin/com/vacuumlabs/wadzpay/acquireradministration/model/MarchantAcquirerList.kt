package com.vacuumlabs.wadzpay.acquireradministration.model

class MarchantAcquirerList {
    var merchantAcquirerName: String? = null
    var merchantAcquirerId: String? = null
    var merchantAcquirerStatus: String? = null
    var systemGenerated: Boolean? = false
    var subMerchants: List<SubMerchantList> = ArrayList<SubMerchantList>()
}
