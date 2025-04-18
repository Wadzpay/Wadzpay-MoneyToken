package com.vacuumlabs.wadzpay.acquireradministration
import com.vacuumlabs.wadzpay.acquireradministration.dto.OutletDTO
import com.vacuumlabs.wadzpay.acquireradministration.model.AcquirerDataService
import com.vacuumlabs.wadzpay.acquireradministration.model.Aggregator
import com.vacuumlabs.wadzpay.acquireradministration.model.AggregatorRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.AggregatorTree
import com.vacuumlabs.wadzpay.acquireradministration.model.CommonValidator
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityAddress
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityAddressRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityAdminDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityAdminDetailsRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityBankDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityBankDetailsRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityContactDetails
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityContactDetailsRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityInfo
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityInfoRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityOthers
import com.vacuumlabs.wadzpay.acquireradministration.model.EntityOthersRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.Institution
import com.vacuumlabs.wadzpay.acquireradministration.model.InstitutionList
import com.vacuumlabs.wadzpay.acquireradministration.model.InstitutionRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.MarchantAcquirerList
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirer
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirerListing
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirerRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroup
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroupList
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroupListing
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroupRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.Outlet
import com.vacuumlabs.wadzpay.acquireradministration.model.OutletList
import com.vacuumlabs.wadzpay.acquireradministration.model.OutletListing
import com.vacuumlabs.wadzpay.acquireradministration.model.OutletRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.Pos
import com.vacuumlabs.wadzpay.acquireradministration.model.PosList
import com.vacuumlabs.wadzpay.acquireradministration.model.PosListing
import com.vacuumlabs.wadzpay.acquireradministration.model.PosRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantAcquirer
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantAcquirerListing
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantList
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantRepository
import com.vacuumlabs.wadzpay.acquireradministration.model.Violation
import com.vacuumlabs.wadzpay.common.ErroValidationResponse
import com.vacuumlabs.wadzpay.services.AcquirerCsvServcive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.StringJoiner
import java.util.UUID
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.Validator
import kotlin.math.ceil
import kotlin.streams.toList

@Service
class AcquirerAdministrationService(
    val aggregatorRepository: AggregatorRepository,
    val institutionRepository: InstitutionRepository,
    val merchantGroupRepository: MerchantGroupRepository,
    val merchantAcquirerRepository: MerchantAcquirerRepository,
    val subMerchantRepository: SubMerchantRepository,
    val outletRepository: OutletRepository,
    val posRepository: PosRepository,
    val entityOthersRepository: EntityOthersRepository,
    val entityBankDetailsRepository: EntityBankDetailsRepository,
    val entityContactDetailsRepository: EntityContactDetailsRepository,
    val entityAddressRepository: EntityAddressRepository,
    val entityInfoRepository: EntityInfoRepository,
    val entityAdminDetailsRepository: EntityAdminDetailsRepository,
    private val entityManager: EntityManager,
    private val acquirerCvService: AcquirerCsvServcive,
    private val dtoService: DTOEntityConverterService,
    // private val validator: ConstraintValidator<Outlet>
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    //    Aggregator List
    fun getAggregatorList(): MutableList<Aggregator>? {
        println("getAggregatorList")
        var resultAggregatorTableData: MutableList<Aggregator> = mutableListOf()
        resultAggregatorTableData = aggregatorRepository.findAll() as MutableList<Aggregator>
        println(resultAggregatorTableData + "")
        println(resultAggregatorTableData.size.toString() + "")
        return resultAggregatorTableData
    }

    fun fetchAggregatorsToViewModels(
        aggregatorDetailsRequest: AcquirerDataService.AggregatorDetailsRequest
    ): AcquirerDataService.AggregatorDataResponse {
        var aggregatorListData = aggregatorRepository.findAll() as List<Aggregator>
        if (aggregatorDetailsRequest.aggregatorName != null) {
            aggregatorListData = aggregatorListData.filter { e ->
                e.aggregatorName?.contains(aggregatorDetailsRequest.aggregatorName, ignoreCase = true) ?: false
            }
        }

        if (aggregatorDetailsRequest.aggregatorId != null) {
            aggregatorListData = aggregatorListData.filter { e ->
                e.aggregatorPreferenceId?.contains(aggregatorDetailsRequest.aggregatorId, ignoreCase = true) ?: false
            }
        }

        if (aggregatorDetailsRequest.aggregatorStatus != null) {
            aggregatorListData = aggregatorListData.filter { e ->
                e.aggregatorStatus?.contains(aggregatorDetailsRequest.aggregatorStatus, ignoreCase = true) ?: false
            }
        }
        if (aggregatorDetailsRequest.duration> 0) {
            val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            aggregatorListData = aggregatorListData.filter { e ->

                e.others.entityOthersActivationDate != null && LocalDate.parse(e.others.entityOthersActivationDate, dateFormatterDb) > (
                    LocalDate.now()
                        .minusDays(aggregatorDetailsRequest.duration)
                    )
            }
        }
        aggregatorListData = aggregatorListData.sortedBy { list -> list.aggregatorPreferenceId }
        var pagination = AcquirerDataService.Pagination(
            current_page = aggregatorDetailsRequest.page,
            total_records = aggregatorListData.size,
            total_pages = calculateTotalNoPages(
                aggregatorListData.size.toDouble(),
                if (aggregatorDetailsRequest.limit > 0) aggregatorDetailsRequest.limit.toDouble() else aggregatorListData.size.toDouble()
            )
        )

        if (aggregatorDetailsRequest.page != null && aggregatorDetailsRequest.page > 0) {
            val pageNo = aggregatorDetailsRequest.page - 1
            aggregatorListData =
                aggregatorListData.stream().skip(pageNo * aggregatorDetailsRequest.limit)
                    .limit(aggregatorDetailsRequest.limit).toList()
        }

        return AcquirerDataService.AggregatorDataResponse(
            fetchAllAggregatorsCount(),
            aggregatorListData,
            pagination
        )
    }

    fun fetchInstitutionsToViewModels(
        institutionDetailsRequest: AcquirerDataService.InstitutionDetailsRequest
    ): AcquirerDataService.InstitutionDataResponse {
        var institutionListWithAggregator: List<Institution> = ArrayList<Institution>()
        if (institutionDetailsRequest.aggregatorPreferenceId != null) {
            institutionListWithAggregator =
                institutionRepository.findByAggregatorPreferenceId(institutionDetailsRequest.aggregatorPreferenceId) as List<Institution>
            if (institutionDetailsRequest.institutionName != null) {
                institutionListWithAggregator = institutionListWithAggregator.filter { e ->
                    e.insitutionName?.contains(institutionDetailsRequest.institutionName, ignoreCase = true) ?: false
                }
            }

            if (institutionDetailsRequest.institutionId != null) {
                institutionListWithAggregator = institutionListWithAggregator.filter { e ->
                    e.institutionId?.contains(institutionDetailsRequest.institutionId, ignoreCase = true) ?: false
                }
            }

            if (institutionDetailsRequest.insitutionStatus != null) {
                institutionListWithAggregator = institutionListWithAggregator.filter { e ->
                    e.insitutionStatus?.contains(institutionDetailsRequest.insitutionStatus, ignoreCase = true) ?: false
                }
            }
            if (institutionDetailsRequest.duration> 0) {
                val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                institutionListWithAggregator = institutionListWithAggregator.filter { e ->
                    e.others.entityOthersActivationDate != null && LocalDate.parse(e.others.entityOthersActivationDate, dateFormatterDb) > (
                        LocalDate.now()
                            .minusDays(institutionDetailsRequest.duration)
                        )
                }
            }
        }
        institutionListWithAggregator = institutionListWithAggregator.sortedBy { list -> list.institutionId }
        var pagination = AcquirerDataService.Pagination(
            current_page = institutionDetailsRequest.page,
            total_records = institutionListWithAggregator.size,
            total_pages = calculateTotalNoPages(
                institutionListWithAggregator.size.toDouble(),
                if (institutionDetailsRequest.limit > 0) institutionDetailsRequest.limit.toDouble() else institutionListWithAggregator.size.toDouble()
            )
        )

        if (institutionDetailsRequest.page != null && institutionDetailsRequest.page > 0) {
            val pageNo = institutionDetailsRequest.page - 1
            institutionListWithAggregator =
                institutionListWithAggregator.stream().skip(pageNo * institutionDetailsRequest.limit)
                    .limit(institutionDetailsRequest.limit).toList()
        }

        return AcquirerDataService.InstitutionDataResponse(
            pagination.total_records,
            institutionListWithAggregator,
            pagination
        )
    }

    fun fetchMerchantGroupToViewModels(
        merchantGroupDetailsRequest: AcquirerDataService.MerchantGroupDetailsRequest
    ): AcquirerDataService.MerchantGroupDataResponse {
        var merchantGroupListWithAggregator: List<MerchantGroup> = ArrayList<MerchantGroup>()
        var merchantGroupListWithAggregatorParent: List<MerchantGroupListing> = ArrayList<MerchantGroupListing>()

        if (merchantGroupDetailsRequest.aggregatorPreferenceId != null && merchantGroupDetailsRequest.institutionPreferenceId != null) {
            merchantGroupListWithAggregator = merchantGroupRepository.findByUniqueId(
                merchantGroupDetailsRequest.institutionPreferenceId!!,
                merchantGroupDetailsRequest.aggregatorPreferenceId!!
            ) as List<MerchantGroup>
            merchantGroupListWithAggregatorParent = merchantGroupRepository.findByRecordsWithParent(
                merchantGroupDetailsRequest.institutionPreferenceId!!,
                merchantGroupDetailsRequest.aggregatorPreferenceId!!
            )

            if (merchantGroupDetailsRequest.merchantGroupId != null) {
                merchantGroupListWithAggregatorParent = merchantGroupListWithAggregatorParent.filter { e ->
                    e.merchantGroup.merchantGroupPreferenceId?.contains(
                        merchantGroupDetailsRequest.merchantGroupId,
                        ignoreCase = true
                    ) ?: false
                }.toMutableList()
            }

            if (merchantGroupDetailsRequest.status != null) {
                merchantGroupListWithAggregatorParent = merchantGroupListWithAggregatorParent.filter { e ->
                    e.merchantGroup.merchantGroupStatus?.contains(merchantGroupDetailsRequest.status, ignoreCase = true) ?: false
                }.toMutableList()
            }

            if (merchantGroupDetailsRequest.merchantGroupName != null) {
                merchantGroupListWithAggregatorParent = merchantGroupListWithAggregatorParent.filter { e ->
                    e.merchantGroup.merchantGroupName?.contains(merchantGroupDetailsRequest.merchantGroupName, ignoreCase = true)
                        ?: false
                }.toMutableList()
            }
            if (merchantGroupDetailsRequest.duration> 0) {
                val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                merchantGroupListWithAggregatorParent = merchantGroupListWithAggregatorParent.filter { e ->
                    e.merchantGroup?.others.entityOthersActivationDate != null && LocalDate.parse(
                        e.merchantGroup?.others.entityOthersActivationDate,
                        dateFormatterDb
                    ) > (
                        LocalDate.now()
                            .minusDays(merchantGroupDetailsRequest.duration)
                        )
                }
            }
        }
        merchantGroupListWithAggregatorParent =
            merchantGroupListWithAggregatorParent.sortedBy { list -> list.merchantGroup.merchantGroupPreferenceId } as List<MerchantGroupListing>

        var pagination = AcquirerDataService.Pagination(
            current_page = merchantGroupDetailsRequest.page,
            total_records = merchantGroupListWithAggregatorParent.size,
            total_pages = calculateTotalNoPages(
                merchantGroupListWithAggregatorParent.size.toDouble(),

                if (merchantGroupDetailsRequest.limit > 0) merchantGroupDetailsRequest.limit.toDouble() else merchantGroupListWithAggregator.size.toDouble()
            )
        )

        if (merchantGroupDetailsRequest.page != null && merchantGroupDetailsRequest.page > 0) {
            val pageNo = merchantGroupDetailsRequest.page - 1
            merchantGroupListWithAggregatorParent =
                merchantGroupListWithAggregatorParent.stream().skip(pageNo * merchantGroupDetailsRequest.limit)
                    .limit(merchantGroupDetailsRequest.limit).toList()
        }

        return AcquirerDataService.MerchantGroupDataResponse(
            pagination.total_records,
            merchantGroupListWithAggregatorParent,
            pagination
        )
    }

    fun fetchMerchantsToViewModels(
        merchantDetailsRequest: AcquirerDataService.MerchantDetailsRequest
    ): AcquirerDataService.MerchantDataResponse {
        var merchantListWithAggregator: List<MerchantAcquirerListing> = ArrayList<MerchantAcquirerListing>()
        if (merchantDetailsRequest.aggregatorPreferenceId != null && merchantDetailsRequest.institutionPreferenceId != null && merchantDetailsRequest.merchantGroupPreferenceId != null) {
            merchantListWithAggregator = merchantAcquirerRepository.findByRecordsWithParent(
                merchantDetailsRequest.aggregatorPreferenceId!!,
                merchantDetailsRequest.institutionPreferenceId!!,
                merchantDetailsRequest.merchantGroupPreferenceId!!
            ) as List<MerchantAcquirerListing>

            if (merchantDetailsRequest.merchantName != null) {
                merchantListWithAggregator = merchantListWithAggregator.filter { e ->
                    e.merchantAcquirer.merchantAcquirerName?.contains(merchantDetailsRequest.merchantName, ignoreCase = true) ?: false
                }
            }

            if (merchantDetailsRequest.merchantId != null) {
                merchantListWithAggregator = merchantListWithAggregator.filter { e ->
                    e.merchantAcquirer.merchantAcquirerId?.contains(merchantDetailsRequest.merchantId, ignoreCase = true) ?: false
                }
            }

            if (merchantDetailsRequest.status != null) {
                merchantListWithAggregator = merchantListWithAggregator.filter { e ->
                    e.merchantAcquirer.merchantAcquirerStatus?.contains(merchantDetailsRequest.status, ignoreCase = true) ?: false
                }
            }
            if (merchantDetailsRequest.duration> 0) {
                val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                merchantListWithAggregator = merchantListWithAggregator.filter { e ->
                    e.merchantAcquirer.others.entityOthersActivationDate != null && LocalDate.parse(e.merchantAcquirer.others.entityOthersActivationDate, dateFormatterDb) > (
                        LocalDate.now()
                            .minusDays(merchantDetailsRequest.duration)
                        )
                }
            }
        }
        merchantListWithAggregator = merchantListWithAggregator.sortedBy { list -> list.merchantAcquirer.merchantAcquirerId }
        var pagination = AcquirerDataService.Pagination(
            current_page = merchantDetailsRequest.page,
            total_records = merchantListWithAggregator.size,
            total_pages = calculateTotalNoPages(
                merchantListWithAggregator.size.toDouble(),
                if (merchantDetailsRequest.limit > 0) merchantDetailsRequest.limit.toDouble() else merchantListWithAggregator.size.toDouble(),

            )
        )

        if (merchantDetailsRequest.page != null && merchantDetailsRequest.page > 0) {
            val pageNo = merchantDetailsRequest.page - 1
            merchantListWithAggregator =
                merchantListWithAggregator.stream().skip(pageNo * merchantDetailsRequest.limit)
                    .limit(merchantDetailsRequest.limit).toList()
        }

        return AcquirerDataService.MerchantDataResponse(
            pagination.total_records,
            merchantListWithAggregator,
            pagination
        )
    }

    fun fetchSubMerchantsToViewModels(
        subMerchantDetailsRequest: AcquirerDataService.SubMerchantDetailsRequest
    ): AcquirerDataService.SubMerchantDataResponse {
        if (subMerchantDetailsRequest.aggregatorPreferenceId != null && subMerchantDetailsRequest.institutionPreferenceId != null && subMerchantDetailsRequest.merchantGroupPreferenceId != null && subMerchantDetailsRequest.merchantAcquirerPreferenceId != null) {
            var subMerchantListWithAggregator = subMerchantRepository.findByRecordsWithParent(
                subMerchantDetailsRequest.aggregatorPreferenceId!!,
                subMerchantDetailsRequest.institutionPreferenceId!!,
                subMerchantDetailsRequest.merchantGroupPreferenceId!!,
                subMerchantDetailsRequest.merchantAcquirerPreferenceId!!
            ) as List<SubMerchantAcquirerListing>
            if (subMerchantDetailsRequest.status != null) {
                subMerchantListWithAggregator = subMerchantListWithAggregator.filter { e ->
                    e.subMerchantAcquirer.subMerchantAcquirerStatus?.contains(subMerchantDetailsRequest.status!!, ignoreCase = true)

                        ?: false
                }
            }
            if (subMerchantDetailsRequest.subMerchantAcquirerId != null) {
                subMerchantListWithAggregator = subMerchantListWithAggregator.filter { e ->
                    e.subMerchantAcquirer.subMerchantAcquirerId?.contains(subMerchantDetailsRequest.subMerchantAcquirerId!!, ignoreCase = true)

                        ?: false
                }
            }
            subMerchantListWithAggregator =
                subMerchantListWithAggregator.sortedBy { list -> list.subMerchantAcquirer.subMerchantAcquirerId }
            var pagination = AcquirerDataService.Pagination(
                current_page = subMerchantDetailsRequest.page,
                total_records = subMerchantListWithAggregator.size,
                total_pages = calculateTotalNoPages(
                    subMerchantListWithAggregator.size.toDouble(),
                    if (subMerchantDetailsRequest.limit > 0) subMerchantDetailsRequest.limit.toDouble() else subMerchantListWithAggregator.size.toDouble(),
                )
            )

            if (subMerchantDetailsRequest.page != null && subMerchantDetailsRequest.page > 0) {
                val pageNo = subMerchantDetailsRequest.page - 1
                subMerchantListWithAggregator =
                    subMerchantListWithAggregator.stream().skip(pageNo * subMerchantDetailsRequest.limit)
                        .limit(subMerchantDetailsRequest.limit).toList()
            }
            if (subMerchantDetailsRequest.duration> 0) {
                val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                subMerchantListWithAggregator = subMerchantListWithAggregator.filter { e ->
                    e.subMerchantAcquirer.others.entityOthersActivationDate != null && LocalDate.parse(e.subMerchantAcquirer.others.entityOthersActivationDate, dateFormatterDb) > (
                        LocalDate.now()
                            .minusDays(subMerchantDetailsRequest.duration)
                        )
                }
            }

            return AcquirerDataService.SubMerchantDataResponse(
                pagination.total_records,
                subMerchantListWithAggregator,
                pagination
            )
        }

        var subMerchantsListData = subMerchantRepository.findAll() as List<SubMerchantAcquirerListing>

        if (subMerchantDetailsRequest.subMerchantName != null) {
            subMerchantsListData = subMerchantsListData.filter { e ->
                e.subMerchantAcquirer.subMerchantAcquirerName?.contains(subMerchantDetailsRequest.subMerchantName, ignoreCase = true)
                    ?: false
            }
        }
        if (subMerchantDetailsRequest.status != null) {
            subMerchantsListData = subMerchantsListData.filter { e ->
                e.subMerchantAcquirer.subMerchantAcquirerStatus?.contains(subMerchantDetailsRequest.status!!, ignoreCase = true) ?: false
            }
        }
        subMerchantsListData = subMerchantsListData.sortedBy { list -> list.subMerchantAcquirer.subMerchantAcquirerId }

        var pagination = AcquirerDataService.Pagination(
            current_page = subMerchantDetailsRequest.page,
            total_records = subMerchantsListData.size,
            total_pages = calculateTotalNoPages(
                subMerchantsListData.size.toDouble(),
                if (subMerchantDetailsRequest.limit > 0) subMerchantDetailsRequest.limit.toDouble() else subMerchantsListData.size.toDouble()
            )
        )

        if (subMerchantDetailsRequest.page != null && subMerchantDetailsRequest.page > 0) {
            val pageNo = subMerchantDetailsRequest.page - 1
            subMerchantsListData =
                subMerchantsListData.stream().skip(pageNo * subMerchantDetailsRequest.limit)
                    .limit(subMerchantDetailsRequest.limit).toList()
        }

        return AcquirerDataService.SubMerchantDataResponse(
            pagination.total_records,
            subMerchantsListData,
            pagination
        )
    }

    fun fetchAllAggregatorsCount(): Int? {
        var aggregatorDetailsRequest = AcquirerDataService.AggregatorDetailsRequest(page = null)
        val aggregatorsList = aggregatorRepository.findAll() as List<Aggregator>
        return aggregatorsList.size
    }

    fun fetchAllInstitutionsCount(): Int? {
        var institutionDetailsRequest = AcquirerDataService.InstitutionDetailsRequest(page = null)
        val institutionList = institutionRepository.findAll() as List<Institution>
        return institutionList.size
    }
    fun calculateTotalNoPages(size: Double, limit: Double): Double {
        val totalNoPages = ceil((size / limit).toDouble())
        return if (totalNoPages > 0) {
            totalNoPages
        } else {
            1.0
        }
    }

    fun createAggregator(aggregator: Aggregator) {
        aggregatorRepository.save(aggregator)
    }

    fun createInstitution(institution: Institution) {
        institutionRepository.save(institution)
    }

    fun createSubMerchant(subMerchantDetails: AcquirerDataService.SubMerchantDetailsSaveRequest) {
        if (subMerchantDetails.isDirect!!) {
            if (subMerchantDetails.parentType == "aggregator" && subMerchantDetails.parentDataAggregator is Aggregator) {
                var institutionNew = Institution()
                institutionNew.institutionId = subMerchantDetails.subMerchant.insitutionPreferenceId!!
                institutionNew.systemGenerated = true
                var institution = updateInstitutionDetials(institutionNew, subMerchantDetails.parentDataAggregator)
                val institutionSaved = institutionRepository.findByInstitutionId(subMerchantDetails.subMerchant.insitutionPreferenceId!!)
                if (institutionSaved !is Institution) {
                    institutionRepository.save(institution)
                }
                var merchantGroupNew = MerchantGroup()
                merchantGroupNew.systemGenerated = true
                merchantGroupNew.generatedFrom = "aggregator"
                merchantGroupNew.merchantGroupPreferenceId = subMerchantDetails.subMerchant.merchantGroupPreferenceId
                var merchantGroup = updateMerchantGroupDetials(merchantGroupNew, institution)
                val merchantGroupSaved = merchantGroupRepository.findByMerchantGroupPreferenceId(subMerchantDetails.subMerchant.merchantGroupPreferenceId!!)
                if (merchantGroupSaved !is MerchantGroup) {
                    merchantGroupRepository.save(merchantGroup)
                }
                var merchantNew = MerchantAcquirer()
                merchantNew.merchantAcquirerId = subMerchantDetails.subMerchant.merchantAcquirerPreferenceId
                merchantNew.systemGenerated = true
                merchantNew.generatedFrom = "aggregator"
                var merchant = updateMerchantDetials(merchantNew, merchantGroup)
                val merchantSaved = merchantAcquirerRepository.findByMerchantAcquirerId(subMerchantDetails.subMerchant.merchantAcquirerPreferenceId!!)
                if (merchantSaved !is MerchantAcquirer) {
                    merchantAcquirerRepository.save(merchant)
                }
            } else if (subMerchantDetails.parentType == "institution" && subMerchantDetails.parentDataInstitution is Institution) {
                var merchantGroupNew = MerchantGroup()
                merchantGroupNew.merchantGroupPreferenceId = subMerchantDetails.subMerchant.merchantGroupPreferenceId
                merchantGroupNew.systemGenerated = true
                merchantGroupNew.generatedFrom = "institution"
                var merchantGroup =
                    updateMerchantGroupDetials(merchantGroupNew, subMerchantDetails.parentDataInstitution)
                val merchantGroupSaved = merchantGroupRepository.findByMerchantGroupPreferenceId(subMerchantDetails.subMerchant.merchantGroupPreferenceId!!)
                if (merchantGroupSaved !is MerchantGroup) {
                    merchantGroupRepository.save(merchantGroup)
                }
                var merchantNew = MerchantAcquirer()
                merchantNew.merchantAcquirerId = subMerchantDetails.subMerchant.merchantAcquirerPreferenceId
                merchantNew.systemGenerated = true
                merchantNew.generatedFrom = "institution"
                var merchant =
                    updateMerchantDetials(merchantNew, merchantGroup)
                val merchantSaved = merchantAcquirerRepository.findByMerchantAcquirerId(subMerchantDetails.subMerchant.merchantAcquirerPreferenceId!!)
                if (merchantSaved !is MerchantAcquirer) {
                    merchantAcquirerRepository.save(merchant)
                }
            } else if (subMerchantDetails.parentType == "merchantGroup" && subMerchantDetails.parentDataMerchantGroup is MerchantGroup) {
                var merchantNew = MerchantAcquirer()
                merchantNew.merchantAcquirerId = subMerchantDetails.subMerchant.merchantAcquirerPreferenceId
                merchantNew.systemGenerated = true
                merchantNew.generatedFrom = "merchantGroup"
                var merchant =
                    updateMerchantDetials(merchantNew, subMerchantDetails.parentDataMerchantGroup)
                val merchantSaved = merchantAcquirerRepository.findByMerchantAcquirerId(subMerchantDetails.subMerchant.merchantAcquirerPreferenceId!!)
                if (merchantSaved !is MerchantAcquirer) {
                    merchantAcquirerRepository.save(merchant)
                }
            }
        }
        subMerchantRepository.save(subMerchantDetails.subMerchant)
    }

    @Transactional
    fun createOutlet(outletDetails: AcquirerDataService.OutletDetailsSaveRequest) {
        var outletPersist: Outlet = Outlet(
            outletDetails.outlet.id,
            outletDetails.outlet.aggregatorPreferenceId,
            outletDetails.outlet.insitutionPreferenceId,
            outletDetails.outlet.merchantGroupPreferenceId,
            outletDetails.outlet.merchantAcquirerPreferenceId,
            outletDetails.outlet.subMerchantPreferenceId,
            outletDetails.outlet.outletId,
            outletDetails.outlet.clientOutletId,
            outletDetails.outlet.outletName,
            outletDetails.outlet.outletStatus,
            outletDetails.outlet.outletLogo,
            outletDetails.outlet.isParentBlocked,
            outletDetails.outlet.isParentDeActivated,
            outletDetails.outlet.isParentClosed,
            outletDetails.outlet.address,
            outletDetails.outlet.adminDetails,
            outletDetails.outlet.bankDetails,
            outletDetails.outlet.contactDetails,
            outletDetails.outlet.info,
            outletDetails.outlet.others
        )
        // list of Pos's
        /*        var posList: ArrayList<Pos> = ArrayList<Pos>()
                if (outletDetails.outlet.posList.size> 0) {
                    outletDetails.outlet.posList.forEach() {
                        var pos: Pos = Pos(
                            it.id,
                            it.outletPreferenceId,
                            it.posId,
                            it.posModel,
                            it.posManufacturer,
                            it.posSerialNum,
                            it.posMacAddress,
                            it.posIPAddress,
                            it.posFirmwareVersion,
                            it.status,
                            it.posKey
                        )
                        pos.posKey = it.posKey
                        posList.add(pos)
                    }
                }
                outletPersist.posList = posList*/
        if (outletDetails.isDirect!!) {
            if (outletDetails.parentType == "aggregator" && outletDetails.parentDataAggregator is Aggregator) {
                var institutionNew = Institution()
                institutionNew.institutionId = outletDetails.outlet.insitutionPreferenceId!!
                institutionNew.systemGenerated = true
                var institution = updateInstitutionDetials(institutionNew, outletDetails.parentDataAggregator)
                val institutionSaved = institutionRepository.findByInstitutionId(outletDetails.outlet.insitutionPreferenceId!!)
                if (institutionSaved !is Institution) {
                    institutionRepository.save(institution)
                }
                var merchantGroupNew = MerchantGroup()
                merchantGroupNew.merchantGroupPreferenceId = outletDetails.outlet.merchantGroupPreferenceId
                merchantGroupNew.systemGenerated = true
                merchantGroupNew.generatedFrom = "aggregator"
                var merchantGroup = updateMerchantGroupDetials(merchantGroupNew, institution)
                val merchantGroupSaved = merchantGroupRepository.findByMerchantGroupPreferenceId(outletDetails.outlet.merchantGroupPreferenceId!!)
                if (merchantGroupSaved !is MerchantGroup) {
                    merchantGroupRepository.save(merchantGroup)
                }
                var merchant = MerchantAcquirer()
                merchant.merchantAcquirerId = outletDetails.outlet.merchantAcquirerPreferenceId
                merchant.systemGenerated = true
                merchant.generatedFrom = "aggregator"
                val merchantUpdated = updateMerchantDetials(merchant, merchantGroup)
                val merchantSaved = merchantAcquirerRepository.findByMerchantAcquirerId(outletDetails.outlet.merchantAcquirerPreferenceId!!)
                if (merchantSaved !is MerchantAcquirer) {
                    merchantAcquirerRepository.save(merchantUpdated)
                }
                var subMerchantNew = SubMerchantAcquirer()
                subMerchantNew.subMerchantAcquirerId = outletDetails.outlet.subMerchantPreferenceId
                subMerchantNew.generatedFrom = "aggregator"
                subMerchantNew.systemGenerated = true
                var submerchant = updateSubMerchantDetails(subMerchantNew, merchant)
                val subMerchantSaved = subMerchantRepository.findBySubMerchantAcquirerId(outletDetails.outlet.subMerchantPreferenceId!!)
                if (subMerchantSaved !is SubMerchantAcquirer) {
                    subMerchantRepository.save(submerchant)
                }
            } else if (outletDetails.parentType == "institution" && outletDetails.parentDataInstitution is Institution) {
                var merchantGroupNew = MerchantGroup()
                merchantGroupNew.merchantGroupPreferenceId = outletDetails.outlet.merchantGroupPreferenceId
                merchantGroupNew.systemGenerated = true
                merchantGroupNew.generatedFrom = "institution"
                var merchantGroup = updateMerchantGroupDetials(merchantGroupNew, outletDetails.parentDataInstitution)
                val merchantGroupSaved = merchantGroupRepository.findByMerchantGroupPreferenceId(outletDetails.outlet.merchantGroupPreferenceId!!)
                if (merchantGroupSaved !is MerchantGroup) {
                    merchantGroupRepository.save(merchantGroup)
                }
                var merchantNew = MerchantAcquirer()
                merchantNew.merchantAcquirerId = outletDetails.outlet.merchantAcquirerPreferenceId
                merchantNew.systemGenerated = true
                merchantNew.generatedFrom = "institution"
                val merchant = updateMerchantDetials(merchantNew, merchantGroup)
                val merchantSaved = merchantAcquirerRepository.findByMerchantAcquirerId(outletDetails.outlet.merchantAcquirerPreferenceId!!)
                if (merchantSaved !is MerchantAcquirer) {
                    merchantAcquirerRepository.save(merchant)
                }
                var subMerchantNew = SubMerchantAcquirer()
                subMerchantNew.subMerchantAcquirerId = outletDetails.outlet.subMerchantPreferenceId
                subMerchantNew.systemGenerated = true
                subMerchantNew.generatedFrom = "institution"
                var submerchant = updateSubMerchantDetails(subMerchantNew, merchant)
                val subMerchantSaved = subMerchantRepository.findBySubMerchantAcquirerId(outletDetails.outlet.subMerchantPreferenceId!!)
                if (subMerchantSaved !is SubMerchantAcquirer) {
                    subMerchantRepository.save(submerchant)
                }
            } else if (outletDetails.parentType == "merchantGroup" && outletDetails.parentDataMerchantGroup is MerchantGroup) {
                var merchantNew = MerchantAcquirer()
                merchantNew.merchantAcquirerId = outletDetails.outlet.merchantAcquirerPreferenceId
                merchantNew.systemGenerated = true
                merchantNew.generatedFrom = "merchantGroup"
                val merchant = updateMerchantDetials(merchantNew, outletDetails.parentDataMerchantGroup)
                val merchantSaved = merchantAcquirerRepository.findByMerchantAcquirerId(outletDetails.outlet.merchantAcquirerPreferenceId!!)
                if (merchantSaved !is MerchantAcquirer) { merchantAcquirerRepository.save(merchant) }
                var subMerchantNew = SubMerchantAcquirer()
                subMerchantNew.subMerchantAcquirerId = outletDetails.outlet.subMerchantPreferenceId
                subMerchantNew.systemGenerated = true
                subMerchantNew.generatedFrom = "merchantGroup"
                var submerchant = updateSubMerchantDetails(subMerchantNew, merchant)
                val subMerchantSaved = subMerchantRepository.findBySubMerchantAcquirerId(outletDetails.outlet.subMerchantPreferenceId!!)
                if (subMerchantSaved !is SubMerchantAcquirer) {
                    subMerchantRepository.save(submerchant)
                }
            } else if (outletDetails.parentType == "merchant" && outletDetails.parentDataMerchantAcquirer is MerchantAcquirer) {
                var subMerchantNew = SubMerchantAcquirer()
                subMerchantNew.subMerchantAcquirerId = outletDetails.outlet.subMerchantPreferenceId
                subMerchantNew.systemGenerated = true
                subMerchantNew.generatedFrom = "merchant"
                var submerchant = updateSubMerchantDetails(subMerchantNew, outletDetails.parentDataMerchantAcquirer)
                val subMerchantSaved = subMerchantRepository.findBySubMerchantAcquirerId(outletDetails.outlet.subMerchantPreferenceId!!)
                if (subMerchantSaved !is SubMerchantAcquirer) { subMerchantRepository.save(submerchant) }
            }
        }

        outletRepository.save(outletPersist)
    }

    @Transactional
    fun createPos(pos: Pos) {
        pos.posUniqueId = generateUniqueString()
        posRepository.save(pos)
    }
    fun generateUniqueString(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString().replace("-", "").substring(0, 21)
    }

    fun getAggregatorTree(): MutableIterable<AggregatorTree> {
        var result: MutableList<Aggregator> = mutableListOf()
        result =
            aggregatorRepository.findAll().sortedBy { list -> list.aggregatorPreferenceId } as MutableList<Aggregator>
        var result1: MutableList<AggregatorTree>
        var finalArrayList: MutableList<AggregatorTree> = mutableListOf()
        result.forEach {
            println("inside for loop Tree")
            var tree: AggregatorTree = AggregatorTree()
            tree.aggregatorPreferenceId = it.aggregatorPreferenceId
            tree.aggregatorName = it.aggregatorName
            tree.aggregatorStatus = it.aggregatorStatus
            tree.aggregatorLogo = it.aggregatorLogo
            tree.aggregatorImage = "test"
            tree.institutions = instutionList(it)
            finalArrayList.add(tree)
        }
        return finalArrayList
    }

    fun findByAcquirerAggregatorById(aggregatorId: String): Aggregator {
        return aggregatorRepository.getAggregatorByAggregatorPreferenceId(aggregatorId)
    }

    fun findInstitutionById(aggregatorId: String): Institution {
        return institutionRepository.getByInstitutionId(aggregatorId)
    }

    fun findMerchantGroupById(merchantGroupId: String): MerchantGroup? {
        return merchantGroupRepository.getByMerchantGroupPreferenceId(merchantGroupId)
    }

    fun findMerchantById(merchantId: String): MerchantAcquirer? {
        return merchantAcquirerRepository.findByMerchantAcquirerId(merchantId)
    }
    fun findSubMerchantById(subMerchantId: String): SubMerchantAcquirer? {
        return subMerchantRepository.findBySubMerchantAcquirerId(subMerchantId)
    }

    // Aggregator Update
    fun acquirerAggregatorUpdateV2(aggregatorReq: Aggregator): String {
        try {
            var aggregator =
                aggregatorRepository.findByAggregatorPreferenceId(aggregatorReq.aggregatorPreferenceId!!)
            // var aggregator: Aggregator = aggregatorRepository.getAggregatorById(id)
            println("acquirerAggregatorUpdateV2")
            if (aggregator is Aggregator) {
                aggregator.aggregatorPreferenceId = aggregatorReq.aggregatorPreferenceId
                aggregator.aggregatorName = aggregatorReq.aggregatorName
                aggregator.clientAggregatorPreferenceId = aggregatorReq.clientAggregatorPreferenceId
                aggregator.aggregatorLogo = aggregatorReq.aggregatorLogo
                // val address = entityAddressRepository.save(aggregatorReq.address)
                aggregator.address = aggregatorReq.address
                // val adminDetails = entityAdminDetailsRepository.save(aggregatorReq.adminDetails)
                aggregator.adminDetails = aggregatorReq.adminDetails
                // val bankDetails = entityBankDetailsRepository.save(aggregatorReq.bankDetails)
                aggregator.bankDetails = aggregatorReq.bankDetails
                // val contact = entityContactDetailsRepository.save(aggregatorReq.contactDetails)
                aggregator.contactDetails = aggregatorReq.contactDetails
                // val info = entityInfoRepository.save(aggregatorReq.info)
                aggregator.info = aggregatorReq.info
                // val others = entityOthersRepository.save(aggregatorReq.others)
                aggregator.others = aggregatorReq.others

                var institutionList: MutableList<Institution>? = mutableListOf()
                institutionList = institutionRepository.findByAggregatorPreferenceId(aggregator.aggregatorPreferenceId!!)
                institutionList?.forEach { it1 ->
                    if (it1.systemGenerated) {
                        it1.insitutionName = aggregatorReq.aggregatorName
                        it1.insitutionPreferenceId = aggregatorReq.clientAggregatorPreferenceId
                        it1.institutionLogo = aggregatorReq.aggregatorLogo
                        it1.address = aggregatorReq.address
                        it1.adminDetails = aggregatorReq.adminDetails
                        it1.bankDetails = aggregatorReq.bankDetails
                        it1.contactDetails = aggregatorReq.contactDetails
                        it1.info = aggregatorReq.info
                        it1.others = aggregatorReq.others
                        var merchantGroupList =
                            merchantGroupRepository.findByInsitutionPreferenceId(it1.institutionId!!)
                        merchantGroupList!!.forEach { it2 ->
                            if (it2.systemGenerated && it2.generatedFrom == "aggregator") {
                                it2.merchantGroupName = aggregatorReq.aggregatorName
                                it2.clientMerchantGroupId = aggregatorReq.clientAggregatorPreferenceId
                                it2.merchantGroupLogo = aggregatorReq.aggregatorLogo
                                it2.address = aggregatorReq.address
                                it2.adminDetails = aggregatorReq.adminDetails
                                it2.bankDetails = aggregatorReq.bankDetails
                                it2.contactDetails = aggregatorReq.contactDetails
                                it2.info = aggregatorReq.info
                                it2.others = aggregatorReq.others
                            }
                            var merchantList =
                                merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                            merchantList!!.forEach { it3 ->
                                if (it3.systemGenerated && it2.generatedFrom == "aggregator") {
                                    it3.merchantAcquirerName = aggregatorReq.aggregatorName
                                    it3.clientMerchantAcquirerId = aggregatorReq.clientAggregatorPreferenceId
                                    it3.merchantAcquirerLogo = aggregatorReq.aggregatorLogo
                                    it3.address = aggregatorReq.address
                                    it3.adminDetails = aggregatorReq.adminDetails
                                    it3.bankDetails = aggregatorReq.bankDetails
                                    it3.contactDetails = aggregatorReq.contactDetails
                                    it3.info = aggregatorReq.info
                                    it3.others = aggregatorReq.others

                                    var subMerchantList =
                                        subMerchantRepository.findByMerchantAcquirerPreferenceId(it3.merchantAcquirerId!!)
                                    subMerchantList!!.forEach { it4 ->
                                        if (it4.systemGenerated && it4.generatedFrom == "aggregator") {
                                            it4.subMerchantAcquirerName = aggregatorReq.aggregatorName
                                            it4.clientSubMerchantAcquirerId = aggregatorReq.clientAggregatorPreferenceId
                                            it4.subMerchantAcquirerLogo = aggregatorReq.aggregatorLogo
                                            it4.address = aggregatorReq.address
                                            it4.adminDetails = aggregatorReq.adminDetails
                                            it4.bankDetails = aggregatorReq.bankDetails
                                            it4.contactDetails = aggregatorReq.contactDetails
                                            it4.info = aggregatorReq.info
                                            it4.others = aggregatorReq.others
                                        }
                                    }
                                    subMerchantRepository.saveAll(subMerchantList)
                                }
                            }
                            merchantAcquirerRepository.saveAll(merchantList)
                        }
                        merchantGroupRepository.saveAll(merchantGroupList)
                    }
                }
                institutionRepository.saveAll(institutionList!!)

                if (aggregator.aggregatorStatus == "active" && aggregatorReq.aggregatorStatus == "block") {
                    institutionList.forEach { it1 ->
                        if (!it1.isParentBlocked) {
                            it1.isParentBlocked = true
                            if (it1.insitutionStatus != "block") {
                                var merchantGroupList: MutableList<MerchantGroup>? = mutableListOf()
                                merchantGroupList =
                                    merchantGroupRepository.findByInsitutionPreferenceId(it1.institutionId!!)
                                if (merchantGroupList != null && merchantGroupList.size> 0) {

                                    merchantGroupList.forEach { it2 ->
                                        if (!it2.isParentBlocked) {
                                            it2.isParentBlocked = true
                                            if (it2.merchantGroupStatus != "block") {

                                                var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                                merchantList =
                                                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                                                if (merchantList != null && merchantList.size> 0) {

                                                    merchantList.forEach { it3 ->
                                                        if (!it3.isParentBlocked) {
                                                            it3.isParentBlocked = true
                                                            println("merchnat1 ${it3.merchantAcquirerId}")

                                                            blockUnblockSubMerchant(it3, true)
                                                            // merchantAcquirerRepository.save(it3)
                                                        }
                                                    }
                                                    merchantAcquirerRepository.saveAll(merchantList)
                                                }
                                            }
                                        }
                                    }
                                    merchantGroupRepository.saveAll(merchantGroupList)
                                    // merchantGroupList.forEach{img->merchantGroupRepository.save(img)}
                                }
                            }
                        }
                    }
                    institutionRepository.saveAll(institutionList!!)
                } else if (aggregator.aggregatorStatus == "block" && aggregatorReq.aggregatorStatus == "active") {
                    if (institutionList != null && institutionList.size> 0) {
                        institutionList.forEach { it1 ->
                            if (it1.isParentBlocked) {
                                it1.isParentBlocked = false
                            }
                            if (it1.insitutionStatus != "block") {

                                var merchantGroupList: MutableList<MerchantGroup>? = mutableListOf()
                                merchantGroupList =
                                    merchantGroupRepository.findByInsitutionPreferenceId(it1.institutionId!!)
                                if (merchantGroupList != null && merchantGroupList!!.size> 0) {
                                    merchantGroupList?.forEach { it2 ->
                                        if (it2.isParentBlocked) {
                                            it2.isParentBlocked = false
                                            if (it2.merchantGroupStatus != "block") {
                                                var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                                merchantList =
                                                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                                                if (merchantList != null && merchantList!!.size> 0) {
                                                    merchantList?.forEach { it3 ->
                                                        if (it3.isParentBlocked) {
                                                            it3.isParentBlocked = false
                                                            blockUnblockSubMerchant(it3, false)
                                                        }
                                                    }
                                                    merchantAcquirerRepository.saveAll(merchantList!!)
                                                }
                                            }
                                        }
                                    }
                                    merchantGroupRepository.saveAll(merchantGroupList!!)
                                }
                            }
                        }

                        institutionRepository.saveAll(institutionList)
                    }
                }
                if (aggregatorReq.aggregatorStatus == "closed") {
                    institutionList.forEach { it1 ->
                        it1.insitutionStatus = "closed"
                        var merchantGroupList: MutableList<MerchantGroup>? = mutableListOf()
                        merchantGroupList =
                            merchantGroupRepository.findByInsitutionPreferenceId(it1.institutionId!!)
                        if (merchantGroupList != null && merchantGroupList!!.size> 0) {

                            merchantGroupList!!.forEach { it2 ->
                                it2.merchantGroupStatus = "closed"

                                var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                merchantList =
                                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                                if (merchantList != null && merchantList!!.size> 0) {

                                    merchantList!!.forEach { it3 ->
                                        it3.merchantAcquirerStatus = "closed"
                                        closeSubMerchant(it3, true)
                                        // merchantAcquirerRepository.save(it3)
                                    }
                                }
                                merchantAcquirerRepository.saveAll(merchantList!!)
                            }

                            merchantGroupRepository.saveAll(merchantGroupList!!)
                            // merchantGroupList.forEach{img->merchantGroupRepository.save(img)}
                        }
                    }
                }
                if (aggregator.aggregatorStatus == "active" && aggregatorReq.aggregatorStatus == "de-active") {
                    institutionList.forEach { it1 ->
                        if (!it1.isParentDeActivated) {
                            it1.isParentDeActivated = true
                            if (it1.insitutionStatus != "de-active") {
                                var merchantGroupList: MutableList<MerchantGroup>? = mutableListOf()
                                merchantGroupList =
                                    merchantGroupRepository.findByInsitutionPreferenceId(it1.institutionId!!)
                                if (merchantGroupList != null && merchantGroupList!!.size> 0) {

                                    merchantGroupList!!.forEach { it2 ->
                                        if (!it2.isParentDeActivated) {
                                            it2.isParentDeActivated = true
                                            if (it2.merchantGroupStatus != "de-active") {

                                                var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                                merchantList =
                                                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                                                if (merchantList != null && merchantList!!.size> 0) {

                                                    merchantList!!.forEach { it3 ->
                                                        if (!it3.isParentDeActivated) {
                                                            it3.isParentDeActivated = true
                                                            println("merchnat1 ${it3.merchantAcquirerId}")

                                                            deActivateSubMerchant(it3, true)
                                                            // merchantAcquirerRepository.save(it3)
                                                        }
                                                    }
                                                    merchantAcquirerRepository.saveAll(merchantList!!)
                                                }
                                            }
                                        }
                                    }
                                    merchantGroupRepository.saveAll(merchantGroupList!!)
                                    // merchantGroupList.forEach{img->merchantGroupRepository.save(img)}
                                }
                            }
                        }
                    }
                    institutionRepository.saveAll(institutionList!!)
                } else if (aggregator.aggregatorStatus == "de-active" && aggregatorReq.aggregatorStatus == "active") {
                    if (institutionList != null && institutionList.size> 0) {
                        institutionList.forEach { it1 ->
                            if (it1.isParentDeActivated) {
                                it1.isParentDeActivated = false
                            }
                            if (it1.insitutionStatus != "de-active") {

                                var merchantGroupList: MutableList<MerchantGroup>? = mutableListOf()
                                merchantGroupList =
                                    merchantGroupRepository.findByInsitutionPreferenceId(it1.institutionId!!)
                                if (merchantGroupList != null && merchantGroupList!!.size> 0) {
                                    merchantGroupList?.forEach { it2 ->
                                        if (it2.isParentDeActivated) {
                                            it2.isParentDeActivated = false
                                            if (it2.merchantGroupStatus != "de-active") {
                                                var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                                merchantList =
                                                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                                                if (merchantList != null && merchantList!!.size> 0) {
                                                    merchantList?.forEach { it3 ->
                                                        if (it3.isParentDeActivated) {
                                                            it3.isParentDeActivated = false
                                                            deActivateSubMerchant(it3, false)
                                                        }
                                                    }
                                                    merchantAcquirerRepository.saveAll(merchantList!!)
                                                }
                                            }
                                        }
                                    }
                                    merchantGroupRepository.saveAll(merchantGroupList!!)
                                }
                            }
                        }

                        institutionRepository.saveAll(institutionList)
                    }
                }
                aggregator.aggregatorStatus = aggregatorReq.aggregatorStatus
                aggregatorRepository.save(aggregator)
            }
        } catch (e: Exception) {
            throw Exception("Invalid ID" + e.message)
        }
        return "Updated Successfully"
    }

    // Merchant Group Update
    fun acquirerMerchantGroupUpdateV2(merchantGroupRequest: MerchantGroup): Any {
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        val error1 = com.vacuumlabs.wadzpay.acquireradministration.model.ValidationErrorResponseCustom(mutableListOf())
        val list = error1.violations
        val error = StringBuilder()
        val joiner = StringJoiner(",")
        val violations: Set<ConstraintViolation<MerchantGroup>> = validator.validate(merchantGroupRequest)
        val commonViolations = getViolations(validator, merchantGroupRequest.adminDetails, merchantGroupRequest.contactDetails, merchantGroupRequest.bankDetails, merchantGroupRequest.address, merchantGroupRequest.info)
        if (violations.isNotEmpty()) {

            addErrors(violations, error, joiner)
        }
        addCommonViolationMessages(commonViolations, error, joiner)

        violations.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        error1.violations = addViolationsToList(commonViolations, list)

        if (joiner.toString().isNotEmpty()) {
            // error1.violations=violations.toList() as MutableList<Violation>
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroValidationResponse(HttpStatus.BAD_REQUEST.value(), joiner.toString().toString(), error1))
        }

        var merchantGroup: MerchantGroup? =
            merchantGroupRepository.getByMerchantGroupPreferenceId(merchantGroupRequest.merchantGroupPreferenceId!!)
        if (merchantGroup is MerchantGroup) {
            merchantGroup.aggregatorPreferenceId = merchantGroupRequest.aggregatorPreferenceId
            merchantGroup.insitutionPreferenceId = merchantGroupRequest.insitutionPreferenceId
            merchantGroup.merchantGroupPreferenceId = merchantGroupRequest.merchantGroupPreferenceId
            merchantGroup.clientMerchantGroupId = merchantGroupRequest.clientMerchantGroupId
            merchantGroup.merchantGroupName = merchantGroupRequest.merchantGroupName
            merchantGroup.merchantGroupLogo = merchantGroupRequest.merchantGroupLogo
            merchantGroup.address = entityAddressRepository.save(merchantGroupRequest.address)
            merchantGroup.adminDetails = entityAdminDetailsRepository.save(merchantGroupRequest.adminDetails)
            merchantGroup.bankDetails = entityBankDetailsRepository.save(merchantGroupRequest.bankDetails)
            merchantGroup.contactDetails = entityContactDetailsRepository.save(merchantGroupRequest.contactDetails)
            merchantGroup.info = entityInfoRepository.save(merchantGroupRequest.info)
            merchantGroup.others = entityOthersRepository.save(merchantGroupRequest.others)
            var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
            merchantList =
                merchantAcquirerRepository.findByMerchantGroupPreferenceId(merchantGroup.merchantGroupPreferenceId!!)
            merchantList?.forEach { it ->
                if (it.systemGenerated && it.generatedFrom == "merchantGroup") {
                    it.clientMerchantAcquirerId = merchantGroupRequest.clientMerchantGroupId
                    it.merchantAcquirerName = merchantGroupRequest.merchantGroupName
                    it.merchantAcquirerLogo = merchantGroupRequest.merchantGroupLogo
                    it.address = merchantGroupRequest.address
                    it.adminDetails = merchantGroupRequest.adminDetails
                    it.bankDetails = merchantGroupRequest.bankDetails
                    it.contactDetails = merchantGroupRequest.contactDetails
                    it.info = merchantGroupRequest.info
                    it.others = merchantGroupRequest.others
                    val submerchants = subMerchantRepository.findByMerchantAcquirerPreferenceId(it.merchantAcquirerId!!)
                    submerchants!!.forEach { it1 ->
                        if (it1.systemGenerated && it1.generatedFrom == "merchantGroup") {
                            it1.clientSubMerchantAcquirerId = merchantGroupRequest.clientMerchantGroupId
                            it1.subMerchantAcquirerName = merchantGroupRequest.merchantGroupName
                            it1.subMerchantAcquirerLogo = merchantGroupRequest.merchantGroupLogo
                            it1.address = merchantGroupRequest.address
                            it1.adminDetails = merchantGroupRequest.adminDetails
                            it1.bankDetails = merchantGroupRequest.bankDetails
                            it1.contactDetails = merchantGroupRequest.contactDetails
                            it1.info = merchantGroupRequest.info
                            it1.others = merchantGroupRequest.others
                        }
                    }
                    subMerchantRepository.saveAll(submerchants)
                }
            }
            merchantAcquirerRepository.saveAll(merchantList!!)
            if (merchantGroup.merchantGroupStatus == "active" && merchantGroupRequest.merchantGroupStatus == "block") {
                merchantList.forEach { it1 ->
                    it1.isParentBlocked = true
                    blockUnblockSubMerchant(it1, true)
                }
                merchantAcquirerRepository.saveAll(merchantList!!)
            } else if (merchantGroup.merchantGroupStatus == "block" && merchantGroupRequest.merchantGroupStatus == "active") {
                merchantList.forEach { it1 ->
                    if (it1.isParentBlocked) {
                        it1.isParentBlocked = false
                    }
                    blockUnblockSubMerchant(it1, false)
                }
                merchantAcquirerRepository.saveAll(merchantList)
            } else if (merchantGroupRequest.merchantGroupStatus == "de-active") {
                merchantList.forEach { it1 ->
                    it1.isParentDeActivated = true
                    deActivateSubMerchant(it1, true)
                }
            } else if (merchantGroupRequest.merchantGroupStatus == "closed") {
                merchantList.forEach { it1 ->
                    it1.merchantAcquirerStatus = "closed"
                    closeSubMerchant(it1, true)
                }
            }
            merchantGroup.merchantGroupStatus = merchantGroupRequest.merchantGroupStatus
            println(merchantGroup)
            println(merchantGroup.merchantGroupStatus)
            merchantGroupRepository.save(merchantGroup)
            return "Updated Successfully"
        } else
            return "Merchant Not found"
    }

    fun acquirerMerchantAcquirerUpdate(merchantAcquirerRequest: MerchantAcquirer): Any {
        logger.info("acquirerMerchantAcquirerUpdate")
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        val error1 = com.vacuumlabs.wadzpay.acquireradministration.model.ValidationErrorResponseCustom(mutableListOf())
        val list = error1.violations
        val error = StringBuilder()
        val joiner = StringJoiner(",")
        val violations: Set<ConstraintViolation<MerchantAcquirer>> = validator.validate(merchantAcquirerRequest)
        val commonViolations = getViolations(validator, merchantAcquirerRequest.adminDetails, merchantAcquirerRequest.contactDetails, merchantAcquirerRequest.bankDetails, merchantAcquirerRequest.address, merchantAcquirerRequest.info)
        if (violations.isNotEmpty()) {
            addErrors(violations, error, joiner)
        }
        addCommonViolationMessages(commonViolations, error, joiner)
        violations.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        error1.violations = addViolationsToList(commonViolations, list)

        /*if (violationsBank.isNotEmpty()) {

            addErrors(violationsBank, error, joiner)
        }
        if (violationsContact.isNotEmpty()) {
            addErrors(violationsContact, error, joiner)
        }
        if (violationsInfo.isNotEmpty()) {

            addErrors(violationsInfo, error, joiner)
        }
        if (violationsAdmin.isNotEmpty()) {

            addErrors(violationsAdmin, error, joiner)
        }
        if (violationsAddress.isNotEmpty()) {

            addErrors(violationsAddress, error, joiner)
        }*/
        if (joiner.toString().isNotEmpty()) {
            // error1.violations=violations.toList() as MutableList<Violation>
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroValidationResponse(HttpStatus.BAD_REQUEST.value(), joiner.toString().toString(), error1))
        }
        var marchantAcquirer: MerchantAcquirer? =
            merchantAcquirerRepository.findByMerchantAcquirerId(merchantAcquirerRequest.merchantAcquirerId!!)
        marchantAcquirer?.aggregatorPreferenceId = merchantAcquirerRequest.aggregatorPreferenceId
        marchantAcquirer?.insitutionPreferenceId = merchantAcquirerRequest.insitutionPreferenceId
        marchantAcquirer?.merchantGroupPreferenceId = merchantAcquirerRequest.merchantGroupPreferenceId
        marchantAcquirer?.merchantAcquirerId = merchantAcquirerRequest.merchantAcquirerId
        marchantAcquirer?.merchantAcquirerName = merchantAcquirerRequest.merchantAcquirerName
        marchantAcquirer?.clientMerchantAcquirerId = merchantAcquirerRequest.clientMerchantAcquirerId
        marchantAcquirer?.merchantAcquirerLogo = merchantAcquirerRequest.merchantAcquirerLogo
        marchantAcquirer?.address = merchantAcquirerRequest.address
        marchantAcquirer?.adminDetails = merchantAcquirerRequest.adminDetails
        marchantAcquirer?.bankDetails = merchantAcquirerRequest.bankDetails
        marchantAcquirer?.others = entityOthersRepository.save(merchantAcquirerRequest.others)
        marchantAcquirer?.info = entityInfoRepository.save(merchantAcquirerRequest.info)
        marchantAcquirer?.bankDetails = entityBankDetailsRepository.save(merchantAcquirerRequest.bankDetails)
        // var admin: EntityAdminDetails? = null
        try {
            marchantAcquirer?.adminDetails = entityAdminDetailsRepository.save(merchantAcquirerRequest.adminDetails)
        } catch (e: ConstraintViolationException) {
            throw e
            // ("Application ConstraintViolationException")
        }
        var address: EntityAddress? = null
        try {
            marchantAcquirer?.address = entityAddressRepository.save(merchantAcquirerRequest.address)
        } catch (e: ConstraintViolationException) {
            throw e
            // ("Application ConstraintViolationException")
        }

        marchantAcquirer?.contactDetails = entityContactDetailsRepository.save(merchantAcquirerRequest.contactDetails)

        // marchantAcquirer?.contactDetails = merchantAcquirerRequest.contactDetails
        // marchantAcquirer?.info = merchantAcquirerRequest.info
        // marchantAcquirer?.others = merchantAcquirerRequest.others
        var submerchantList: MutableList<SubMerchantAcquirer> ? = mutableListOf()
        submerchantList =
            subMerchantRepository.findByMerchantAcquirerPreferenceId(merchantAcquirerRequest.merchantAcquirerId!!)
        submerchantList?.forEach { it ->
            if (it.systemGenerated && it.generatedFrom == "merchant") {
                it.clientSubMerchantAcquirerId = merchantAcquirerRequest.clientMerchantAcquirerId
                it.subMerchantAcquirerName = merchantAcquirerRequest.merchantAcquirerName
                it.subMerchantAcquirerLogo = merchantAcquirerRequest.merchantAcquirerLogo
                it.address = merchantAcquirerRequest.address
                it.adminDetails = merchantAcquirerRequest.adminDetails
                it.bankDetails = merchantAcquirerRequest.bankDetails
                it.others = entityOthersRepository.save(merchantAcquirerRequest.others)
                it.info = entityInfoRepository.save(merchantAcquirerRequest.info)
                it.bankDetails = entityBankDetailsRepository.save(merchantAcquirerRequest.bankDetails)
            }
        }
        subMerchantRepository.saveAll(submerchantList!!)
        if (merchantAcquirerRequest.merchantAcquirerStatus == "block" && marchantAcquirer?.merchantAcquirerStatus == "active") {
            if (submerchantList != null && submerchantList.size> 0) {
                submerchantList.forEach { it1 ->
                    it1.isParentBlocked = true
                    blockUnblockOutlet(it1, true)
                }
                subMerchantRepository.saveAll(submerchantList)
            }
        } else if (merchantAcquirerRequest.merchantAcquirerStatus == "active" && marchantAcquirer?.merchantAcquirerStatus == "block") {
            if (submerchantList != null && submerchantList.size> 0) {

                submerchantList.forEach { it1 ->
                    it1.isParentBlocked = false
                    blockUnblockOutlet(it1, false)
                }

                subMerchantRepository.saveAll(submerchantList)
            }
        } else if (merchantAcquirerRequest.merchantAcquirerStatus == "de-active") {
            deActivateSubMerchant(marchantAcquirer!!, true)
        } else if (merchantAcquirerRequest.merchantAcquirerStatus == "closed") {
            closeSubMerchant(marchantAcquirer!!, true)
        }
        marchantAcquirer?.merchantAcquirerStatus = merchantAcquirerRequest.merchantAcquirerStatus
        if (marchantAcquirer is MerchantAcquirer) {
            merchantAcquirerRepository.save(marchantAcquirer)
            return "Updated Successfully"
        } else
            return "Merchant Not found"
    }
    fun addViolationsToList(commonViolations: CommonValidator, list: MutableList<Violation>): MutableList<Violation> {
        commonViolations.violationsAddress.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        commonViolations.violationsContact.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        commonViolations.violationsBank.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        commonViolations.violationsAdmin.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        commonViolations.violationsInfo.forEach { it -> list.add(Violation(it.propertyPath.toString(), it.messageTemplate)) }
        return list
    }
    fun addCommonViolationMessages(validations: CommonValidator, error: StringBuilder, joiner: StringJoiner) {
        if (validations.violationsBank.isNotEmpty()) {

            addErrors(validations.violationsBank, error, joiner)
        }
        if (validations.violationsContact.isNotEmpty()) {
            addErrors(validations.violationsContact, error, joiner)
        }
        if (validations.violationsInfo.isNotEmpty()) {

            addErrors(validations.violationsInfo, error, joiner)
        }
        if (validations.violationsAdmin.isNotEmpty()) {

            addErrors(validations.violationsAdmin, error, joiner)
        }
        if (validations.violationsAddress.isNotEmpty()) {

            addErrors(validations.violationsAddress, error, joiner)
        }
    }
    fun getViolations(validator: Validator, adminDetails: EntityAdminDetails, contactDetails: EntityContactDetails, bankDetails: EntityBankDetails, address: EntityAddress, info: EntityInfo): CommonValidator {

        val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> =
            validator.validate(adminDetails)
        val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
            validator.validate(contactDetails)
        val violationsBank: Set<ConstraintViolation<EntityBankDetails>> =
            validator.validate(bankDetails)
        val violationsAddress: Set<ConstraintViolation<EntityAddress>> =
            validator.validate(address)
        val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(info)
        return CommonValidator(violationsContact, violationsAdmin, violationsBank, violationsAddress, violationsInfo)
    }
    // Aggregator Delete
    fun acquirerAggregatorDelete(aggregatorId: String): String {
        println("acquirerAggregatorDelete")
        return try {
            var aggregator = aggregatorRepository.getAggregatorByAggregatorPreferenceId(aggregatorId)
            aggregatorRepository.deleteById(aggregator.id)
            "Deleted Successfully"
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    // Merchant Group Delete
    fun acquirerMerchantGroupDelete(deleteMerchantGroupId: String): String {
        println("acquirerMerchantGroupDelete")
        return try {
            var merchantGroup = merchantGroupRepository.getByMerchantGroupPreferenceId(deleteMerchantGroupId)
            if (merchantGroup is MerchantGroup) {
                merchantGroupRepository.deleteById(merchantGroup.id)
                "Deleted Successfully"
            } else {
                "Merchant Group not found "
            }
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    // Merchant Group Delete
    fun acquirerMerchantAcquirerDelete(deleteMerchantAcquirerId: String): String {
        println("acquirerMerchantAcquirerDelete")
        return try {
            var merchant = merchantAcquirerRepository.findByMerchantAcquirerId(deleteMerchantAcquirerId)
            if (merchant is MerchantAcquirer) {
                merchantAcquirerRepository.deleteById(merchant.id)
                "Deleted Successfully"
            } else
                "Merchant not found"
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    // Merchant Group Delete
    fun subMerchantAcquirerDelete(subMerchantAcquirerId: String): String {
        println("acquirerMerchantAcquirerDelete")
        return try {
            var subMerchant = subMerchantRepository.findBySubMerchantAcquirerId(subMerchantAcquirerId)
            if (subMerchant is SubMerchantAcquirer) {
                subMerchantRepository.deleteById(subMerchant.id)
                "Deleted Successfully"
            } else {
                "Sub Merchant not found"
            }
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    @Transactional
    fun saveAggegatorList(list: List<Aggregator>): List<Aggregator> {
        var errorList = mutableListOf<Aggregator>()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        list.forEach { it ->
            val violations: Set<ConstraintViolation<Aggregator>> = validator.validate(it)
            val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> = validator.validate(it.adminDetails)
            val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
                validator.validate(it.contactDetails)
            val violationsBank: Set<ConstraintViolation<EntityBankDetails>> = validator.validate(it.bankDetails)
            val violationsAddress: Set<ConstraintViolation<EntityAddress>> = validator.validate(it.address)
            val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(it.info)
            var error = StringBuilder()
            val joiner = StringJoiner(",")

            if (violations.isNotEmpty()) {

                addErrors(violations, error, joiner)
            }
            if (violationsAddress.isNotEmpty()) {

                addErrors(violationsAddress, error, joiner)
            }
            if (violationsContact.isNotEmpty()) {
                addErrors(violationsContact, error, joiner)
            }
            if (violationsBank.isNotEmpty()) {

                addErrors(violationsBank, error, joiner)
            }
            if (violationsAdmin.isNotEmpty()) {

                addErrors(violationsAdmin, error, joiner)
            }
            if (violationsInfo.isNotEmpty()) {

                addErrors(violationsInfo, error, joiner)
            }
            if (joiner.toString().isNotEmpty()) {

                it.error = joiner.toString()
                errorList.add(it)
            } else {
                var aggregator: Aggregator? =
                    aggregatorRepository.findByAggregatorPreferenceId(it.aggregatorPreferenceId!!)
                if (aggregator?.aggregatorPreferenceId.isNullOrEmpty()) {
                    try {

                        aggregatorRepository.save(it)
                    } catch (e: Exception) {
                        it.error = "Application Error"
                        errorList.add(it)
                    }
                } else {
                    // update
                    try {
                        acquirerAggregatorUpdateV2(it)
                    } catch (e: ConstraintViolationException) {
                        it.error = e.message
                        errorList.add(it)
                    } catch (e: Exception) {
                        it.error = "Application Error"
                        errorList.add(it)
                    }
                }
            }
        }
        println("errorList $errorList")

        return errorList
    }

    @Transactional
    fun saveInstitutionList(list: List<Institution>): List<Institution> {
        var errorList = mutableListOf<Institution>()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        list.forEach { it ->
            val violations: Set<ConstraintViolation<Institution>> = validator.validate(it)
            val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> = validator.validate(it.adminDetails)
            val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
                validator.validate(it.contactDetails)
            val violationsBank: Set<ConstraintViolation<EntityBankDetails>> = validator.validate(it.bankDetails)
            val violationsAddress: Set<ConstraintViolation<EntityAddress>> = validator.validate(it.address)
            val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(it.info)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                addErrors(violations, error, joiner)
            }
            if (violationsAddress.isNotEmpty()) {
                addErrors(violationsAddress, error, joiner)
            }
            if (violationsContact.isNotEmpty()) {
                addErrors(violationsContact, error, joiner)
            }
            if (violationsBank.isNotEmpty()) {
                addErrors(violationsBank, error, joiner)
            }
            if (violationsAdmin.isNotEmpty()) {
                addErrors(violationsAdmin, error, joiner)
            }
            if (violationsInfo.isNotEmpty()) {
                addErrors(violationsInfo, error, joiner)
            }
            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
                errorList.add(it)
            }
            if (joiner.toString().isEmpty() && it.error.toString().isNotEmpty()) {
                errorList.add(it)
            } else {
                var institution: Institution? = institutionRepository.findByInstitutionId(it.institutionId!!)
                if (institution?.institutionId.isNullOrEmpty()) {
                    try {
                        institutionRepository.save(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                } else {
                    // update
                    try {
                        acquirerInstitutionUpdateV2(it)
                    } catch (e: ConstraintViolationException) {
                        it.error = e.message
                        errorList.add(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                }
            }
            println("errorList $errorList")
        }
        return errorList
    }

    @Transactional
    fun saveMerchantGroupList(list: List<MerchantGroup>): List<MerchantGroup> {
        var errorList = mutableListOf<MerchantGroup>()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        list.forEach { it ->
            val violations: Set<ConstraintViolation<MerchantGroup>> = validator.validate(it)

            val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> = validator.validate(it.adminDetails)
            val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
                validator.validate(it.contactDetails)
            val violationsBank: Set<ConstraintViolation<EntityBankDetails>> = validator.validate(it.bankDetails)
            val violationsAddress: Set<ConstraintViolation<EntityAddress>> = validator.validate(it.address)
            val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(it.info)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                addErrors(violations, error, joiner)
            }
            if (violationsAddress.isNotEmpty()) {
                addErrors(violationsAddress, error, joiner)
            }
            if (violationsContact.isNotEmpty()) {
                addErrors(violationsContact, error, joiner)
            }
            if (violationsBank.isNotEmpty()) {
                addErrors(violationsBank, error, joiner)
            }
            if (violationsAdmin.isNotEmpty()) {
                addErrors(violationsAdmin, error, joiner)
            }
            if (violationsInfo.isNotEmpty()) {
                addErrors(violationsInfo, error, joiner)
            }
            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
                errorList.add(it)
            } else {
                var merchantGroup: MerchantGroup? =
                    merchantGroupRepository.findByMerchantGroupPreferenceId(it.merchantGroupPreferenceId!!)
                if (merchantGroup?.merchantGroupPreferenceId.isNullOrEmpty()) {
                    try {
                        merchantGroupRepository.save(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                } else {
                    // update
                    try {
                        acquirerMerchantGroupUpdateV2(it)
                    } catch (e: ConstraintViolationException) {
                        it.error = e.message
                        errorList.add(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                }
            }
            println("errorList $errorList")
        }
        return errorList
    }

    @Transactional
    fun saveMerchantList(list: List<MerchantAcquirer>): List<MerchantAcquirer> {
        var errorList = mutableListOf<MerchantAcquirer>()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        list.forEach { it ->
            val violations: Set<ConstraintViolation<MerchantAcquirer>> = validator.validate(it)
            val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> = validator.validate(it.adminDetails)
            val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
                validator.validate(it.contactDetails)
            val violationsBank: Set<ConstraintViolation<EntityBankDetails>> = validator.validate(it.bankDetails)
            val violationsAddress: Set<ConstraintViolation<EntityAddress>> = validator.validate(it.address)
            val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(it.info)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                addErrors(violations, error, joiner)
            }
            if (violationsAddress.isNotEmpty()) {
                addErrors(violationsAddress, error, joiner)
            }
            if (violationsContact.isNotEmpty()) {
                addErrors(violationsContact, error, joiner)
            }
            if (violationsBank.isNotEmpty()) {
                addErrors(violationsBank, error, joiner)
            }
            if (violationsAdmin.isNotEmpty()) {
                addErrors(violationsAdmin, error, joiner)
            }
            if (violationsInfo.isNotEmpty()) {
                addErrors(violationsInfo, error, joiner)
            }
            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
                errorList.add(it)
            } else {
                var merchant: MerchantAcquirer? =
                    merchantAcquirerRepository.findByMerchantAcquirerId(it.merchantAcquirerId!!)
                if (merchant?.merchantAcquirerId.isNullOrEmpty()) {
                    try {
                        merchantAcquirerRepository.save(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                } else {
                    // update
                    try {
                        acquirerMerchantAcquirerUpdate(it)
                    } catch (e: ConstraintViolationException) {
                        it.error = e.message
                        errorList.add(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                }
            }
            println("errorList $errorList")
        }
        return errorList
    }

    @Transactional
    fun saveSubMerchantList(list: List<SubMerchantAcquirer>): List<SubMerchantAcquirer> {
        var errorList = mutableListOf<SubMerchantAcquirer>()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        list.forEach { it ->
            val violations: Set<ConstraintViolation<SubMerchantAcquirer>> = validator.validate(it)
            val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> = validator.validate(it.adminDetails)
            val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
                validator.validate(it.contactDetails)
            val violationsBank: Set<ConstraintViolation<EntityBankDetails>> = validator.validate(it.bankDetails)
            val violationsAddress: Set<ConstraintViolation<EntityAddress>> = validator.validate(it.address)
            val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(it.info)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                addErrors(violations, error, joiner)
            }
            if (violationsAddress.isNotEmpty()) {
                addErrors(violationsAddress, error, joiner)
            }
            if (violationsContact.isNotEmpty()) {
                addErrors(violationsContact, error, joiner)
            }
            if (violationsBank.isNotEmpty()) {
                addErrors(violationsBank, error, joiner)
            }
            if (violationsAdmin.isNotEmpty()) {
                addErrors(violationsAdmin, error, joiner)
            }
            if (violationsInfo.isNotEmpty()) {
                addErrors(violationsInfo, error, joiner)
            }
            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
                errorList.add(it)
            } else {
                var subMerchant: SubMerchantAcquirer? =
                    subMerchantRepository.findBySubMerchantAcquirerId(it.subMerchantAcquirerId!!)
                if (subMerchant?.subMerchantAcquirerId.isNullOrEmpty()) {
                    try {
                        subMerchantRepository.save(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                } else {
                    // update
                    try {
                        updateSubMerchant(it)
                    } catch (e: ConstraintViolationException) {
                        it.error = e.message
                        errorList.add(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                }
            }
            println("errorList $errorList")
        }
        return errorList
    }
    fun fetchOutletById(outletId: String): Outlet? {
        return outletRepository.findByOutletId(outletId)
    }

    @Transactional
    fun saveOutletList(list: List<Outlet>): List<Outlet> {
        var errorList = mutableListOf<Outlet>()
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        list.forEach { it ->
            val violations: Set<ConstraintViolation<Outlet>> = validator.validate(it)
            val violationsAdmin: Set<ConstraintViolation<EntityAdminDetails>> = validator.validate(it.adminDetails)
            val violationsContact: Set<ConstraintViolation<EntityContactDetails>> =
                validator.validate(it.contactDetails)
            val violationsBank: Set<ConstraintViolation<EntityBankDetails>> = validator.validate(it.bankDetails)
            val violationsAddress: Set<ConstraintViolation<EntityAddress>> = validator.validate(it.address)
            val violationsInfo: Set<ConstraintViolation<EntityInfo>> = validator.validate(it.info)
            var error = StringBuilder()
            val joiner = StringJoiner(",")
            if (violations.isNotEmpty()) {
                addErrors(violations, error, joiner)
            }
            if (violationsAddress.isNotEmpty()) {
                addErrors(violationsAddress, error, joiner)
            }
            if (violationsContact.isNotEmpty()) {
                addErrors(violationsContact, error, joiner)
            }
            if (violationsBank.isNotEmpty()) {
                addErrors(violationsBank, error, joiner)
            }
            if (violationsAdmin.isNotEmpty()) {
                addErrors(violationsAdmin, error, joiner)
            }
            if (violationsInfo.isNotEmpty()) {
                addErrors(violationsInfo, error, joiner)
            }
            if (joiner.toString().isNotEmpty()) {
                it.error = joiner.toString()
                errorList.add(it)
            } else {
                var outlet: Outlet? = outletRepository.findByOutletId(it.outletId!!)
                if (outlet?.outletId.isNullOrEmpty()) {
                    try {
                        outletRepository.save(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                } else {
                    // update
                    try {
                        updateOutlet(it)
                    } catch (e: ConstraintViolationException) {
                        it.error = e.message
                        errorList.add(it)
                    } catch (e: Exception) {
                        it.error = e.message
                        errorList.add(it)
                    }
                }
            }
            println("errorList $errorList")
        }
        return errorList
    }

    fun addErrors(violations: Set<ConstraintViolation<*>>, error: StringBuilder, joiner: StringJoiner) {
        if (violations.isNotEmpty()) {
            violations.forEach {

                if (!joiner.toString().contains(it.message))
                    joiner.add(it.message)
            }
            // error.append(joiner)
        }
    }

    fun processOutlet(@Valid it: OutletDTO, errorList: MutableList<OutletDTO>, allDone: Boolean) {
        var outlet = outletRepository.findByOutletId(it.outletId!!)
        if (outlet?.outletId.isNullOrEmpty()) {
            try {
                outletRepository.save(dtoService.toOutletModel(it))
            } catch (e: Exception) {
                it.error = e.message
                errorList.add(it)
                // allDone=false
            }
        } else {
            // update
            try {
                updateOutletThroughFile(dtoService.toOutletModel(it))
            } catch (e: ConstraintViolationException) {
                val outet = Outlet()
                it.error = e.message
                errorList.add(it)
                // allDone=false
            } catch (e: Exception) {
                val outet = Outlet()
                it.error = e.message
                errorList.add(it)
                // allDone=false
            }
            // errorList.add(outet)
            // allDone=false
        }
    }

    @Transactional
    fun saveOutletList2(list: List<OutletDTO>): Any {
        var allDone = true
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator
        var errorList = mutableListOf<OutletDTO>()
        list.forEach { it ->
            val violations: Set<ConstraintViolation<OutletDTO>> = validator.validate(it)

            println("violations====== $violations")

            processOutlet(it, errorList, allDone)
        }

        if (allDone) return "Upload Successful"
        else return "mama"
        // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("One or More records failed to upload please check in error report for details ")
    }

    @Transactional
    fun outletDelete(outletReq: Outlet): String {
        return try {
            var outlet: Outlet? = outletRepository.findByOutletId(outletReq.outletId!!)
            println("outlet delete" + outlet)
            if (outlet is Outlet) {
                /*
                                outlet.posList.removeAll(outlet.posList)
                */
                outletRepository.deleteById(outlet.id)
                entityManager.flush()
            }
            "Deleted Successfully"
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    @Transactional
    fun posDelete(posId: String): String {
        println("pos delete $posId")
        return try {
            var pos = posRepository.findByPosId(posId)!!
            println("$pos")
            posRepository.deleteById(pos.id)
            "Deleted Successfully"
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    // Institution Delete
    fun acquirerInstitutionDelete(institutionId: String): String {
        println("acquirerInstitutionDelete")
        return try {
            var institution = institutionRepository.getByInstitutionId(institutionId)
            institutionRepository.deleteById(institution.id)
            "Deleted Successfully"
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    //    Institution List
    fun getInstitutionList(): MutableList<Institution>? {
        println("getInstitutionList")
        var resultInstutionTableData: MutableList<Institution> = mutableListOf()
        resultInstutionTableData = institutionRepository.findAll() as MutableList<Institution>
        println(resultInstutionTableData + "")
        println(resultInstutionTableData.size.toString() + "")
        return resultInstutionTableData
    }

    fun getInstitutionByAggregatorPrefId(prefId: String): MutableList<Institution>? {
        println("getInstitutionList")
        var resultInstutionTableData: MutableList<Institution> = mutableListOf()
        resultInstutionTableData =
            institutionRepository.getInstitutionByAggregatorPreferenceId(prefId) as MutableList<Institution>
        println(resultInstutionTableData + "")
        println(resultInstutionTableData.size.toString() + "")
        return resultInstutionTableData
    }

    fun getMerchantGrpByInstitutionPrefId(institutionPrefId: String): MutableList<MerchantGroup>? {
        println("getMerchantGrpByInstitutePrefId")
        var resultInstutionTableData: MutableList<MerchantGroup> = mutableListOf()
        resultInstutionTableData =
            merchantGroupRepository.findByInsitutionPreferenceId(institutionPrefId) as MutableList<MerchantGroup>
        println(resultInstutionTableData + "")
        println(resultInstutionTableData.size.toString() + "")
        return resultInstutionTableData
    }

    fun getMerchantByMerchantGroupPrefId(marchantGroupPrefId: String): MutableList<MerchantAcquirer>? {
        println("getMerchantByMerchantGroupPrefId")
        var resultInstutionTableData: MutableList<MerchantAcquirer> = mutableListOf()
        resultInstutionTableData =
            merchantAcquirerRepository.findByMerchantGroupPreferenceId(marchantGroupPrefId) as MutableList<MerchantAcquirer>
        println(resultInstutionTableData + "")
        println(resultInstutionTableData.size.toString() + "")
        return resultInstutionTableData
    }

    fun getAggregatorByAggregatorId(aggregatorId: String): Aggregator {
        println("getMerchantByMerchantGroupPrefId")
        var resultInstutionTableData: Aggregator
        resultInstutionTableData =
            aggregatorRepository.getAggregatorByAggregatorPreferenceId(aggregatorId) as Aggregator
        return resultInstutionTableData
    }

    // Institution Update
    fun acquirerInstitutionUpdateV2(institutionReq: Institution): String {
        try {
            var institution: Institution = institutionRepository.getByInstitutionId(institutionReq.institutionId!!)
            institution.aggregatorPreferenceId = institutionReq.aggregatorPreferenceId
            institution.insitutionPreferenceId = institutionReq.insitutionPreferenceId
            institution.insitutionName = institutionReq.insitutionName
            institution.institutionLogo = institutionReq.institutionLogo
            institution.address = institutionReq.address
            institution.adminDetails = institutionReq.adminDetails
            institution.bankDetails = institutionReq.bankDetails
            institution.contactDetails = institutionReq.contactDetails
            institution.info = institutionReq.info
            institution.others = institutionReq.others
            var merchantGroupList: MutableList<MerchantGroup>? = mutableListOf()
            merchantGroupList = merchantGroupRepository.findByInsitutionPreferenceId(institution.institutionId!!)
            var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
            merchantGroupList?.forEach { it ->
                if (it.systemGenerated && it.generatedFrom == "institution") {
                    it.clientMerchantGroupId = institutionReq.insitutionPreferenceId
                    it.merchantGroupName = institutionReq.insitutionName
                    it.merchantGroupLogo = institutionReq.institutionLogo
                    it.address = institutionReq.address
                    it.adminDetails = institutionReq.adminDetails
                    it.bankDetails = institutionReq.bankDetails
                    it.contactDetails = institutionReq.contactDetails
                    it.info = institutionReq.info
                    it.others = institutionReq.others
                }
                merchantList =
                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it.merchantGroupPreferenceId!!)
                if (merchantList != null && merchantList!!.size> 0)
                    merchantList?.forEach { it1 ->
                        if (it1.systemGenerated && it1.generatedFrom == "institution") {
                            it1.merchantAcquirerName = institutionReq.insitutionName
                            it1.merchantAcquirerLogo = institutionReq.institutionLogo
                            it1.clientMerchantAcquirerId = institutionReq.insitutionPreferenceId
                            it1.address = institutionReq.address
                            it1.adminDetails = institutionReq.adminDetails
                            it1.bankDetails = institutionReq.bankDetails
                            it1.contactDetails = institutionReq.contactDetails
                            it1.info = institutionReq.info
                            it1.others = institutionReq.others
                            var submerchantList =
                                subMerchantRepository.findByMerchantAcquirerPreferenceId(it1.merchantAcquirerId!!)
                            if (submerchantList != null && submerchantList!!.size> 0)
                                submerchantList.forEach { it2 ->
                                    if (it2.systemGenerated && it2.generatedFrom == "institution") {
                                        it2.subMerchantAcquirerName = institutionReq.insitutionName
                                        it2.subMerchantAcquirerLogo = institutionReq.institutionLogo
                                        it2.clientSubMerchantAcquirerId = institutionReq.insitutionPreferenceId
                                        it2.address = institutionReq.address
                                        it2.adminDetails = institutionReq.adminDetails
                                        it2.bankDetails = institutionReq.bankDetails
                                        it2.contactDetails = institutionReq.contactDetails
                                        it2.info = institutionReq.info
                                        it2.others = institutionReq.others
                                    }
                                    subMerchantRepository.saveAll(submerchantList!!)
                                }
                        }
                    }
                merchantAcquirerRepository.saveAll(merchantList!!)
            }

            if (institution.insitutionStatus.isNotEmpty() && institution.insitutionStatus == "active" && institutionReq.insitutionStatus == "block") {
                merchantGroupList?.forEach { it ->
                    if (!it.isParentBlocked) {
                        it.isParentBlocked = true
                    }
                    if (it.merchantGroupStatus != "block") {
                        /*
                                                var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                                merchantList =
                                                    merchantAcquirerRepository.findByMerchantGroupPreferenceId(it.merchantGroupPreferenceId!!)
                        */
                        merchantList?.forEach { it1 ->
                            if (!it1.isParentBlocked) {
                                it1.isParentBlocked = true
                            }

                            blockUnblockSubMerchant(it1, true)
                        }
                        merchantAcquirerRepository.saveAll(merchantList!!)
                    }
                }
            } else if (institution.insitutionStatus.isNotEmpty() && institution.insitutionStatus == "block" && institutionReq.insitutionStatus == "active") {
                if (merchantGroupList != null && merchantGroupList.size> 0) {
                    merchantGroupList.forEach {
                        if (it.isParentBlocked) {
                            it.isParentBlocked = false
                        }
                        if (it.merchantGroupStatus != "block") {
                            /*                            var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                                                        merchantList =
                                                            merchantAcquirerRepository.findByMerchantGroupPreferenceId(it.merchantGroupPreferenceId!!)*/
                            if (merchantList != null && merchantList!!.size> 0)
                                merchantList?.forEach { it1 ->
                                    if (it1.isParentBlocked) {
                                        it1.isParentBlocked = false
                                    }
                                    blockUnblockSubMerchant(it1, false)
                                }
                            merchantAcquirerRepository.saveAll(merchantList!!)
                        }
                    }
                }
            } else if (institutionReq.insitutionStatus == "de-active") {
                merchantGroupList =
                    merchantGroupRepository.findByInsitutionPreferenceId(institutionReq.institutionId!!)
                merchantGroupList!!.forEach { it2 ->
                    if (!it2.isParentDeActivated) {
                        it2.isParentDeActivated = true
                        if (it2.merchantGroupStatus != "de-active") {
                            var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                            merchantList =
                                merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                            if (merchantList != null && merchantList!!.size> 0) {

                                merchantList!!.forEach { it3 ->
                                    if (!it3.isParentDeActivated) {
                                        it3.isParentDeActivated = true
                                        deActivateSubMerchant(it3, true)
                                        // merchantAcquirerRepository.save(it3)
                                    }
                                }
                                merchantAcquirerRepository.saveAll(merchantList!!)
                            }
                        }
                    }
                }
                merchantGroupRepository.saveAll(merchantGroupList!!)
            } else if (institutionReq.insitutionStatus == "closed") {
                merchantGroupList =
                    merchantGroupRepository.findByInsitutionPreferenceId(institutionReq.institutionId!!)
                merchantGroupList!!.forEach { it2 ->
                    if (it2.merchantGroupStatus != "closed") {
                        it2.merchantGroupStatus = "closed"
                    }
                    var merchantList: MutableList<MerchantAcquirer>? = mutableListOf()
                    merchantList =
                        merchantAcquirerRepository.findByMerchantGroupPreferenceId(it2.merchantGroupPreferenceId!!)
                    merchantList!!.forEach { it1 ->
                        it1.merchantAcquirerStatus = "closed"
                        closeSubMerchant(it1, true)
                    }
                }
            }
            institution.insitutionStatus = institutionReq.insitutionStatus
            merchantGroupRepository.saveAll(merchantGroupList!!)
            institutionRepository.save(institution)
        } catch (e: Exception) {
            throw Exception("Invalid ID" + e.message)
        }
        return "Updated Successfully"
    }

    //    Merchant Group List
    fun getMerchantGroupList(): MutableList<MerchantGroup>? {
        println("getMerchantGroupList")
        var resultMerchantGroupTableData: MutableList<MerchantGroup> =
            merchantGroupRepository.findAll() as MutableList<MerchantGroup>
        println(resultMerchantGroupTableData + "")
        println(resultMerchantGroupTableData.size.toString() + "")
        return resultMerchantGroupTableData
    }

    // Merchant Group Save
    fun createMerchantGroup(
        merchantGroupDetails: AcquirerDataService.MerchantGroupDetailsSaveRequest
    ): String {
        if (merchantGroupDetails.isDirect!!) {
            if (merchantGroupDetails.parentType == "aggregator" && merchantGroupDetails.parentDataAggregator is Aggregator) {
                var institutionNew = Institution()
                institutionNew.institutionId = merchantGroupDetails.merchantGroup.insitutionPreferenceId!!
                institutionNew.systemGenerated = true
                var institution = updateInstitutionDetials(institutionNew, merchantGroupDetails.parentDataAggregator)
                val institutionSaved = institutionRepository.findByInstitutionId(merchantGroupDetails.merchantGroup.insitutionPreferenceId!!)
                if (institutionSaved !is Institution) {
                    institutionRepository.save(institution)
                }
            }
        }
        merchantGroupRepository.save(merchantGroupDetails.merchantGroup)
        return "Saved Successfully"
    }

    //    Merchant Acquirer List
    fun getMerchantAcquirerList(): MutableList<MerchantAcquirer>? {
        println("getMerchantAcquirerList")
        var resultMerchantAcquirerTableData: MutableList<MerchantAcquirer> =
            merchantAcquirerRepository.findAll() as MutableList<MerchantAcquirer>
        println(resultMerchantAcquirerTableData + "")
        println(resultMerchantAcquirerTableData.size.toString() + "")
        return resultMerchantAcquirerTableData
    }

    // Merchant Acquirer Update
    /*fun acquirerMerchantAcquirerUpdate(merchantAcquirerRequest: MerchantAcquirerRequest): String {
        println("acquirerMerchantAcquirerUpdate")
        var merchantAcquirer: MerchantAcquirer =
            merchantAcquirerRepository.getByMerchantAcquirerId(merchantAcquirerRequest.merchantAcquirerId!!)
        merchantAcquirer.merchantAcquirerPreferenceId = merchantAcquirerRequest.merchantAcquirerPreferenceId
        merchantAcquirer.merchantAcquirerMerchantGroupId = merchantAcquirerRequest.merchantAcquirerMerchantGroupId
        merchantAcquirer.merchantAcquirerName = merchantAcquirerRequest.merchantAcquirerName
        merchantAcquirer.merchantAcquirerStatus = merchantAcquirerRequest.merchantAcquirerStatus
        merchantAcquirer.merchantAcquirerEntityBankDetailsId =
            merchantAcquirerRequest.merchantAcquirerEntityBankDetailsId
        merchantAcquirer.merchantAcquirerEntityAddressId = merchantAcquirerRequest.merchantAcquirerEntityAddressId
        merchantAcquirer.merchantAcquirerEntityContactDetailsId =
            merchantAcquirerRequest.merchantAcquirerEntityContactDetailsId
        merchantAcquirer.merchantAcquirerEntityAdminDetailsId =
            merchantAcquirerRequest.merchantAcquirerEntityAdminDetailsId
        merchantAcquirer.merchantAcquirerEntityOthersId = merchantAcquirerRequest.merchantAcquirerEntityOthersId
        merchantAcquirer.merchantAcquirerEntityInfoId = merchantAcquirerRequest.merchantAcquirerEntityInfoId
        merchantAcquirerRepository.save(merchantAcquirer)
        return "Updated Successfully"
    }*/

    // Merchant Acquirer Save
    /*fun acquirerMerchantAcquirerSave(
        merchantAcquirerRequest: MerchantAcquirerRequest
    ): String {
        println("acquirerMerchantAcquirerSave")
        var merchantAcquirer = MerchantAcquirer(
            merchantAcquirerPreferenceId = merchantAcquirerRequest.merchantAcquirerPreferenceId,
            merchantAcquirerMerchantGroupId = merchantAcquirerRequest.merchantAcquirerMerchantGroupId,
            merchantAcquirerName = merchantAcquirerRequest.merchantAcquirerName,
            merchantAcquirerStatus = merchantAcquirerRequest.merchantAcquirerStatus,
            merchantAcquirerEntityBankDetailsId = merchantAcquirerRequest.merchantAcquirerEntityBankDetailsId,
            merchantAcquirerEntityAddressId = merchantAcquirerRequest.merchantAcquirerEntityAddressId,
            merchantAcquirerEntityContactDetailsId = merchantAcquirerRequest.merchantAcquirerEntityContactDetailsId,
            merchantAcquirerEntityAdminDetailsId = merchantAcquirerRequest.merchantAcquirerEntityAdminDetailsId,
            merchantAcquirerEntityOthersId = merchantAcquirerRequest.merchantAcquirerEntityOthersId,
            merchantAcquirerEntityInfoId = merchantAcquirerRequest.merchantAcquirerEntityInfoId
        )
        merchantAcquirerRepository.save(merchantAcquirer)

        return "Saved Successfully"
    }*/
    fun acquirerMerchantAcquirerSaveV2(
        merchantAcquirerRequest: AcquirerDataService.MerchantDetailsSaveRequest
    ): String {
        println("acquirerMerchantAcquirerSave")
        if (merchantAcquirerRequest.isDirect!!) {
            if (merchantAcquirerRequest.parentType == "aggregator" && merchantAcquirerRequest.parentDataAggregator is Aggregator) {
                var institutionNew = Institution()
                institutionNew.institutionId = merchantAcquirerRequest.merchant.insitutionPreferenceId
                institutionNew.systemGenerated = true
                var institution = updateInstitutionDetials(institutionNew, merchantAcquirerRequest.parentDataAggregator)
                val institutionSaved = institutionRepository.findByInstitutionId(merchantAcquirerRequest.merchant.insitutionPreferenceId!!)
                if (institutionSaved !is Institution) {
                    institutionRepository.save(institution)
                }
                var merchantGroupNew = MerchantGroup()
                merchantGroupNew.systemGenerated = true
                merchantGroupNew.generatedFrom = "aggregator"
                merchantGroupNew.merchantGroupPreferenceId = merchantAcquirerRequest.merchant.merchantGroupPreferenceId
                var merchantGroup = updateMerchantGroupDetials(merchantGroupNew, institution)
                val merchantGroupSaved = merchantGroupRepository.findByMerchantGroupPreferenceId(merchantAcquirerRequest.merchant.merchantGroupPreferenceId!!)
                if (merchantGroupSaved !is MerchantGroup) {
                    merchantGroupRepository.save(merchantGroup)
                }
            } else if (merchantAcquirerRequest.parentType == "institution" && merchantAcquirerRequest.parentDataInstitution is Institution) {
                var merchantGroupNew = MerchantGroup()
                merchantGroupNew.merchantGroupPreferenceId = merchantAcquirerRequest.merchant.merchantGroupPreferenceId
                merchantGroupNew.systemGenerated = true
                merchantGroupNew.generatedFrom = "institution"
                var merchantGroup =
                    updateMerchantGroupDetials(merchantGroupNew, merchantAcquirerRequest.parentDataInstitution)
                val merchantGroupSaved = merchantGroupRepository.findByMerchantGroupPreferenceId(merchantAcquirerRequest.merchant.merchantGroupPreferenceId!!)
                if (merchantGroupSaved !is MerchantGroup) {
                    merchantGroupRepository.save(merchantGroup)
                }
            }
        }
        merchantAcquirerRepository.save(merchantAcquirerRequest.merchant)

        return "Saved Successfully"
    }

    // Sub Merchant Delete
    fun subMerchantDelete(id: Long): String {
        println("subMerchantDelete")
        return try {
            subMerchantRepository.deleteById(id)
            "Deleted Successfully"
        } catch (e: Exception) {
            println(e)
            e.message.toString()
        }
    }

    //    Sub Merchant List
    fun getSubMerchantList(): MutableList<SubMerchantAcquirer>? {
        println("getSubMerchantList")
        var resultSubMerchantTableData: MutableList<SubMerchantAcquirer> =
            subMerchantRepository.findAll() as MutableList<SubMerchantAcquirer>
        println(resultSubMerchantTableData + "")
        println(resultSubMerchantTableData.size.toString() + "")
        return resultSubMerchantTableData
    }

    // Sub Merchant Update
    fun updateSubMerchant(subMerchantRequest: SubMerchantAcquirer): String {
        var subMerchantAcquirer: SubMerchantAcquirer? =
            subMerchantRepository.findBySubMerchantAcquirerId(subMerchantRequest.subMerchantAcquirerId!!)
        if (subMerchantAcquirer is SubMerchantAcquirer) {
            subMerchantAcquirer.aggregatorPreferenceId = subMerchantRequest.aggregatorPreferenceId
            subMerchantAcquirer.insitutionPreferenceId = subMerchantRequest.insitutionPreferenceId
            subMerchantAcquirer.merchantGroupPreferenceId = subMerchantRequest.merchantGroupPreferenceId
            subMerchantAcquirer.merchantAcquirerPreferenceId = subMerchantRequest.merchantAcquirerPreferenceId
            subMerchantAcquirer.subMerchantAcquirerId = subMerchantRequest.subMerchantAcquirerId
            subMerchantAcquirer.subMerchantAcquirerName = subMerchantRequest.subMerchantAcquirerName
            subMerchantAcquirer.clientSubMerchantAcquirerId = subMerchantRequest.clientSubMerchantAcquirerId
            subMerchantAcquirer.subMerchantAcquirerLogo = subMerchantRequest.subMerchantAcquirerLogo
            subMerchantAcquirer.address = subMerchantRequest.address
            subMerchantAcquirer.adminDetails = subMerchantRequest.adminDetails
            subMerchantAcquirer.bankDetails = subMerchantRequest.bankDetails
            subMerchantAcquirer.contactDetails = subMerchantRequest.contactDetails
            subMerchantAcquirer.info = subMerchantRequest.info
            subMerchantAcquirer.others = subMerchantRequest.others
            var outletList: MutableList<Outlet>? = mutableListOf()
            outletList = outletRepository.findBySubMerchantPreferenceId(subMerchantAcquirer.subMerchantAcquirerId!!)
            if (subMerchantRequest.subMerchantAcquirerStatus == "block" && subMerchantAcquirer.subMerchantAcquirerStatus == "active") {
                if (outletList != null && outletList.size> 0) {
                    outletList.forEach {
                        if (!it.isParentBlocked) {
                            it.isParentBlocked = true
                        }
                    }
                    outletRepository.saveAll(outletList)
                }
            } else if (subMerchantRequest.subMerchantAcquirerStatus == "active" && subMerchantAcquirer.subMerchantAcquirerStatus == "block") {
                if (outletList != null && outletList.size> 0) {
                    outletList.forEach {
                        if (it.isParentBlocked) {
                            it.isParentBlocked = false
                        }
                    }
                    outletRepository.saveAll(outletList)
                }
            }
            if (subMerchantRequest.subMerchantAcquirerStatus == "de-active") {
                deActivateOutlet(subMerchantAcquirer, true)
            } else if (subMerchantRequest.subMerchantAcquirerStatus == "closed") {
                closeOutlet(subMerchantAcquirer, true)
            }
            subMerchantAcquirer.subMerchantAcquirerStatus = subMerchantRequest.subMerchantAcquirerStatus
            subMerchantRepository.save(subMerchantAcquirer)
            return "Updated Successfully"
        }
        return "SubMerchant Not found"
    }

    /*
    @Throws(RuntimeException::class)
*/
    @Transactional
    fun updateOutlet(outletRequest: Outlet): String? {
        var outlet: Outlet? = outletRepository.findByOutletId(outletRequest.outletId!!)
        if (outlet is Outlet) {
            outlet.aggregatorPreferenceId = outletRequest.aggregatorPreferenceId
            outlet.insitutionPreferenceId = outletRequest.insitutionPreferenceId
            outlet.merchantGroupPreferenceId = outletRequest.merchantGroupPreferenceId
            outlet.merchantAcquirerPreferenceId = outletRequest.merchantAcquirerPreferenceId
            outlet.subMerchantPreferenceId = outletRequest.subMerchantPreferenceId
            outlet.outletName = outletRequest.outletName
            outlet.clientOutletId = outletRequest.clientOutletId
            outlet.outletStatus = outletRequest.outletStatus
            outlet.outletLogo = outletRequest.outletLogo
            outlet.address = outletRequest.address
            outlet.adminDetails = outletRequest.adminDetails
            outlet.bankDetails = outletRequest.bankDetails
            outlet.contactDetails = outletRequest.contactDetails
            outlet.info = outletRequest.info
            outlet.others = outletRequest.others
            /*            val posObjList: MutableList<Pos> = posRepository.findByOutlet(outlet)
                        outlet.posList.removeAll(outlet.posList)
                        entityManager.flush()
                        outlet.posList.addAll(outletRequest.posList)*/
            var posList: MutableList<Pos> = mutableListOf()
            posList = posRepository.findPosByOutletPreferenceId(outlet.outletId!!).toList().toMutableList()
            if (outletRequest.outletStatus == "block" && outlet.outletStatus == "active") {
                if (posList != null && posList.size> 0) {
                    posList.forEach {
                        if (!it.isParentBlocked) {
                            it.isParentBlocked = true
                        }
                    }
                    posRepository.saveAll(posList)
                }
            } else if (outletRequest.outletStatus == "active" && outlet.outletStatus == "block") {
                if (posList != null && posList.size> 0) {
                    posList.forEach {
                        if (it.isParentBlocked) {
                            it.isParentBlocked = false
                        }
                    }
                    posRepository.saveAll(posList)
                }
            }
            try {
                if (outletRequest.outletStatus == "de-active") {
                    posList.forEach {
                        if (!it.isParentDeActivated) {
                            it.isParentDeActivated = true
                        }
                    }
                    posRepository.saveAll(posList)
                } else if (outletRequest.outletStatus == "closed") {
                    posList.forEach {
                        if (it.status != "closed") {
                            it.status = "closed"
                        }
                        posRepository.saveAll(posList)
                    }
                }
                outletRepository.save(outlet)
            } catch (e: ConstraintViolationException) {
                throw RuntimeException("Application ConstraintViolationException")
            } catch (e: RuntimeException) {
                throw RuntimeException("Application Error")
            }
            return "Updated Successfully"
        }
        return "Cannot find Outlet"
    }

    @Transactional(rollbackOn = arrayOf(ConstraintViolationException::class))
    @Throws(ConstraintViolationException::class)
    fun updateOutletThroughFile(outletRequest: Outlet): String? {
        var outlet: Outlet? = outletRepository.findByOutletId(outletRequest.outletId!!)
        if (outlet is Outlet) {
            outlet.aggregatorPreferenceId = outletRequest.aggregatorPreferenceId
            outlet.insitutionPreferenceId = outletRequest.insitutionPreferenceId
            outlet.merchantGroupPreferenceId = outletRequest.merchantGroupPreferenceId
            outlet.merchantAcquirerPreferenceId = outletRequest.merchantAcquirerPreferenceId
            outlet.subMerchantPreferenceId = outletRequest.subMerchantPreferenceId
            outlet.outletName = outletRequest.outletName
            val others = entityOthersRepository.save(outletRequest.others)
            val info = entityInfoRepository.save(outletRequest.info)
            val bank = entityBankDetailsRepository.save(outletRequest.bankDetails)
            var admin: EntityAdminDetails? = null
            try {
                admin = entityAdminDetailsRepository.save(outletRequest.adminDetails)
            } catch (e: ConstraintViolationException) {
                throw e
                // ("Application ConstraintViolationException")
            }
            var address: EntityAddress? = null
            try {
                address = entityAddressRepository.save(outletRequest.address)
            } catch (e: ConstraintViolationException) {
                throw e
                // ("Application ConstraintViolationException")
            }

            val contact = entityContactDetailsRepository.save(outletRequest.contactDetails)

            outlet.outletStatus = outletRequest.outletStatus
            outlet.outletLogo = outletRequest.outletLogo
            outlet.address = address
            outlet.adminDetails = admin
            outlet.bankDetails = bank
            outlet.contactDetails = contact
            outlet.info = info
            outlet.others = others

            try {
                outletRepository.save(outlet)
            } catch (e: ConstraintViolationException) {
                throw e
                // ("Application ConstraintViolationException")
            } catch (e: RuntimeException) {
                throw RuntimeException("Application Error")
            }
            return "Updated Successfully"
        }
        return "Cannot find Outlet"
    }

    @Transactional
    fun updatePos(posRequest: Pos): String {
        var pos: Pos =
            posRepository.findUniquePosByQuery(posRequest.posUniqueId, posRequest.aggregatorPreferenceId!!, posRequest.insitutionPreferenceId!!, posRequest.merchantGroupPreferenceId!!, posRequest.merchantAcquirerPreferenceId!!, posRequest.subMerchantPreferenceId!!, posRequest.outletPreferenceId)!!
        pos.posId = posRequest.posId
        pos.outletPreferenceId = posRequest.outletPreferenceId
        pos.posIPAddress = posRequest.posIPAddress
        pos.posManufacturer = posRequest.posManufacturer
        pos.posModel = posRequest.posModel
        pos.posFirmwareVersion = posRequest.posFirmwareVersion
        pos.posMacAddress = posRequest.posMacAddress
        pos.posSerialNum = posRequest.posSerialNum
        pos.status = posRequest.status
        posRepository.save(pos)
        return "Updated Successfully"
    }

    // poslist by outlet
    fun getPosListByOutletId(posDetailsRequest: AcquirerDataService.PosDetailsRequest): MutableList<Pos>? {
        var resultPos: MutableList<Pos> =
            posRepository.findPosRecordsByAllMatchedCriteria(
                posDetailsRequest.aggregatorPreferenceId!!,
                posDetailsRequest.institutionPreferenceId!!,
                posDetailsRequest.merchantGroupPreferenceId!!,
                posDetailsRequest.merchantAcquirerPreferenceId!!,
                posDetailsRequest.subMerchantPreferenceId!!,
                posDetailsRequest.outletPreferenceId!!
            ) as MutableList<Pos>
        println(resultPos.size.toString() + "")
        return resultPos
    }
    fun getPosList(posDetailsRequest: AcquirerDataService.PosDetailsRequest): AcquirerDataService.PosResponse? {

        if (posDetailsRequest.aggregatorPreferenceId != null && posDetailsRequest.institutionPreferenceId != null && posDetailsRequest.merchantGroupPreferenceId != null && posDetailsRequest.merchantAcquirerPreferenceId != null && posDetailsRequest.subMerchantPreferenceId != null && posDetailsRequest.outletPreferenceId != null) {

            var posListWithAggregator = posRepository.findRecordsWithParent(
                posDetailsRequest.aggregatorPreferenceId!!,
                posDetailsRequest.institutionPreferenceId!!,
                posDetailsRequest.merchantGroupPreferenceId!!,
                posDetailsRequest.merchantAcquirerPreferenceId!!,
                posDetailsRequest.subMerchantPreferenceId!!,
                posDetailsRequest.outletPreferenceId
            ) as List<PosListing>
            if (posDetailsRequest.status != null) {
                posListWithAggregator = posListWithAggregator.filter { e ->
                    e.pos.status.contains(posDetailsRequest.status!!, ignoreCase = true) ?: false
                }
            }
            if (posDetailsRequest.posId != null) {
                posListWithAggregator = posListWithAggregator.filter { e ->
                    e.pos.posId.contains(posDetailsRequest.posId!!, ignoreCase = true) ?: false
                }
            }
            if (posDetailsRequest.duration> 0) {
                val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                posListWithAggregator = posListWithAggregator.filter { e ->
                    e.outlet.others.entityOthersActivationDate != null && LocalDate.parse(e.outlet.others.entityOthersActivationDate, dateFormatterDb) > (
                        LocalDate.now()
                            .minusDays(posDetailsRequest.duration)
                        )
                }
            }
            var pagination = AcquirerDataService.Pagination(
                current_page = posDetailsRequest.page,
                total_records = posListWithAggregator.size,
                total_pages = calculateTotalNoPages(
                    posListWithAggregator.size.toDouble(),
                    if (posDetailsRequest.limit > 0) posDetailsRequest.limit.toDouble() else posListWithAggregator.size.toDouble()
                )
            )

            if (posDetailsRequest.page != null && posDetailsRequest.page > 0) {
                val pageNo = posDetailsRequest.page - 1
                posListWithAggregator =
                    posListWithAggregator.stream().skip(pageNo * posDetailsRequest.limit)
                        .limit(posDetailsRequest.limit).toList()
            }
            return AcquirerDataService.PosResponse(
                posListWithAggregator,
                pagination
            )
        }
        return null
    }
    fun getOutletList(outletDetailsRequest: AcquirerDataService.OutletDetailsRequest): AcquirerDataService.OutletResponse? {
        if (outletDetailsRequest.aggregatorPreferenceId != null && outletDetailsRequest.institutionPreferenceId != null && outletDetailsRequest.merchantGroupPreferenceId != null && outletDetailsRequest.merchantAcquirerPreferenceId != null && outletDetailsRequest.subMerchantPreferenceId != null) {
            var outletListWithAggregator = outletRepository.findByRecordsWithParent(
                outletDetailsRequest.aggregatorPreferenceId!!,
                outletDetailsRequest.institutionPreferenceId!!,
                outletDetailsRequest.merchantGroupPreferenceId!!,
                outletDetailsRequest.merchantAcquirerPreferenceId!!,
                outletDetailsRequest.subMerchantPreferenceId!!
            ) as List<OutletListing>
            if (outletDetailsRequest.status != null) {
                outletListWithAggregator = outletListWithAggregator.filter { e ->
                    e.outlet.outletStatus?.contains(outletDetailsRequest.status!!, ignoreCase = true) ?: false
                }
            }
            if (outletDetailsRequest.outletName != null) {
                outletListWithAggregator = outletListWithAggregator.filter { e ->
                    e.outlet.outletName?.contains(outletDetailsRequest.outletName!!, ignoreCase = true) ?: false
                }
            }
            if (outletDetailsRequest.outletId != null) {
                outletListWithAggregator = outletListWithAggregator.filter { e ->
                    e.outlet.outletId?.contains(outletDetailsRequest.outletId!!, ignoreCase = true) ?: false
                }
            }
            if (outletDetailsRequest.duration> 0) {
                val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                outletListWithAggregator = outletListWithAggregator.filter { e ->
                    e.outlet.others.entityOthersActivationDate != null && LocalDate.parse(e.outlet.others.entityOthersActivationDate, dateFormatterDb) > (
                        LocalDate.now()
                            .minusDays(outletDetailsRequest.duration)
                        )
                }
            }
            var pagination = AcquirerDataService.Pagination(
                current_page = outletDetailsRequest.page,
                total_records = outletListWithAggregator.size,
                total_pages = calculateTotalNoPages(
                    outletListWithAggregator.size.toDouble(),
                    if (outletDetailsRequest.limit > 0) outletDetailsRequest.limit.toDouble() else outletListWithAggregator.size.toDouble()
                )
            )

            if (outletDetailsRequest.page != null && outletDetailsRequest.page > 0) {
                val pageNo = outletDetailsRequest.page - 1
                outletListWithAggregator =
                    outletListWithAggregator.stream().skip(pageNo * outletDetailsRequest.limit)
                        .limit(outletDetailsRequest.limit).toList()
            }
            return AcquirerDataService.OutletResponse(
                pagination.total_records,
                outletListWithAggregator,
                pagination
            )
        }
        /*        var outletListData = outletRepository.findAll() as List<Outlet>
                if (outletDetailsRequest.outletName != null) {
                    outletListData = outletListData.filter { e ->
                        e.outletName?.contains(outletDetailsRequest.outletName, ignoreCase = true)
                            ?: false
                    }
                }
                if (outletDetailsRequest.status != null) {
                    outletListData = outletListData.filter { e ->
                        e.outletStatus?.contains(outletDetailsRequest.status!!, ignoreCase = true) ?: false
                    }
                }
                var pagination = AcquirerDataService.Pagination(
                    current_page = outletDetailsRequest.page,
                    total_records = outletListData.size,
                    total_pages = calculateTotalNoPages(
                        outletListData.size.toDouble(),
                        if (outletDetailsRequest.limit > 0) outletDetailsRequest.limit.toDouble() else outletListData.size.toDouble()
                    )
                )

                if (outletDetailsRequest.page != null && outletDetailsRequest.page > 0) {
                    val pageNo = outletDetailsRequest.page - 1
                    outletListData =
                        outletListData.stream().skip(pageNo * outletDetailsRequest.limit)
                            .limit(outletDetailsRequest.limit).toList()
                }

                return AcquirerDataService.OutletResponse(
                    outletListData,
                    pagination
                )*/
        return null
    }

    // Sub Merchant Save
    fun acquirerSubMerchantAcquirerSave(
        subMerchantAcquirer: SubMerchantAcquirer
    ): String {
        println("acquirerSubMerchantAcquirerSave")
        subMerchantRepository.save(subMerchantAcquirer)
        return "Saved Successfully"
    }

    //
    fun updateListPendingToActive() {
        var resultOtherTableData: MutableList<EntityOthers> = mutableListOf()
        resultOtherTableData = entityOthersRepository.findAll() as MutableList<EntityOthers>
        val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val todayDateStr: String = LocalDate.now().format(dateFormatterDb).toString()
        resultOtherTableData =
            resultOtherTableData.filter {
                r ->
                r.entityOthersActivationDate.toString().contains(todayDateStr.trim())
            }
                .toMutableList()

        for (i in 1..resultOtherTableData.size) {
            val aggregator =
                aggregatorRepository.getAggregatorByOthersId(resultOtherTableData[i - 1].id)
            val institution =
                institutionRepository.getInstitutionByOthersId(resultOtherTableData[i - 1].id)
            val merchantGroup =
                merchantGroupRepository.getMerchantGroupByOthersId(resultOtherTableData[i - 1].id)
            val merchant =
                merchantAcquirerRepository.getMerchantAcquirerByOthersId(resultOtherTableData[i - 1].id)
            val subMerchant =
                subMerchantRepository.getSubMerchantAcquirerByOthersId(resultOtherTableData[i - 1].id)
            val outlet =
                outletRepository.getOutletByOthersId(resultOtherTableData[i - 1].id)

            if ((aggregator != null) && (aggregator.aggregatorStatus == "pending")) {
                updateAggregatorStatus(aggregator, "active")
            }
            if ((institution != null) && (institution.insitutionStatus == "pending")) {
                updateInstitutionStatus(institution, "active")
            }
            if ((merchantGroup != null) && (merchantGroup.merchantGroupStatus == "pending")) {
                updateMerchantGroupStatus(merchantGroup, "active")
            }
            if ((merchant != null) && (merchant.merchantAcquirerStatus == "pending")) {
                updateMerchantStatus(merchant, "active")
            }
            if ((subMerchant != null) && (subMerchant.subMerchantAcquirerStatus == "pending")) {
                updateSubMerchantStatus(subMerchant, "active")
            }
            if ((outlet != null) && (outlet.outletStatus == "pending")) {
                updateOutletStatus(outlet, "active")
            }
        }
    }
    fun updateListActiveToDeActivate() {
        var resultOtherTableData: MutableList<EntityOthers> = mutableListOf()
        resultOtherTableData = entityOthersRepository.findAll() as MutableList<EntityOthers>
        val dateFormatterDb: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val todayDateStr: String = LocalDate.now().format(dateFormatterDb).toString()
        resultOtherTableData =
            resultOtherTableData.filter {
                r ->
                r.entityOthersExpiryDate.toString().contains(todayDateStr.trim())
            }
                .toMutableList()

        for (i in 1..resultOtherTableData.size) {
            val aggregator =
                aggregatorRepository.getAggregatorByOthersId(resultOtherTableData[i - 1].id)
            val institution =
                institutionRepository.getInstitutionByOthersId(resultOtherTableData[i - 1].id)
            val merchantGroup =
                merchantGroupRepository.getMerchantGroupByOthersId(resultOtherTableData[i - 1].id)
            val merchant =
                merchantAcquirerRepository.getMerchantAcquirerByOthersId(resultOtherTableData[i - 1].id)
            val subMerchant =
                subMerchantRepository.getSubMerchantAcquirerByOthersId(resultOtherTableData[i - 1].id)
            val outlet =
                outletRepository.getOutletByOthersId(resultOtherTableData[i - 1].id)

            if ((aggregator != null) && (aggregator.aggregatorStatus == "active")) {
                updateAggregatorStatus(aggregator, "de-active")
            }
            if ((institution != null) && (institution.insitutionStatus == "active")) {
                updateInstitutionStatus(institution, "de-active")
            }
            if ((merchantGroup != null) && (merchantGroup.merchantGroupStatus == "active")) {
                updateMerchantGroupStatus(merchantGroup, "de-active")
            }
            if ((merchant != null) && (merchant.merchantAcquirerStatus == "active")) {
                updateMerchantStatus(merchant, "de-active")
            }
            if ((subMerchant != null) && (subMerchant.subMerchantAcquirerStatus == "active")) {
                updateSubMerchantStatus(subMerchant, "de-active")
            }
            if ((outlet != null) && (outlet.outletStatus == "active")) {
                updateOutletStatus(outlet, "de-active")
            }
        }
    }

    fun updateAggregatorStatus(aggregatorReq: Aggregator, status: String) {
        aggregatorReq.aggregatorStatus = status
        acquirerAggregatorUpdateV2(aggregatorReq)
    }
    fun updateInstitutionStatus(institution: Institution, status: String) {
        institution.insitutionStatus = status
        acquirerInstitutionUpdateV2(institution)
    }
    fun updateMerchantGroupStatus(merchantGroup: MerchantGroup, status: String) {
        merchantGroup.merchantGroupStatus = status
        acquirerMerchantGroupUpdateV2(merchantGroup)
    }
    fun updateMerchantStatus(merchantAcquirer: MerchantAcquirer, status: String) {
        merchantAcquirer.merchantAcquirerStatus = status
        acquirerMerchantAcquirerUpdate(merchantAcquirer)
    }
    fun updateSubMerchantStatus(subMerchant: SubMerchantAcquirer, status: String) {
        subMerchant.subMerchantAcquirerStatus = status
        updateSubMerchant(subMerchant)
    }
    fun updateOutletStatus(outlet: Outlet, status: String) {
        outlet.outletStatus = status
        updateOutlet(outlet)
    }
    fun updateAggregatorStatusToDeActive(aggregatorReq: Aggregator) {
        aggregatorReq.aggregatorStatus = "de-active"
        acquirerAggregatorUpdateV2(aggregatorReq)
    }

    @Scheduled(cron = "0 0 6,19 * * *")
    fun runScheduledToAggregatorActive() {
        println("Scheduled task executed for Aggregator Pending To Active!")
        updateListPendingToActive()
    }
    @Scheduled(cron = "0 0 6,19 * * *")
    fun runScheduledToAggregatorInActive() {
        println("Scheduled task executed for Aggregator Active To De-active!")
        updateListActiveToDeActivate()
    }

    fun blockUnblockOutlet(subMerchant: SubMerchantAcquirer, block: Boolean) {
        var outletList: MutableList<Outlet>? = mutableListOf()
        if (subMerchant.subMerchantAcquirerStatus != "block") {
            if (block) {
                outletList =
                    outletRepository.findBySubMerchantPreferenceId(subMerchant.subMerchantAcquirerId!!)
                if (outletList != null && outletList.size> 0) {
                    outletList.forEach { it5 ->
                        if (!it5.isParentBlocked) {
                            it5.isParentBlocked = true
                        }
                        println("outlet u ${it5.outletId}")
                        outletRepository.save(it5)
                    }
                    // outletList.forEach{ism->outletRepository.save(ism)}

                    /*outletRepository.saveAll(outletList)*/
                }
            }
            if (!block) {
                outletList =
                    outletRepository.findBySubMerchantPreferenceId(subMerchant.subMerchantAcquirerId!!)
                if (outletList != null && outletList.size> 0) {
                    outletList.forEach { it5 ->
                        if (it5.isParentBlocked) {
                            it5.isParentBlocked = false
                        }
                        println("outlet ${it5.outletId}")
                        outletRepository.save(it5)
                    }
                    // outletRepository.saveAll(outletList)
                }
            }
        }
    }
    fun deActivateOutlet(subMerchant: SubMerchantAcquirer, deActive: Boolean) {
        var outletList: MutableList<Outlet>? = mutableListOf()
        if (subMerchant.subMerchantAcquirerStatus != "de-active") {
            if (deActive) {
                outletList =
                    outletRepository.findBySubMerchantPreferenceId(subMerchant.subMerchantAcquirerId!!)
                if (outletList != null && outletList.size> 0) {
                    outletList.forEach { it5 ->
                        if (!it5.isParentDeActivated) {
                            it5.isParentDeActivated = true
                        }
                        outletRepository.save(it5)
                    }
                    // outletList.forEach{ism->outletRepository.save(ism)}

                    /*outletRepository.saveAll(outletList)*/
                }
            }
            if (!deActive) {
                outletList =
                    outletRepository.findBySubMerchantPreferenceId(subMerchant.subMerchantAcquirerId!!)
                if (outletList != null && outletList.size> 0) {
                    outletList.forEach { it5 ->
                        if (it5.isParentDeActivated) {
                            it5.isParentDeActivated = false
                        }
                        println("outlet ${it5.outletId}")
                        outletRepository.save(it5)
                    }
                    // outletRepository.saveAll(outletList)
                }
            }
        }
    }
    fun closeOutlet(subMerchant: SubMerchantAcquirer, close: Boolean) {
        var outletList: MutableList<Outlet>? = mutableListOf()
        if (close) {
            outletList =
                outletRepository.findBySubMerchantPreferenceId(subMerchant.subMerchantAcquirerId!!)
            if (outletList != null && outletList.size> 0) {
                outletList.forEach { it5 ->
                    it5.outletStatus = "closed"
                    var posList: MutableList<Pos>? = mutableListOf()
                    posRepository.findPosByOutletPreferenceId(it5.outletId!!)
                    posList?.forEach { it6 ->
                        it6.status = "closed"
                        posRepository.save(it6)
                    }
                    outletRepository.save(it5)
                }
                // outletList.forEach{ism->outletRepository.save(ism)}

                /*outletRepository.saveAll(outletList)*/
            }
        }
    }

    fun blockUnblockSubMerchant(merchantAcquirer: MerchantAcquirer, block: Boolean) {
        if (merchantAcquirer.merchantAcquirerStatus != "blocked") {
            var submerchantList: MutableList<SubMerchantAcquirer>? = mutableListOf()
            if (block) {
                submerchantList =
                    subMerchantRepository.findByMerchantAcquirerPreferenceId(merchantAcquirer.merchantAcquirerId!!)
                if (submerchantList != null && submerchantList.size> 0) {
                    submerchantList.forEach { it4 ->
                        if (!it4.isParentBlocked) {
                            it4.isParentBlocked = true
                            println("submerchnat ${it4.subMerchantAcquirerId}")
                            blockUnblockOutlet(it4, true)
                            subMerchantRepository.save(it4)
                        }
                    }
                    // submerchantList.forEach{ism->subMerchantRepository.save(ism)}
                }
            } else {
                submerchantList =
                    subMerchantRepository.findByMerchantAcquirerPreferenceId(merchantAcquirer.merchantAcquirerId!!)
                if (submerchantList != null && submerchantList.size> 0) {
                    submerchantList.forEach { it4 ->
                        println("submerchnatu ${it4.subMerchantAcquirerId}")

                        if (it4.isParentBlocked) {
                            it4.isParentBlocked = false
                            blockUnblockOutlet(it4, false)
                            subMerchantRepository.save(it4)
                        }
                    }
                    // submerchantList.forEach{ism->subMerchantRepository.save(ism)}
                }
            }
        }
    }
    fun deActivateSubMerchant(merchantAcquirer: MerchantAcquirer, deActive: Boolean) {
        if (merchantAcquirer.merchantAcquirerStatus != "de-active") {
            var submerchantList: MutableList<SubMerchantAcquirer>? = mutableListOf()
            if (deActive) {
                submerchantList =
                    subMerchantRepository.findByMerchantAcquirerPreferenceId(merchantAcquirer.merchantAcquirerId!!)
                if (submerchantList != null && submerchantList.size> 0) {
                    submerchantList.forEach { it4 ->
                        if (!it4.isParentDeActivated) {
                            it4.isParentDeActivated = true
                            deActivateOutlet(it4, true)
                            subMerchantRepository.save(it4)
                        }
                    }
                    // submerchantList.forEach{ism->subMerchantRepository.save(ism)}
                }
            } else {
                submerchantList =
                    subMerchantRepository.findByMerchantAcquirerPreferenceId(merchantAcquirer.merchantAcquirerId!!)
                if (submerchantList != null && submerchantList.size> 0) {
                    submerchantList.forEach { it4 ->
                        println("submerchnatu ${it4.subMerchantAcquirerId}")

                        if (it4.isParentDeActivated) {
                            it4.isParentDeActivated = false
                            deActivateOutlet(it4, false)
                            subMerchantRepository.save(it4)
                        }
                    }
                    // submerchantList.forEach{ism->subMerchantRepository.save(ism)}
                }
            }
        }
    }
    fun closeSubMerchant(merchantAcquirer: MerchantAcquirer, closed: Boolean) {
        var submerchantList: MutableList<SubMerchantAcquirer>? = mutableListOf()
        if (closed) {
            submerchantList =
                subMerchantRepository.findByMerchantAcquirerPreferenceId(merchantAcquirer.merchantAcquirerId!!)
            if (submerchantList != null && submerchantList.size> 0) {
                submerchantList.forEach { it4 ->
                    it4.subMerchantAcquirerStatus = "closed"
                    closeOutlet(it4, true)
                    subMerchantRepository.save(it4)
                }
            }
        }
        // submerchantList.forEach{ism->subMerchantRepository.save(ism)}
    }

    fun instutionList(it: Aggregator): MutableList<InstitutionList> {
        var institutionList: MutableList<InstitutionList> = ArrayList<InstitutionList>()
        var institution: MutableList<Institution> = ArrayList<Institution>()
        institution =
            it?.aggregatorPreferenceId?.let { it1 ->
                institutionRepository.findByAggregatorPreferenceIdOrderByInstitutionId(
                    it1
                )
            }!!/*.sortedBy { list->list.institutionId } as MutableList<Institution>*/
        var institutionArray: MutableList<String> = mutableListOf<String>()
        if (institution.size> 0) {
            institution.forEach {
                println("inside institutions   ")
                var institutionL1: InstitutionList = InstitutionList()
                institutionL1.institutionName = it.insitutionName
                institutionL1.institutionId = it.institutionId
                institutionL1.institutionPreferenceId = it.insitutionPreferenceId
                institutionL1.institutionStatus = it.insitutionStatus
                institutionL1.institutionImage = "test"
                institutionL1.institutionLogo = it.institutionLogo
                institutionL1.systemGenerated = it.systemGenerated
                var institutionArray: MutableList<String> = mutableListOf<String>()
                institutionL1.merchantGroup = merchantGroupListFetch(it)
                institutionList.add(institutionL1)
            }
        }
        return institutionList
    }

    fun merchantGroupListFetch(it: Institution): MutableList<MerchantGroupList> {
        var merchantGroupList: MutableList<MerchantGroupList> = ArrayList<MerchantGroupList>()
        var merchantGroup: MutableList<MerchantGroup> = ArrayList<MerchantGroup>()
        var merchantGroupLists124: MutableList<MerchantGroupList> = ArrayList<MerchantGroupList>()
        merchantGroup =
            it.let { it1 ->
                it.aggregatorPreferenceId?.let { it2 ->
                    it.institutionId?.let { it3 ->
                        merchantGroupRepository.findByUniqueIdOrderByMerchantGroupPreferecneId(
                            it3,
                            it2
                        )
                    }
                }
            }!!
        if (merchantGroup.size> 0) {
            merchantGroup.forEach {
                println("inside institutions   ")
                var merchantGroupList: MerchantGroupList = MerchantGroupList()
                merchantGroupList.merchantGroupName = it.merchantGroupName
                merchantGroupList.merchantGroupPreferenceId = it.merchantGroupPreferenceId
                merchantGroupList.merchantGroupStatus = it.merchantGroupStatus
                merchantGroupList.merchantGroupImage = "test"
                merchantGroupList.systemGenerated = it.systemGenerated
                merchantGroupList.merchants = getMerchantTreeList(it)
                merchantGroupLists124.add(merchantGroupList)
            }
        }
        return merchantGroupLists124
    }

    fun getMerchantTreeList(it: MerchantGroup): MutableList<MarchantAcquirerList> {
        var merchantAcquirerList1234: MutableList<MarchantAcquirerList> = ArrayList<MarchantAcquirerList>()
        var merchantAcquirer: MutableList<MerchantAcquirer> = ArrayList<MerchantAcquirer>()
        merchantAcquirer =
            (it.aggregatorPreferenceId)?.let { it1 ->
                it.insitutionPreferenceId?.let { it2 ->
                    it.merchantGroupPreferenceId?.let { it3 ->
                        merchantAcquirerRepository.findByUniqueIdOrderByMerchantAcquirerId(
                            it.aggregatorPreferenceId!!,
                            it2, it3
                        )
                    }
                }
            }!!

        if (merchantAcquirer.size> 0) {
            merchantAcquirer.forEach {
                var merchantAcquirerList: MarchantAcquirerList = MarchantAcquirerList()
                merchantAcquirerList.merchantAcquirerId = it.merchantAcquirerId
                merchantAcquirerList.merchantAcquirerName = it.merchantAcquirerName
                merchantAcquirerList.merchantAcquirerStatus = it.merchantAcquirerStatus
                merchantAcquirerList.systemGenerated = it.systemGenerated
                merchantAcquirerList.subMerchants = getSubMerchanTreeList(it)
                merchantAcquirerList1234.add(merchantAcquirerList)
            }
        }
        return merchantAcquirerList1234
    }

    fun getSubMerchanTreeList(it: MerchantAcquirer): MutableList<SubMerchantList> {
        var subMerchantAcquirerList1234: MutableList<SubMerchantList> = ArrayList<SubMerchantList>()
        var subMerchantAcquirer: MutableList<SubMerchantAcquirer> = ArrayList<SubMerchantAcquirer>()

        subMerchantAcquirer = (it.aggregatorPreferenceId)?.let { it1 ->
            it.insitutionPreferenceId?.let { it2 ->
                it.merchantGroupPreferenceId?.let { it3 ->
                    it.merchantAcquirerId?.let { it4 ->
                        subMerchantRepository.findByUniqueIdOrderOrderBySubMerchantAcquirerId(
                            it.aggregatorPreferenceId!!,
                            it2, it3, it4
                        )
                    }
                }
            }
        }!!
        if (subMerchantAcquirer.size> 0) {
            subMerchantAcquirer.forEach {
                var subMerchantList: SubMerchantList = SubMerchantList()
                subMerchantList.subMerchantAcquirerId = it.subMerchantAcquirerId
                subMerchantList.subMerchantAcquirerName = it.subMerchantAcquirerName
                subMerchantList.subMerchantAcquirerStatus = it.subMerchantAcquirerStatus
                subMerchantList.systemGenerated = it.systemGenerated
                subMerchantList.outlets = getoutletTreeList(it)
                subMerchantAcquirerList1234.add(subMerchantList)
            }
        }
        return subMerchantAcquirerList1234
    }

    fun getoutletTreeList(it: SubMerchantAcquirer): MutableList<OutletList> {
        var outletListParent: MutableList<OutletList> = ArrayList<OutletList>()
        var outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet = (it.aggregatorPreferenceId)?.let { it1 ->
            it.insitutionPreferenceId?.let { it2 ->
                it.merchantGroupPreferenceId?.let { it3 ->
                    it.merchantAcquirerPreferenceId?.let { it4 ->
                        it.subMerchantAcquirerId?.let { it5 ->
                            outletRepository.findByUniqueIdOrdeOrderByOutletId(
                                it.aggregatorPreferenceId!!,
                                it2, it3, it4, it5
                            )
                        }
                    }
                }
            }
        }!!
        if (outlet.size> 0) {
            outlet.forEach {
                var outletList: OutletList = OutletList()
                outletList.outletId = it.outletId
                outletList.outletName = it.outletName
                outletList.outletStatus = it.outletStatus
                var posList: ArrayList<PosList> = ArrayList<PosList>()
                var pos: List<Pos> = ArrayList<Pos>()
                pos = (it.aggregatorPreferenceId)?.let { it1 ->
                    it.insitutionPreferenceId?.let { it2 ->
                        it.merchantGroupPreferenceId?.let { it3 ->
                            it.merchantAcquirerPreferenceId?.let { it4 ->
                                it.subMerchantPreferenceId?.let { it5 ->

                                    it.outletId?.let { it6 ->
                                        posRepository.findPosByOutletPreferenceId(
                                            it6
                                        )
                                    }
                                }
                            }
                        }
                    }
                }!!
                if (pos.size> 0) {
                    pos.forEach() {
                        var posListObj: PosList = PosList()
                        posListObj.posId = it.posId
                        posListObj.posIPAddress = it.posIPAddress
                        posListObj.posFirmwareVersion = it.posFirmwareVersion
                        posListObj.posManufacturer = it.posManufacturer
                        posListObj.posModel = it.posModel
                        posListObj.posSerialNum = it.posSerialNum
                        posListObj.posKey = it.posKey
                        posList.add(posListObj)
                    }
                }
                outletList.posList = posList
                outletListParent.add(outletList)
            }
        }
        return outletListParent
    }

    fun updateInstitutionDetials(institution: Institution, aggregator: Aggregator): Institution {
        institution.insitutionName = aggregator.aggregatorName
        institution.insitutionStatus = aggregator.aggregatorStatus!!
        institution.address = createNewAddressFromParentDetails(aggregator.address)
        institution.adminDetails = createNewAdminDetailsFromParent(aggregator.adminDetails)
        institution.bankDetails = createNewBankDetailsFromParent(aggregator.bankDetails)
        institution.contactDetails = createNewContactFromParent(aggregator.contactDetails)
        institution.info = createNewInfoFromParent(aggregator.info)
        institution.aggregatorPreferenceId = aggregator.aggregatorPreferenceId
        institution.insitutionPreferenceId = aggregator.clientAggregatorPreferenceId
        institution.institutionLogo = aggregator.aggregatorLogo
        institution.others = createNewOthersFromParent(aggregator.others)
        return institution
    }

    fun updateMerchantGroupDetials(merchantGroup: MerchantGroup, institution: Institution): MerchantGroup {
        merchantGroup.merchantGroupName = institution.insitutionName
        merchantGroup.aggregatorPreferenceId = institution.aggregatorPreferenceId
        merchantGroup.insitutionPreferenceId = institution.institutionId
        merchantGroup.clientMerchantGroupId = institution.insitutionPreferenceId
        merchantGroup.address = createNewAddressFromParentDetails(institution.address)
        merchantGroup.adminDetails = createNewAdminDetailsFromParent(institution.adminDetails)
        merchantGroup.bankDetails = createNewBankDetailsFromParent(institution.bankDetails)
        merchantGroup.contactDetails = createNewContactFromParent(institution.contactDetails)
        merchantGroup.others = createNewOthersFromParent(institution.others)
        merchantGroup.merchantGroupStatus = institution.insitutionStatus
        merchantGroup.info = createNewInfoFromParent(institution.info)
        merchantGroup.merchantGroupLogo = institution.institutionLogo
        return merchantGroup
    }

    fun updateMerchantDetials(merchant: MerchantAcquirer, merchantGroup: MerchantGroup): MerchantAcquirer {
        merchant.merchantAcquirerName = merchantGroup.merchantGroupName
        merchant.clientMerchantAcquirerId = merchantGroup.clientMerchantGroupId
        merchant.merchantGroupPreferenceId = merchantGroup.merchantGroupPreferenceId
        merchant.insitutionPreferenceId = merchantGroup.insitutionPreferenceId
        merchant.aggregatorPreferenceId = merchantGroup.aggregatorPreferenceId
        merchant.address = createNewAddressFromParentDetails(merchantGroup.address)
        merchant.adminDetails = createNewAdminDetailsFromParent(merchantGroup.adminDetails)
        merchant.contactDetails = createNewContactFromParent(merchantGroup.contactDetails)
        merchant.info = createNewInfoFromParent(merchantGroup.info)
        merchant.merchantAcquirerStatus = merchantGroup.merchantGroupStatus
        merchant.bankDetails = createNewBankDetailsFromParent(merchantGroup.bankDetails)
        merchant.merchantAcquirerLogo = merchantGroup.merchantGroupLogo
        merchant.others = createNewOthersFromParent(merchantGroup.others)
        return merchant
    }

    fun updateSubMerchantDetails(subMerchant: SubMerchantAcquirer, merchant: MerchantAcquirer): SubMerchantAcquirer {
        subMerchant.subMerchantAcquirerName = merchant.merchantAcquirerName
        subMerchant.merchantAcquirerPreferenceId = merchant.merchantAcquirerId
        subMerchant.merchantGroupPreferenceId = merchant.merchantGroupPreferenceId
        subMerchant.insitutionPreferenceId = merchant.insitutionPreferenceId
        subMerchant.aggregatorPreferenceId = merchant.aggregatorPreferenceId
        subMerchant.subMerchantAcquirerStatus = merchant.merchantAcquirerStatus
        subMerchant.address = createNewAddressFromParentDetails(merchant.address)
        subMerchant.adminDetails = createNewAdminDetailsFromParent(merchant.adminDetails)
        subMerchant.bankDetails = createNewBankDetailsFromParent(merchant.bankDetails)
        subMerchant.contactDetails = createNewContactFromParent(merchant.contactDetails)
        subMerchant.info = createNewInfoFromParent(merchant.info)
        subMerchant.others = createNewOthersFromParent(merchant.others)
        subMerchant.isParentBlocked = merchant.isParentBlocked
        subMerchant.subMerchantAcquirerLogo = merchant.merchantAcquirerLogo
        return subMerchant
    }

    fun createNewAddressFromParentDetails(address: EntityAddress): EntityAddress {
        val entityAddress = EntityAddress()
        entityAddress.entityAddressAddressLine1 = address.entityAddressAddressLine1
        entityAddress.entityAddressAddressLine2 = address.entityAddressAddressLine2
        entityAddress.entityAddressAddressLine3 = address.entityAddressAddressLine3
        entityAddress.entityAddressCity = address.entityAddressCity
        entityAddress.entityAddressState = address.entityAddressState
        entityAddress.entityAddressCountry = address.entityAddressCountry
        entityAddress.entityAddressPostalCode = address.entityAddressPostalCode
        return entityAddress
    }
    fun createNewAdminDetailsFromParent(adminDetails: EntityAdminDetails): EntityAdminDetails {
        val entityAdminDetails = EntityAdminDetails()
        entityAdminDetails.entityAdminDetailsFirstName = adminDetails.entityAdminDetailsFirstName
        entityAdminDetails.entityAdminDetailsMiddleName = adminDetails.entityAdminDetailsMiddleName
        entityAdminDetails.entityAdminDetailsLastName = adminDetails.entityAdminDetailsLastName
        entityAdminDetails.entityAdminDetailsDepartment = adminDetails.entityAdminDetailsDepartment
        entityAdminDetails.entityAdminDetailsEmailId = adminDetails.entityAdminDetailsEmailId
        entityAdminDetails.entityAdminDetailsMobileNumber = adminDetails.entityAdminDetailsMobileNumber
        return entityAdminDetails
    }
    fun createNewContactFromParent(contactDetails: EntityContactDetails): EntityContactDetails {
        val entityContactDetails = EntityContactDetails()
        entityContactDetails.entityContactDetailsFirstName = contactDetails.entityContactDetailsFirstName
        entityContactDetails.entityContactDetailsMiddleName = contactDetails.entityContactDetailsMiddleName
        entityContactDetails.entityContactDetailsLastName = contactDetails.entityContactDetailsLastName
        entityContactDetails.entityContactDetailsDepartment = contactDetails.entityContactDetailsDepartment
        entityContactDetails.entityContactDetailsDesignation = contactDetails.entityContactDetailsDesignation
        entityContactDetails.entityContactDetailsEmailId = contactDetails.entityContactDetailsEmailId
        entityContactDetails.entityContactDetailsMobileNumber = contactDetails.entityContactDetailsMobileNumber
        return entityContactDetails
    }
    fun createNewBankDetailsFromParent(bankDetails: EntityBankDetails): EntityBankDetails {
        val entityBankDetails = EntityBankDetails()
        entityBankDetails.entityBankDetailsBankName = bankDetails.entityBankDetailsBankName
        entityBankDetails.entityBankDetailsBankAccountNumber = bankDetails.entityBankDetailsBankAccountNumber
        entityBankDetails.entityBankDetailsBranchCode = bankDetails.entityBankDetailsBranchCode
        entityBankDetails.entityBankDetailsBranchLocation = bankDetails.entityBankDetailsBranchLocation
        entityBankDetails.entityBankDetailsBankHolderName = bankDetails.entityBankDetailsBankHolderName
        return entityBankDetails
    }
    fun createNewInfoFromParent(info: EntityInfo): EntityInfo {
        val entityInfo = EntityInfo()
        entityInfo.entityInfoDescription = info.entityInfoDescription
        entityInfo.entityInfoLogo = info.entityInfoLogo
        entityInfo.entityInfoRegion = info.entityInfoRegion
        entityInfo.entityInfoType = info.entityInfoType
        entityInfo.entityInfoAbbrevation = info.entityInfoAbbrevation
        entityInfo.entityInfoBaseFiatCurrency = info.entityInfoBaseFiatCurrency
        entityInfo.entityInfoDefaultDigitalCurrency = info.entityInfoDefaultDigitalCurrency
        entityInfo.entityInfoTimezone = info.entityInfoTimezone
        return entityInfo
    }
    fun createNewOthersFromParent(others: EntityOthers): EntityOthers {
        val entityOthers = EntityOthers()
        entityOthers.entityOthersActivationDate = others.entityOthersActivationDate
        entityOthers.entityOthersCustomerOfflineTxn = others.entityOthersCustomerOfflineTxn
        entityOthers.entityOthersApprovalWorkFlow = others.entityOthersApprovalWorkFlow
        entityOthers.entityOthersMerchantOfflineTxn = others.entityOthersMerchantOfflineTxn
        return entityOthers
    }
}
