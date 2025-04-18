package com.vacuumlabs.wadzpay.acquireradministration.model

import javax.validation.Valid
import javax.validation.constraints.PositiveOrZero

class AcquirerDataService {

    data class AggregatorDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val aggregatorName: String ? = null,
        val aggregatorId: String ? = null,
        val aggregatorStatus: String ? = null,
        val limit: Long = 10,
        val duration: Long = 0
    )

    data class AggregatorDataResponse(
        val totalCount: Int? = 0,
        val aggregatorList: List<Aggregator>? = null,
        val pagination: Pagination? = null
    )

    data class InstitutionDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val institutionName: String ? = null,
        val institutionId: String ? = null,
        val insitutionStatus: String ? = null,
        val aggregatorPreferenceId: String ? = null,
        val limit: Long = 10,
        val duration: Long = 0

    )

    data class InstitutionDataResponse(
        val totalCount: Int? = 0,
        val institutionList: List<Institution>? = null,
        val pagination: Pagination? = null
    )

    data class MerchantGroupDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val merchantGroupName: String ? = null,
        val merchantGroupId: String ? = null,
        val status: String ? = null,
        var aggregatorPreferenceId: String? = null,
        var institutionPreferenceId: String? = null,
        val limit: Long = 10,
        val duration: Long = 0

    )

    data class MerchantGroupDataResponseold(
        val totalCount: Int? = 0,
        val merchantGroupList: List<MerchantGroup>? = null,
        val pagination: Pagination? = null
    )
    data class MerchantGroupDataResponse(
        val totalCount: Int? = 0,
        val merchantGroupList: List<MerchantGroupListing>? = null,
        val pagination: Pagination? = null,
    )
    data class MerchantDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val merchantName: String ? = null,
        val merchantId: String ? = null,
        val status: String ? = null,
        var aggregatorPreferenceId: String? = null,
        var institutionPreferenceId: String? = null,
        var merchantGroupPreferenceId: String? = null,
        val limit: Long = 10,
        val duration: Long = 0
    )

    data class MerchantDataResponseold(
        val totalCount: Int? = 0,
        val merchantList: List<MerchantAcquirer>? = null,
        val pagination: Pagination? = null
    )
    data class MerchantDataResponse(
        val totalCount: Int? = 0,
        val merchantList: List<MerchantAcquirerListing>? = null,
        val pagination: Pagination? = null
    )

    data class SubMerchantDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val subMerchantName: String ? = null,
        var aggregatorPreferenceId: String? = null,
        var institutionPreferenceId: String? = null,
        var merchantGroupPreferenceId: String? = null,
        var merchantAcquirerPreferenceId: String? = null,
        var subMerchantAcquirerId: String? = null,
        var status: String? = null,
        val limit: Long = 10,
        var duration: Long = 0
    )

    data class SubMerchantDataResponseOld(
        val totalCount: Int? = 0,
        val merchantList: List<SubMerchantAcquirer>? = null,
        val pagination: Pagination? = null
    )
    data class SubMerchantDataResponse(
        val totalCount: Int? = 0,
        val merchantList: List<SubMerchantAcquirerListing>? = null,
        val pagination: Pagination? = null
    )
    data class OutletDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val outletName: String ? = null,
        var aggregatorPreferenceId: String? = null,
        var institutionPreferenceId: String? = null,
        var merchantGroupPreferenceId: String? = null,
        var merchantAcquirerPreferenceId: String? = null,
        var subMerchantPreferenceId: String? = null,
        var outletId: String? = null,
        var status: String? = null,
        val limit: Long = 10,
        val duration: Long = 0
    )
    data class PosDetailsRequest(
        @PositiveOrZero
        val page: Long? = null,
        val outletPreferenceId: String ? = null,
        var aggregatorPreferenceId: String? = null,
        var institutionPreferenceId: String? = null,
        var merchantGroupPreferenceId: String? = null,
        var merchantAcquirerPreferenceId: String? = null,
        var subMerchantPreferenceId: String? = null,
        var posId: String? = null,
        var status: String? = null,
        val limit: Long = 10,
        val duration: Long = 0
    )

    data class OutletDetailsSaveRequest(
        @field:Valid
        var outlet: Outlet,
        val parentType: String?,
        val isDirect: Boolean?,
        val aggregatorName: String ?,
        val instituteName: String ?,
        val merchantGroupName: String ?,
        val merchantAcquirerName: String ?,
        val parentDataAggregator: Aggregator?,
        val parentDataInstitution: Institution?,
        val parentDataMerchantGroup: MerchantGroup?,
        val parentDataMerchantAcquirer: MerchantAcquirer?,
        val parentDataSubMerchant: SubMerchantAcquirer?
    )
    data class SubMerchantDetailsSaveRequest(
        @field:Valid
        var subMerchant: SubMerchantAcquirer,
        val parentType: String?,
        val isDirect: Boolean?,
        val parentDataAggregator: Aggregator?,
        val parentDataInstitution: Institution?,
        val parentDataMerchantGroup: MerchantGroup?,
        val parentDataMerchantAcquirer: MerchantAcquirer?,
    )
    data class MerchantDetailsSaveRequest(
        @field:Valid
        var merchant: MerchantAcquirer,
        val parentType: String?,
        val isDirect: Boolean?,
        val parentDataAggregator: Aggregator?,
        val parentDataInstitution: Institution?,
        val parentDataMerchantGroup: MerchantGroup?,
    )
    data class MerchantGroupDetailsSaveRequest(
        @field:Valid
        var merchantGroup: MerchantGroup,
        val parentType: String?,
        val isDirect: Boolean?,
        val parentDataAggregator: Aggregator?,
        val parentDataInstitution: Institution?,
    )
    data class OutletResponseOld(
        val outletList: List<Outlet>? = null,
        val pagination: Pagination? = null
    )
    data class OutletResponse(
        val totalCount: Int? = 0,
        val outletList: List<OutletListing>? = null,
        val pagination: Pagination? = null
    )
    data class PosResponse(
        val posList: List<PosListing>? = null,
        val pagination: Pagination? = null
    )
    data class Pagination(
        val current_page: Long? = 0,
        val total_records: Int = 0,
        val total_pages: Double = 0.0
    )
}
