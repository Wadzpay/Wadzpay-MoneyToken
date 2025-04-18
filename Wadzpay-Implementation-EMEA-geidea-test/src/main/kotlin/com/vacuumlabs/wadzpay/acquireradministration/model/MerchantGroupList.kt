package com.vacuumlabs.wadzpay.acquireradministration.model

class MerchantGroupList {
    var merchantGroupName: String? = null
    var merchantGroupPreferenceId: String? = null
    var merchantGroupStatus: String? = null
    var merchantGroupImage: String? = null
    var systemGenerated: Boolean? = false
    var merchants: List<MarchantAcquirerList> = ArrayList<MarchantAcquirerList>()
}
