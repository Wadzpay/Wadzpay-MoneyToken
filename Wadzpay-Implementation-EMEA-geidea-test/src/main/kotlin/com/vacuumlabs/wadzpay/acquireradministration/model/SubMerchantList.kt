package com.vacuumlabs.wadzpay.acquireradministration.model
class SubMerchantList {
    var subMerchantAcquirerName: String? = null
    var subMerchantAcquirerId: String? = null
    var subMerchantAcquirerStatus: String? = null
    var systemGenerated: Boolean? = false
    var outlets: List<OutletList> = ArrayList<OutletList>()
}
