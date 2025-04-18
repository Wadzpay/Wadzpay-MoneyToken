package com.vacuumlabs.wadzpay.acquireradministration.model

class AggregatorTree {

    var aggregatorPreferenceId: String? = null
    var aggregatorName: String? = null
    var aggregatorStatus: String? = null
    var aggregatorImage: String? = null
    var aggregatorLogo: String? = null
    var institutions: List<InstitutionList> = ArrayList<InstitutionList>()
}
