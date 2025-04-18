package com.vacuumlabs.wadzpay.acquireradministration.model

class InstitutionList {
    var institutionName: String? = null
    var institutionPreferenceId: String? = null
    var institutionId: String? = null
    var institutionStatus: String? = null
    var institutionImage: String? = null
    var institutionLogo: String? = null
    var systemGenerated: Boolean? = false
    var merchantGroup: List<MerchantGroupList> = ArrayList<MerchantGroupList>()
}
