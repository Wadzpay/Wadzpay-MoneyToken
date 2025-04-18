package com.vacuumlabs.wadzpay.acquireradministration

import com.fasterxml.jackson.databind.ObjectMapper
import com.vacuumlabs.wadzpay.acquireradministration.dto.AggregatorDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.InstitutionDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.MerchantAcquirerDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.MerchantGroupDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.OutletDTO
import com.vacuumlabs.wadzpay.acquireradministration.dto.SubMerchantAcquirerDTO
import com.vacuumlabs.wadzpay.acquireradministration.model.AcquirerDataService
import com.vacuumlabs.wadzpay.acquireradministration.model.Aggregator
import com.vacuumlabs.wadzpay.acquireradministration.model.AggregatorLevels
import com.vacuumlabs.wadzpay.acquireradministration.model.AggregatorTree
import com.vacuumlabs.wadzpay.acquireradministration.model.DeleteAggregator
import com.vacuumlabs.wadzpay.acquireradministration.model.DeleteMerchantAcquirer
import com.vacuumlabs.wadzpay.acquireradministration.model.DeleteMerchantGroup
import com.vacuumlabs.wadzpay.acquireradministration.model.DeleteSubMerchantGroup
import com.vacuumlabs.wadzpay.acquireradministration.model.FullDataValidation
import com.vacuumlabs.wadzpay.acquireradministration.model.Institution
import com.vacuumlabs.wadzpay.acquireradministration.model.InstitutionDelete
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantAcquirer
import com.vacuumlabs.wadzpay.acquireradministration.model.MerchantGroup
import com.vacuumlabs.wadzpay.acquireradministration.model.Outlet
import com.vacuumlabs.wadzpay.acquireradministration.model.Pos
import com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantAcquirer
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.services.AcquirerCsvServcive
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@Validated
@RestController
@RequestMapping
@Tag(name = "Acquirer Administration Module Methods")
class AcquirerAggregatorController(
    val acquirerAdministrationService: AcquirerAdministrationService,
    val csvServcive: AcquirerCsvServcive,
    val dtoService: DTOEntityConverterService

) {
    @Bean
    fun filterMultipartResolver(): CommonsMultipartResolver {
        return CommonsMultipartResolver()
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/aggregatorListBackUp"
        ]
    )
    @Operation(summary = "Get list of Aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerAggregatorRetrieve(
        principal: Authentication,
    ): MutableList<Aggregator>? {
        return acquirerAdministrationService.getAggregatorList()
    }

    //    aggregator list
    @PostMapping(
        value = [
            "/merchant/acquirer/aggregatorList"
        ]
    )
    @Operation(summary = "Get list of Aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun aggregatorListWithPagination(
        @RequestBody request: AcquirerDataService.AggregatorDetailsRequest
    ): AcquirerDataService.AggregatorDataResponse {
        return acquirerAdministrationService.fetchAggregatorsToViewModels(request)
    }

    // institution list
    @PostMapping(
        value = [
            "/merchant/acquirer/institutionList"
        ]
    )
    @Operation(summary = "Get list of institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerInstitutionRetrieve(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.InstitutionDetailsRequest
    ): AcquirerDataService.InstitutionDataResponse {
        return acquirerAdministrationService.fetchInstitutionsToViewModels(request)
    }

    // merchant group list
    @PostMapping(
        value = [
            "/merchant/acquirer/getMerchantGroupList"
        ]
    )
    @Operation(summary = "Get list of Merchant Group")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getMerchantGroupList(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.MerchantGroupDetailsRequest
    ): AcquirerDataService.MerchantGroupDataResponse {
        return acquirerAdministrationService.fetchMerchantGroupToViewModels(request)
    }

    // merchant acquirer list
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantAcquirerList"
        ]
    )
    @Operation(summary = "Merchant Acquirer List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantAcquirerList(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.MerchantDetailsRequest
    ): AcquirerDataService.MerchantDataResponse {
        return acquirerAdministrationService.fetchMerchantsToViewModels(request)
    }

    // sub merchant  list
    @PostMapping(
        value = [
            "/merchant/acquirer/subMerchantList"
        ]
    )
    @Operation(summary = "Display submerchant List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun subMerchantAcquirerList(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.SubMerchantDetailsRequest
    ): AcquirerDataService.SubMerchantDataResponse {
        return acquirerAdministrationService.fetchSubMerchantsToViewModels(request)
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/createAggregator"
        ]
    )
    @Operation(summary = "create aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createAggregator(@Validated(FullDataValidation::class) @RequestBody aggregator: Aggregator) {
        if (aggregator.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }

        acquirerAdministrationService.createAggregator(aggregator)
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/saveAggregator"
        ]
    )
    @Operation(summary = "Save Aggregator as a draft")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveAggregator(
        @Valid
        @RequestBody aggregator: Aggregator
    ) {
        if (aggregator.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        acquirerAdministrationService.createAggregator(aggregator)
    }

    // institution list
    @GetMapping(
        value = [
            "/merchant/acquirer/institutionListBackUp"
        ]
    )
    @Operation(summary = "Get list of institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerInstitutionRetrieveBackUp(
        principal: Authentication,
    ): MutableList<Institution>? {
        return acquirerAdministrationService.getInstitutionList()
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/getAggregatorTree"
        ]
    )
    @Operation(summary = "Fetches the aggregator tree")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAggregatorTree(): MutableIterable<AggregatorTree> {
        return acquirerAdministrationService.getAggregatorTree()
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/createInstitution"
        ]
    )
    @Operation(summary = "create an Institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createInstitution(@Validated(FullDataValidation::class)@RequestBody institution: Institution) {
        acquirerAdministrationService.createInstitution(institution)
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/saveInstitution"
        ]
    )
    @Operation(summary = "Save Institution as draft")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveInstitution(@Valid @RequestBody institution: Institution) {
        acquirerAdministrationService.createInstitution(institution)
    }
    // aggregatorUpdate
    @PostMapping(
        value = [
            "/merchant/acquirer/aggregatorUpdate"
        ]
    )
    @Operation(summary = "Update  Aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerAggregatorUpdate(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody aggregatorRequest: Aggregator
    ): String {
        println("acquirerAggregatorUpdate")
        if (aggregatorRequest.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultAggregatorStr: String = acquirerAdministrationService.acquirerAggregatorUpdateV2(aggregatorRequest)
        val responseAggregatorJson = JSONObject()
        responseAggregatorJson.put("result", resultAggregatorStr)
        return responseAggregatorJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/aggregatorDraftUpdate"
        ]
    )
    @Operation(summary = "Update a Draft state  Aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerAggregatorDraftUpdate(
        principal: Authentication,
        @Valid @RequestBody aggregatorRequest: Aggregator
    ): String {
        println("acquirerAggregatorUpdateDraft")
        if (aggregatorRequest.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultAggregatorStr: String = acquirerAdministrationService.acquirerAggregatorUpdateV2(aggregatorRequest)
        val responseAggregatorJson = JSONObject()
        responseAggregatorJson.put("result", resultAggregatorStr)
        return responseAggregatorJson.toString()
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/aggregator/{aggregatorId}"
        ]
    )
    @Operation(summary = " Get an Aggregator By Id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerAggregatorById(
        @PathVariable aggregatorId: String,
        principal: Authentication
    ): Aggregator {
        if (aggregatorId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultAggregator: Aggregator = acquirerAdministrationService.findByAcquirerAggregatorById(aggregatorId)

        return resultAggregator
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/institution/{institutionId}"
        ]
    )
    @Operation(summary = "Institute by id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerInstituionById(
        principal: Authentication,
        @PathVariable institutionId: String
    ): Institution {
        if (institutionId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        return acquirerAdministrationService.findInstitutionById(institutionId)
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/merchantGroup/{merchantGroupId}"
        ]
    )
    @Operation(summary = "MerchantGroup by Id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantGroupById(
        principal: Authentication,
        @PathVariable merchantGroupId: String
    ): MerchantGroup? {
        if (merchantGroupId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_MERCHANT_GROUP_ID)
        }
        return acquirerAdministrationService.findMerchantGroupById(merchantGroupId)
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/merchant/{merchantId}"
        ]
    )
    @Operation(summary = "Merchant by Id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantById(
        principal: Authentication,
        @PathVariable merchantId: String
    ): MerchantAcquirer? {
        if (merchantId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_MERCHANT_ID)
        }
        return acquirerAdministrationService.findMerchantById(merchantId)
    }
    @GetMapping(
        value = [
            "/merchant/acquirer/submerchant/{subMerchantId}"
        ]
    )
    @Operation(summary = "SubMerchant by Id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerSubMerchantById(
        principal: Authentication,
        @PathVariable subMerchantId: String
    ): SubMerchantAcquirer? {
        if (subMerchantId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_MERCHANT_ID)
        }
        return acquirerAdministrationService.findSubMerchantById(subMerchantId)
    }

    // institution Update
    @PostMapping(
        value = [
            "/merchant/acquirer/institutionUpdate"
        ]
    )
    @Operation(summary = "Update an  Institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerInstitutionUpdate(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody institutionRequest: Institution
    ): String {
        if (institutionRequest.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultStr: String = acquirerAdministrationService.acquirerInstitutionUpdateV2(institutionRequest)
        val responseJson = JSONObject()
        responseJson.put("result", resultStr)
        return responseJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/institutionDraft"
        ]
    )
    @Operation(summary = "Update a Draft state Institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerInstitutionDraftUpdate(
        principal: Authentication,
        @Valid @RequestBody institutionRequest: Institution

    ): String {
        if (institutionRequest.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultInstitutionStr: String = acquirerAdministrationService.acquirerInstitutionUpdateV2(institutionRequest)
        val responseInstitutionJson = JSONObject()
        responseInstitutionJson.put("result", resultInstitutionStr)
        return responseInstitutionJson.toString()
    }

    // Merchant Group Update
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantGroupUpdate"
        ]
    )
    @Operation(summary = "Update Merchant Group")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantGroupUpdate(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody merchantGroupDetailsRequest: AcquirerDataService.MerchantGroupDetailsSaveRequest
    ): Any {
        if (merchantGroupDetailsRequest.merchantGroup.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultMerchantGroupStr =
            acquirerAdministrationService.acquirerMerchantGroupUpdateV2(merchantGroupDetailsRequest.merchantGroup)
        if (resultMerchantGroupStr is String) {
            val responseMerchantGroupJson = JSONObject()
            responseMerchantGroupJson.put("result", resultMerchantGroupStr)
            return responseMerchantGroupJson.toString()
        } else {
            return resultMerchantGroupStr
        }
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantGroupUpdateDraft"
        ]
    )
    @Operation(summary = "Update Draft state Merchant Group")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantGroupUpdateDraft(
        principal: Authentication,
        @Valid @RequestBody merchantGroupDetailsRequest: AcquirerDataService.MerchantGroupDetailsSaveRequest
    ): Any {
        if (merchantGroupDetailsRequest.merchantGroup.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultMerchantGroupStr =
            acquirerAdministrationService.acquirerMerchantGroupUpdateV2(merchantGroupDetailsRequest.merchantGroup)
        if (resultMerchantGroupStr is String) {
            val responseMerchantGroupJson = JSONObject()
            responseMerchantGroupJson.put("result", resultMerchantGroupStr)
            return responseMerchantGroupJson.toString()
        } else {
            return resultMerchantGroupStr
        }
    }

    // Merchant Acquirer Update
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantAcquirerUpdate"
        ]
    )
    @Operation(summary = "Merchant Acquirer Update")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantAcquirerUpdate(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody merchantDetailsSaveRequest: AcquirerDataService.MerchantDetailsSaveRequest
    ): Any {
        if (merchantDetailsSaveRequest.merchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultMerchantAcquirerStr =
            acquirerAdministrationService.acquirerMerchantAcquirerUpdate(merchantDetailsSaveRequest.merchant)
        if (resultMerchantAcquirerStr is String) {
            val responseMerchantAcquirerJson = JSONObject()
            responseMerchantAcquirerJson.put("result", resultMerchantAcquirerStr)
            return responseMerchantAcquirerJson.toString()
        } else return resultMerchantAcquirerStr
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantAcquirerUpdateDraft"
        ]
    )
    @Operation(summary = "Merchant Acquirer Update")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantAcquirerUpdateDraft(
        principal: Authentication,
        @Valid @RequestBody merchantDetailsSaveRequest: AcquirerDataService.MerchantDetailsSaveRequest
    ): Any {
        if (merchantDetailsSaveRequest.merchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultStr =
            acquirerAdministrationService.acquirerMerchantAcquirerUpdate(merchantDetailsSaveRequest.merchant)
        if (resultStr is String) {
            val responseJson = JSONObject()
            responseJson.put("result", resultStr)
            return responseJson.toString()
        } else return resultStr
    }

    // aggregatorDelete
    @PostMapping(
        value = [
            "/merchant/acquirer/aggregatorDelete"
        ]
    )
    @Operation(summary = "Delete the particular  Aggregator ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerAggregatorDelete(
        principal: Authentication,
        @RequestBody deleteAggregator: DeleteAggregator
    ): String {
        if (deleteAggregator.aggregatorId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultAggregatorStr: String = acquirerAdministrationService.acquirerAggregatorDelete(deleteAggregator.aggregatorId!!)
        val responseAggregatorJson = JSONObject()
        responseAggregatorJson.put("result", resultAggregatorStr)
        return responseAggregatorJson.toString()
    }

    // Merchant Group Delete
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantGroupDelete"
        ]
    )
    @Operation(summary = "Deletes  Merchnat group ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantGroupDelete(
        principal: Authentication,
        @RequestBody merchantGroupDelete: DeleteMerchantGroup
    ): String {
        if (merchantGroupDelete.merchantGroupId == null) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val resultMerchantGroupStr: String =
            acquirerAdministrationService.acquirerMerchantGroupDelete(merchantGroupDelete.merchantGroupId!!)
        val responseMerchantGroupJson = JSONObject()
        responseMerchantGroupJson.put("result", resultMerchantGroupStr)
        return resultMerchantGroupStr.toString()
    }

    // Merchant Acquirer Delete
    @PostMapping(
        value = [
            "/merchant/acquirer/merchantAcquirerDelete"
        ]
    )
    @Operation(summary = "Delete Merchnat Acquirer")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantAcquirerDelete(
        principal: Authentication,
        @RequestBody deleteMerchantAcquirer: DeleteMerchantAcquirer
    ): String {
        if (deleteMerchantAcquirer.merchantAcquirerId == null) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val resultMerchantAcquirerStr: String =
            acquirerAdministrationService.acquirerMerchantAcquirerDelete(deleteMerchantAcquirer.merchantAcquirerId!!)
        val responseMerchantAcquirerJson = JSONObject()
        responseMerchantAcquirerJson.put("result", resultMerchantAcquirerStr)
        return responseMerchantAcquirerJson.toString()
    }

    // Institution Delete
    @PostMapping(
        value = [
            "/merchant/acquirer/institutionDelete"
        ]
    )
    @Operation(summary = "Delete an Institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerInstitutionDelete(
        principal: Authentication,
        @RequestBody institutionDelete: InstitutionDelete
    ): String {
        if (institutionDelete.institutionId == null) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val resultStr: String =
            acquirerAdministrationService.acquirerInstitutionDelete(institutionDelete.institutionId!!)
        val responseJson = JSONObject()
        responseJson.put("result", resultStr)
        return responseJson.toString()
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/getInstitutionByAggregatorId"
        ]
    )
    @Operation(summary = "Get list of institutions by aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getInstitutionByAggregatorPrefId(
        principal: Authentication,
        aggregatorId: String
    ): MutableList<Institution>? {
        return acquirerAdministrationService.getInstitutionByAggregatorPrefId(aggregatorId)
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/getMerchantGrpByInstitutionId"
        ]
    )
    @Operation(summary = "Get list of Merchant groups by instittution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getMerchantGrpByInstitutionId(
        principal: Authentication,
        institutionId: String
    ): MutableList<MerchantGroup>? {
        return acquirerAdministrationService.getMerchantGrpByInstitutionPrefId(institutionId)
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/getMerchantByMerchantGroupId"
        ]
    )
    @Operation(summary = "Get list of merchants by merchnat group")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getMerchantByMerchantGroupPrefId(
        principal: Authentication,
        merchantGroupId: String
    ): MutableList<MerchantAcquirer>? {
        return acquirerAdministrationService.getMerchantByMerchantGroupPrefId(merchantGroupId)
    }

    @GetMapping(
        value = [
            "/merchant/acquirer/getAggregatorByAggregatorId"
        ]
    )
    @Operation(summary = "Get aggregator")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAggregatorByAggregatorId(
        principal: Authentication,
        aggregatorId: String
    ): Aggregator {
        return acquirerAdministrationService.getAggregatorByAggregatorId(aggregatorId)
    }

    // merchant group list
    @GetMapping(
        value = [
            "/merchant/acquirer/getMerchantGroupListBackUp"
        ]
    )
    @Operation(summary = "Get list of Merchant Group")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getMerchantGroupListBackUp(
        principal: Authentication,
    ): MutableList<MerchantGroup>? {
        return acquirerAdministrationService.getMerchantGroupList()
    }

    // Merchant Group save
    @PostMapping(
        value = [
            "/merchant/acquirer/createMerchantGroup"
        ]
    )
    @Operation(summary = "create merchant group")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createMerchantGroup(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody merchantGroupRequest: AcquirerDataService.MerchantGroupDetailsSaveRequest
    ): String {
        if (merchantGroupRequest.merchantGroup.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultMerchantGroupStr: String = acquirerAdministrationService.createMerchantGroup(merchantGroupRequest)
        val responseMerchantGroupJson = JSONObject()
        responseMerchantGroupJson.put("result", resultMerchantGroupStr)
        return responseMerchantGroupJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/saveMerchantGroup"
        ]
    )
    @Operation(summary = "Save Merchant group as Draft")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveMerchantGroup(
        principal: Authentication,
        @Valid @RequestBody merchantGroupRequest: AcquirerDataService.MerchantGroupDetailsSaveRequest
    ): String {
        if (merchantGroupRequest.merchantGroup.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }

        val resultMerchantGroupStr: String = acquirerAdministrationService.createMerchantGroup(merchantGroupRequest)
        val responseMerchantGroupJson = JSONObject()
        responseMerchantGroupJson.put("result", resultMerchantGroupStr)
        return responseMerchantGroupJson.toString()
    }

    // merchant acquirer list
    @GetMapping(
        value = [
            "/merchant/acquirer/merchantAcquirerListBackUp"
        ]
    )
    @Operation(summary = "Merchant Acquirer List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acquirerMerchantAcquirerListBackUp(
        principal: Authentication,
    ): MutableList<MerchantAcquirer>? {
        return acquirerAdministrationService.getMerchantAcquirerList()
    }

    // Merchant Acquirer Save
    @PostMapping(
        value = [
            "/merchant/acquirer/createMerchantAcquirer"
        ]
    )
    @Operation(summary = "merchant acquirer save")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createMerchantAcquirer(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody merchantAcquirerRequest: AcquirerDataService.MerchantDetailsSaveRequest
    ): String {
        if (merchantAcquirerRequest.merchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultMerchantAcquirerStr: String = acquirerAdministrationService.acquirerMerchantAcquirerSaveV2(merchantAcquirerRequest)
        val responseMerchantAcquirerJson = JSONObject()
        responseMerchantAcquirerJson.put("result", resultMerchantAcquirerStr)
        return responseMerchantAcquirerJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/saveMerchantAcquirer"
        ]
    )
    @Operation(summary = "merchant acquirer save as Draft ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveMerchantAcquirer(
        principal: Authentication,
        @Valid @RequestBody merchantAcquirerRequest: AcquirerDataService.MerchantDetailsSaveRequest
    ): String {
        if (merchantAcquirerRequest.merchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultMerchantAcquirerStr: String = acquirerAdministrationService.acquirerMerchantAcquirerSaveV2(merchantAcquirerRequest)
        val responseMerchantAcquirerJson = JSONObject()
        responseMerchantAcquirerJson.put("result", resultMerchantAcquirerStr)
        return responseMerchantAcquirerJson.toString()
    }

    // Merchant Acquirer Delete
    @PostMapping(
        value = [
            "/merchant/acquirer/subMerchantDelete"
        ]
    )
    @Operation(summary = "Delete submerchant")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun subMerchantDelete(
        principal: Authentication,
        @RequestBody deleteSubMerchantGroup: DeleteSubMerchantGroup
    ): String {
        if (deleteSubMerchantGroup.subMerchantGroupId == null) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val resultSubMerchantAcquirerStr: String =
            acquirerAdministrationService.subMerchantAcquirerDelete(deleteSubMerchantGroup.subMerchantGroupId!!)
        val responseSubMerchantAcquirerJson = JSONObject()
        responseSubMerchantAcquirerJson.put("result", resultSubMerchantAcquirerStr)
        return responseSubMerchantAcquirerJson.toString()
    }

    // Merchant Group Update
    @PostMapping(
        value = [
            "/merchant/acquirer/subMerchantUpdate"
        ]
    )
    @Operation(summary = "Update SubMerchant")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun subMerchantUpdate(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody subMerchantDetailsSaveRequest: AcquirerDataService.SubMerchantDetailsSaveRequest
    ): String {
        if (subMerchantDetailsSaveRequest.subMerchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultSubMerchantAcquirerStr: String =
            acquirerAdministrationService.updateSubMerchant(subMerchantDetailsSaveRequest.subMerchant)
        val responseSubMerchantAcquirerJson = JSONObject()
        responseSubMerchantAcquirerJson.put("result", resultSubMerchantAcquirerStr)
        return responseSubMerchantAcquirerJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/subMerchantUpdateDraft"
        ]
    )
    @Operation(summary = "Update draft state submerchant ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun subMerchantUpdateDraft(
        principal: Authentication,
        @Valid @RequestBody subMerchantDetailsSaveRequest: AcquirerDataService.SubMerchantDetailsSaveRequest
    ): String {
        if (subMerchantDetailsSaveRequest.subMerchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultSubMerchantAcquirerStr: String =
            acquirerAdministrationService.updateSubMerchant(subMerchantDetailsSaveRequest.subMerchant)
        val responseSubMerchantAcquirerJson = JSONObject()
        responseSubMerchantAcquirerJson.put("result", resultSubMerchantAcquirerStr)
        return responseSubMerchantAcquirerJson.toString()
    }
    // Sub Merchant Acquirer Save
    @PostMapping(
        value = [
            "/merchant/acquirer/createSubMerchantAcquirer"
        ]
    )
    @Operation(summary = "Save submerchant acquirer")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createSubMerchantAcquirer(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody subMerchantDetailsSaveRequest: AcquirerDataService.SubMerchantDetailsSaveRequest
    ): String {
        if (subMerchantDetailsSaveRequest.subMerchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        acquirerAdministrationService.createSubMerchant(subMerchantDetailsSaveRequest)
        return "sub merchant created successfully"
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/saveSubMerchantAcquirer"
        ]
    )
    @Operation(summary = "Save Submerchant as draft")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveSubMerchantAcquirer(
        principal: Authentication,
        @Valid @RequestBody subMerchantDetailsSaveRequest: AcquirerDataService.SubMerchantDetailsSaveRequest
    ): String {
        if (subMerchantDetailsSaveRequest.subMerchant.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }

        acquirerAdministrationService.createSubMerchant(subMerchantDetailsSaveRequest)
        return "sub merchant saved successfully"
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/createOutlet"
        ]
    )
    @Operation(summary = "Save Outlet")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createOutlet(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody outletDetails: AcquirerDataService.OutletDetailsSaveRequest
    ): String {
        if (outletDetails.outlet.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultOutetString = acquirerAdministrationService.createOutlet(outletDetails)
        val responseOutletJson = JSONObject()
        responseOutletJson.put("result", resultOutetString)
        return responseOutletJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/saveOutlet"
        ]
    )
    @Operation(summary = "Save Outlet as draft ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun saveOutlet(
        principal: Authentication,
        @Valid @RequestBody outletDetails: AcquirerDataService.OutletDetailsSaveRequest
    ): String {
        if (outletDetails.outlet.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }

        val resultOutetString = acquirerAdministrationService.createOutlet(outletDetails)
        val responseOutletJson = JSONObject()
        responseOutletJson.put("result", resultOutetString)
        return responseOutletJson.toString()
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/updateOutletDraft"
        ]
    )
    @Operation(summary = "Update drfat state Outlet")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updateOutletDraft(
        principal: Authentication,
        @Valid @RequestBody outletReq: AcquirerDataService.OutletDetailsSaveRequest
    ): String {
        if (outletReq.outlet.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }

        val resultOutetString: String? =
            acquirerAdministrationService.updateOutlet(outletReq.outlet)
        val responseOutletJson = JSONObject()
        responseOutletJson.put("result", resultOutetString)
        return responseOutletJson.toString()
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/updateOutlet"
        ]
    )
    @Operation(summary = "Update Outlet")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updateOutlet(
        principal: Authentication,
        @Validated(FullDataValidation::class) @RequestBody outletReq: AcquirerDataService.OutletDetailsSaveRequest
    ): String {
        if (outletReq.outlet.aggregatorPreferenceId.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }

        val resultOutetString: String? =
            acquirerAdministrationService.updateOutlet(outletReq.outlet)
        val responseOutletJson = JSONObject()
        responseOutletJson.put("result", resultOutetString)
        return responseOutletJson.toString()
    }
    @GetMapping(
        value = [
            "/merchant/acquirer/outlet/{outletId}"
        ]
    )
    @Operation(summary = "Display outlet by id")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun outletById(
        principal: Authentication,
        @PathVariable outletId: String
    ): Outlet? {
        return acquirerAdministrationService.fetchOutletById(outletId)
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/outletList"
        ]
    )
    @Operation(summary = "Display outlet List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun outletList(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.OutletDetailsRequest
    ): AcquirerDataService.OutletResponse? {
        return acquirerAdministrationService.getOutletList(request)
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/outletDelete"
        ]
    )
    @Operation(summary = "Delete from outlet List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deleteOutlet(
        principal: Authentication,
        @RequestBody outlet: Outlet
    ): String {
        if (outlet.aggregatorPreferenceId == null) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val resultOutletString: String =
            acquirerAdministrationService.outletDelete(outlet)
        val responseOutletJson = JSONObject()
        responseOutletJson.put("result", resultOutletString)
        return responseOutletJson.toString()
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/createPos"
        ]
    )
    @Operation(summary = "Save Pos")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createPos(
        principal: Authentication,
        @RequestBody pos: Pos
    ): String {
        if (pos.outletPreferenceId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultPosString = acquirerAdministrationService.createPos(pos)
        val responsePosJson = JSONObject()
        responsePosJson.put("result", resultPosString)
        return responsePosJson.toString()
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/updatePos"
        ]
    )
    @Operation(summary = "update pos List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updatePos(
        principal: Authentication,
        @RequestBody pos: Pos
    ): String {
        if (pos.outletPreferenceId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_AGGREGATOR_PREFERENCE_ID)
        }
        val resultPosString: String = acquirerAdministrationService.updatePos(pos)
        val responsePosJson = JSONObject()
        responsePosJson.put("result", resultPosString)
        return responsePosJson.toString()
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/getPosListByOutlet"
        ]
    )
    @Operation(summary = "Get list of Pos")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun fetchPosList(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.PosDetailsRequest
    ): MutableList<Pos>? {
        return acquirerAdministrationService.getPosListByOutletId(request)
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/posList"
        ]
    )
    @Operation(summary = "Get list of Pos")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of Pos",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun fetchPosListing(
        principal: Authentication,
        @RequestBody request: AcquirerDataService.PosDetailsRequest
    ): AcquirerDataService.PosResponse? {
        println("request received $request")
        return acquirerAdministrationService.getPosList(request)
    }
    @PostMapping(
        value = [
            "/merchant/acquirer/deletePos"
        ]
    )
    @Operation(summary = "Delete from List of pos")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deletePos(
        principal: Authentication,
        @RequestBody posId: String
    ): String {
        if (posId.isEmpty()) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val posIdString: String = ObjectMapper().readValue(posId, String::class.java)
        val resultPosString: String =
            acquirerAdministrationService.posDelete(posIdString)
        val responsePosJson = JSONObject()
        responsePosJson.put("result", resultPosString)
        return responsePosJson.toString()
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/upload-aggregator-csv"
        ]
    )
    @Operation(summary = "save from List of aggregators")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadAggregator(
        principal: Authentication,
        @RequestBody file: MultipartFile,
        response: HttpServletResponse
    ): ResponseEntity<Array<Byte>>? {
        if (file.isEmpty) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val fileReader = BufferedReader(InputStreamReader(file.inputStream, "UTF-8"))
        val aggregatorLevels: AggregatorLevels = csvServcive.readCsvAggregator2(fileReader)

        /*
        val listAggregator: List<Aggregator> = csvServcive.readCsvAggregator(fileReader)
*/
        val aggregatorListCli: MutableList<Aggregator> = mutableListOf()
        val institutionListCli: MutableList<Institution> = mutableListOf()
        val merchantGroupListCli: MutableList<MerchantGroup> = mutableListOf()
        val merchantListCli: MutableList<MerchantAcquirer> = mutableListOf()
        val subMerchantListCli: MutableList<SubMerchantAcquirer> = mutableListOf()
        val outletListCli: MutableList<Outlet> = mutableListOf()

        aggregatorLevels.aggregator.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                aggregatorListCli.add(it)
            }
        }
        aggregatorLevels.institution.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                institutionListCli.add(it)
            }
        }
        aggregatorLevels.merchantGroup.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                merchantGroupListCli.add(it)
            }
        }
        aggregatorLevels.merchantAcquirer.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                merchantListCli.add(it)
            }
        }
        aggregatorLevels.subMerchantAcquirer.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                subMerchantListCli.add(it)
            }
        }
        aggregatorLevels.outlet.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                outletListCli.add(it)
            }
        }
        if (aggregatorLevels.aggregator.isNotEmpty()) {
            val errorList: List<Aggregator> =
                acquirerAdministrationService.saveAggegatorList(aggregatorLevels.aggregator)
            var errorDTOList: MutableList<AggregatorDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    aggregatorListCli.filter { it1 -> it1.aggregatorPreferenceId == it.aggregatorPreferenceId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
        }
        if (aggregatorLevels.institution.isNotEmpty()) {
            val errorList: List<Institution> =
                acquirerAdministrationService.saveInstitutionList(aggregatorLevels.institution)
            var errorDTOList: MutableList<AggregatorDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    institutionListCli.filter { it1 -> it1.institutionId == it.institutionId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
        }
        if (aggregatorLevels.merchantGroup.isNotEmpty()) {
            val errorList: List<MerchantGroup> =
                acquirerAdministrationService.saveMerchantGroupList(aggregatorLevels.merchantGroup)
            var errorDTOList: MutableList<AggregatorDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    merchantGroupListCli.filter { it1 -> it1.merchantGroupPreferenceId == it.merchantGroupPreferenceId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
        }
        if (aggregatorLevels.merchantAcquirer.isNotEmpty()) {
            val errorList: List<MerchantAcquirer> =
                acquirerAdministrationService.saveMerchantList(aggregatorLevels.merchantAcquirer)
            var errorDTOList: MutableList<AggregatorDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    merchantListCli.filter { it1 -> it1.merchantAcquirerId == it.merchantAcquirerId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
        }
        if (aggregatorLevels.subMerchantAcquirer.isNotEmpty()) {
            val errorList: List<SubMerchantAcquirer> =
                acquirerAdministrationService.saveSubMerchantList(aggregatorLevels.subMerchantAcquirer)
            var errorDTOList: MutableList<AggregatorDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    subMerchantListCli.filter { it1 -> it1.subMerchantAcquirerId == it.subMerchantAcquirerId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
        }
        if (aggregatorLevels.outlet.isNotEmpty()) {
            val errorList: List<Outlet> =
                acquirerAdministrationService.saveOutletList(aggregatorLevels.outlet)
            var errorDTOList: MutableList<AggregatorDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    outletListCli.filter { it1 -> it1.outletId == it.outletId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
        }

        if (aggregatorListCli.size == 0 && institutionListCli.size == 0 && merchantGroupListCli.size == 0 && merchantListCli.size == 0 && subMerchantListCli.size == 0 && outletListCli.size == 0) {

            return ResponseEntity(HttpStatus.OK)
        }
        response.setContentType("text/csv")
        response.setHeader("Content-Disposition", "attachment; filename=data.csv")
        try {
            var errorDTOListAggregator: MutableList<AggregatorDTO> = mutableListOf()
            var errorDTOListInstitute: List<AggregatorDTO>
            var errorDTOListMerchantGroup: List<AggregatorDTO>
            var errorDTOListMerchant: List<AggregatorDTO>
            var errorDTOListSubMerchant: List<AggregatorDTO>
            var errorDTOListOutlet: List<AggregatorDTO>

            if (aggregatorListCli.isNotEmpty()) {
                errorDTOListAggregator =
                    aggregatorListCli.map { it -> dtoService.toAggregatorDTO(it) }.toMutableList()
            }
            if (institutionListCli.isNotEmpty()) {
                errorDTOListInstitute =
                    institutionListCli.map { it -> dtoService.toAggregatorDTOI(it) }
                errorDTOListAggregator.addAll(errorDTOListInstitute)
            }
            if (merchantGroupListCli.isNotEmpty()) {
                errorDTOListMerchantGroup =
                    merchantGroupListCli.map { it -> dtoService.toAggregatorDTOMg(it) }
                errorDTOListAggregator.addAll(errorDTOListMerchantGroup)
            }
            if (merchantListCli.isNotEmpty()) {
                errorDTOListMerchant =
                    merchantListCli.map { it -> dtoService.toAggregatorDTOMerchant(it) }
                errorDTOListAggregator.addAll(errorDTOListMerchant)
            }
            if (subMerchantListCli.isNotEmpty()) {
                errorDTOListSubMerchant =
                    subMerchantListCli.map { it -> dtoService.toAggregatorDTOSubM(it) }
                errorDTOListAggregator.addAll(errorDTOListSubMerchant)
            }
            if (outletListCli.isNotEmpty()) {
                errorDTOListOutlet =
                    outletListCli.map { it -> dtoService.toAggregatorDTOOutlet(it) }
                errorDTOListAggregator.addAll(errorDTOListOutlet)
            }

            if (aggregatorLevels.aggregator.size.plus(aggregatorLevels.institution.size) == aggregatorListCli.size.plus(institutionListCli.size)) {
                csvServcive.writeAggregatorToCSV(errorDTOListAggregator, response)
                return ResponseEntity(HttpStatus.BAD_REQUEST)
            } else {
                csvServcive.writeAggregatorToCSV(errorDTOListAggregator, response)
                return ResponseEntity(HttpStatus.MULTI_STATUS)
            }
        } catch (e: IOException) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        // }
        /*else {
        print("returning null")
        return null
    }*/
        return null
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/upload-institution-csv"
        ]
    )
    @Operation(summary = "save from List of institutions")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadInstitution(
        principal: Authentication,
        @RequestBody file: MultipartFile,
        response: HttpServletResponse
    ): ResponseEntity<Array<Byte>>? {
        if (file.isEmpty) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val fileReader = BufferedReader(InputStreamReader(file.inputStream, "UTF-8"))

        val listInstitution: List<Institution> = csvServcive.readCsvInstitute(fileReader)
        val errorListCli: MutableList<Institution> = mutableListOf()
        listInstitution.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                errorListCli.add(it)
            }
        }
        if (listInstitution.isNotEmpty()) {
            val errorList: List<Institution> =
                acquirerAdministrationService.saveInstitutionList(listInstitution)
            var errorDTOList: MutableList<InstitutionDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    errorListCli.filter { it1 -> it1.institutionId == it.institutionId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
            if (errorListCli.isEmpty()) {

                return ResponseEntity(HttpStatus.OK)
            }
            response.setContentType("text/csv")
            response.setHeader("Content-Disposition", "attachment; filename=data.csv")
            try {
                if (errorListCli.isNotEmpty()) {
                    val errorDTOListOut: List<InstitutionDTO> =
                        errorListCli.map { it -> dtoService.toInstitutionDTO(it) }
                    if (listInstitution.size == errorListCli.size) {
                        csvServcive.writeInstitutiontoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.BAD_REQUEST)
                    } else {
                        csvServcive.writeInstitutiontoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.MULTI_STATUS)
                    }
                }
            } catch (e: IOException) {
                return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else {
            print("returning null")
            return null
        }
        return null
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/upload-merchantGroup-csv"
        ]
    )
    @Operation(summary = "save from List of merchnatgroups")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadMerchantGroup(
        principal: Authentication,
        @RequestBody file: MultipartFile,
        response: HttpServletResponse
    ): ResponseEntity<Array<Byte>>? {
        if (file.isEmpty) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val fileReader = BufferedReader(InputStreamReader(file.inputStream, "UTF-8"))

        val listMerchantGroup: List<MerchantGroup> = csvServcive.readCsvMerchantGroup(fileReader)
        val errorListCli: MutableList<MerchantGroup> = mutableListOf()
        listMerchantGroup.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                errorListCli.add(it)
            }
        }
        if (listMerchantGroup.isNotEmpty()) {
            val errorList: List<MerchantGroup> =
                acquirerAdministrationService.saveMerchantGroupList(listMerchantGroup)
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    errorListCli.filter { it1 -> it1.merchantGroupPreferenceId == it.merchantGroupPreferenceId }
                        .map { it1 ->
                            {
                                if (it1.error.isNullOrEmpty()) {
                                    it1.error = it.error
                                }
                            }
                        }
                }
            }
            if (errorListCli.isEmpty()) {

                return ResponseEntity(HttpStatus.OK)
            }
            response.setContentType("text/csv")
            response.setHeader("Content-Disposition", "attachment; filename=data.csv")
            try {
                if (errorListCli.isNotEmpty()) {
                    val errorDTOListOut: List<MerchantGroupDTO> =
                        errorListCli.map { it -> dtoService.toMerchantGroupDTO(it) }
                    if (listMerchantGroup.size == errorListCli.size) {
                        csvServcive.writeMerchanGrouptoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.BAD_REQUEST)
                    } else {
                        csvServcive.writeMerchanGrouptoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.MULTI_STATUS)
                    }
                }
            } catch (e: IOException) {
                return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else {
            print("returning null")
            return null
        }
        return null
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/upload-merchant-csv"
        ]
    )
    @Operation(summary = "save from List of merchants")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadMerchant(
        principal: Authentication,
        @RequestBody file: MultipartFile,
        response: HttpServletResponse
    ): ResponseEntity<Array<Byte>>? {
        if (file.isEmpty) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val fileReader = BufferedReader(InputStreamReader(file.inputStream, "UTF-8"))

        val listMerchantAcquirer: List<MerchantAcquirer> = csvServcive.readCsvMerchant(fileReader)
        val errorListCli: MutableList<MerchantAcquirer> = mutableListOf()
        listMerchantAcquirer.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                errorListCli.add(it)
            }
        }
        if (listMerchantAcquirer.isNotEmpty()) {
            val errorList: List<MerchantAcquirer> =
                acquirerAdministrationService.saveMerchantList(listMerchantAcquirer)
            var errorDTOList: MutableList<MerchantAcquirerDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    errorListCli.filter { it1 -> it1.merchantAcquirerId == it.merchantAcquirerId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
            if (errorListCli.isEmpty()) {
                return ResponseEntity(HttpStatus.OK)
            }
            response.setContentType("text/csv")
            response.setHeader("Content-Disposition", "attachment; filename=data.csv")
            try {
                if (errorListCli.isNotEmpty()) {
                    val errorDTOListOut: List<MerchantAcquirerDTO> =
                        errorListCli.map { it -> dtoService.toMerchantDTO(it) }
                    if (listMerchantAcquirer.size == errorListCli.size) {
                        csvServcive.writeMerchantoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.BAD_REQUEST)
                    } else {
                        csvServcive.writeMerchantoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.MULTI_STATUS)
                    }
                }
            } catch (e: IOException) {
                return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else {
            print("returning null")
            return null
        }
        return null
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/upload-submerchant-csv"
        ]
    )
    @Operation(summary = "save from List of submerchants")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadSubMerchant(
        principal: Authentication,
        @RequestBody file: MultipartFile,
        response: HttpServletResponse
    ): ResponseEntity<Array<Byte>>? {
        if (file.isEmpty) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val fileReader = BufferedReader(InputStreamReader(file.inputStream, "UTF-8"))

        val listSubMerchantAcquirer: List<SubMerchantAcquirer> = csvServcive.readCsvSubMerchant(fileReader)
        val errorListCli: MutableList<SubMerchantAcquirer> = mutableListOf()
        listSubMerchantAcquirer.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                errorListCli.add(it)
            }
        }
        if (listSubMerchantAcquirer.isNotEmpty()) {
            val errorList: List<SubMerchantAcquirer> =
                acquirerAdministrationService.saveSubMerchantList(listSubMerchantAcquirer)
            var errorDTOList: MutableList<SubMerchantAcquirerDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    errorListCli.filter { it1 -> it1.subMerchantAcquirerId == it.subMerchantAcquirerId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
            if (errorListCli.isEmpty()) {
                return ResponseEntity(HttpStatus.OK)
            }
            response.setContentType("text/csv")
            response.setHeader("Content-Disposition", "attachment; filename=data.csv")
            try {
                if (errorListCli.isNotEmpty()) {
                    val errorDTOListOut: List<SubMerchantAcquirerDTO> =
                        errorListCli.map { it -> dtoService.toSubMerchantDTO(it) }
                    if (listSubMerchantAcquirer.size == errorListCli.size) {
                        csvServcive.writeSubMerchantoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.BAD_REQUEST)
                    } else {
                        csvServcive.writeSubMerchantoCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.MULTI_STATUS)
                    }
                }
            } catch (e: IOException) {
                return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else {
            print("returning null")
            return null
        }
        return null
    }

    @PostMapping(
        value = [
            "/merchant/acquirer/upload-outlet-csv"
        ],
        produces = ["text/csv"]
    )
    @Operation(summary = "save from List of outlets")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun uploadOutlet(
        principal: Authentication,
        @RequestBody file: MultipartFile,
        response: HttpServletResponse
    ): ResponseEntity<Array<Byte>>?
    /*    ResponseEntity<InputStreamResource>?*/ {
        if (file.isEmpty) {
            throw BadRequestException(ErrorCodes.INVALID_ID)
        }
        val fileReader = BufferedReader(InputStreamReader(file.inputStream, "UTF-8"))

        // val listOutlet: List<OutletDTO> = csvServcive.readCsvOutlet2(fileReader)
        val listOutlet: List<Outlet> = csvServcive.readCsvOutlet(fileReader)
        val errorListCli: MutableList<Outlet> = mutableListOf()
        listOutlet.forEach { it ->
            if (it.error?.isNotEmpty() == true) {
                errorListCli.add(it)
            }
        }
        if (listOutlet.isNotEmpty()) {
            val errorList: List<Outlet> =
                acquirerAdministrationService.saveOutletList(listOutlet)
            var errorDTOList: MutableList<OutletDTO> = mutableListOf()
            errorList.forEach { it ->
                if (it.error?.isNotEmpty() == true) {
                    errorListCli.filter { it1 -> it1.outletId == it.outletId }.map { it1 ->
                        {
                            if (it1.error.isNullOrEmpty()) {
                                it1.error = it.error
                            }
                        }
                    }
                }
            }
            if (errorListCli.isEmpty()) {
                return ResponseEntity(HttpStatus.OK)
            }
            response.setContentType("text/csv")
            response.setHeader("Content-Disposition", "attachment; filename=data.csv")
            try {
                if (errorListCli.isNotEmpty()) {
                    val errorDTOListOut: List<OutletDTO> =
                        errorListCli.map { it -> dtoService.toOutletDTO(it) }
                    if (listOutlet.size == errorListCli.size) {
                        csvServcive.writeOutletToCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.BAD_REQUEST)
                    } else {
                        csvServcive.writeOutletToCSV(errorDTOListOut, response)
                        return ResponseEntity(HttpStatus.MULTI_STATUS)
                    }
                }
            } catch (e: IOException) {
                return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else {
            print("returning null")
            return null
        }
        return null
    }
}

data class MerchantAcquirerRequest(
    val merchantAcquirerId: Long? = null,
    var merchantAcquirerPreferenceId: String,
    val merchantAcquirerMerchantGroupId: Long,
    var merchantAcquirerName: String,
    var merchantAcquirerStatus: String,
    val merchantAcquirerEntityBankDetailsId: Long,
    val merchantAcquirerEntityAddressId: Long,
    val merchantAcquirerEntityContactDetailsId: Long,
    val merchantAcquirerEntityAdminDetailsId: Long,
    val merchantAcquirerEntityOthersId: Long,
    val merchantAcquirerEntityInfoId: Long
)
