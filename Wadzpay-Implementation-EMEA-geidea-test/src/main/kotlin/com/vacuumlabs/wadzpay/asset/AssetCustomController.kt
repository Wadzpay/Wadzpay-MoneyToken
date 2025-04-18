package com.vacuumlabs.wadzpay.asset

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.user.UserAccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/asset")
@Tag(name = "Asset Creation Controller")
@Validated
class AssetCustomController(
    val assetService: AssetService,
    val userAccountService: UserAccountService
) {
    @PostMapping("/requestAssetCreation")
    @Operation(summary = "Request for token creation from issuer")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Request saved successfully"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_INPUT_FORMAT} (${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.INVALID_PASSWORD}) - ValidationErrorResponse<br>${ErrorCodes.UNVERIFIED_PHONE_NUMBER} - ErrorResponse<br>" +
                "One of these error codes: https://github.com/firebase/firebase-admin-java/blob/104ab0dcefcbbff24b6b3fc747f3f363854185f2/src/main/java/com/google/firebase/auth/AuthErrorCode.java - ErrorResponse",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
    )
    fun requestAssetCreation(
        @RequestBody request: AssetCreationRequestData,
        principal: Authentication
    ): AssetCreationResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return assetService.requestAssetCreation(request, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/getAllAssetCreationRequest")
    @Operation(summary = "Get All asset creation Request.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get Asset request"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.NO_DATA_SENT} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAllAssetCreationRequest(
        @RequestParam(required = true, name = "requestSate") requestSate: RequestState,
        principal: Authentication
    ): MutableList<AssetCreationResponse>? {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return assetService.getAllAssetCreationRequest(requestSate, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @PostMapping("/provisionToAsset")
    @Operation(summary = " Asset creation by admin.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Asset creation request"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.NO_DATA_SENT} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun provisionToAsset(
        @RequestParam(required = true, name = "tokenName") tokenName: String,
        principal: Authentication
    ): AssetCreationResponse {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val asset = assetService.provisionToAsset(tokenName, userAccount)
        assetService.refreshAssetCache()
        return asset
    }

    @PostMapping("/createWalletAddressForIssuer")
    @Operation(summary = " Wallet address creation for Given Asset")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Wallet address creation done."),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.NO_DATA_SENT} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createWalletAddressForIssuer(
        @RequestParam(required = true, name = "tokenName") tokenName: String,
        principal: Authentication
    ): String {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        return assetService.createWalletAddressForIssuerToAsset(tokenName, userAccount, null)
    }

    @GetMapping("/assetTokenList")
    @Operation(summary = "Get All Asset List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get Asset request"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.NO_DATA_SENT} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAssetTokenList(
        @RequestParam(required = true, name = "requestSate") requestSate: RequestState,
        principal: Authentication
    ): ListAssetNames {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return assetService.getAssetTokenList(requestSate, userAccount)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    data class AssetCreationRequestData(
        val tokenName: String,
        val tokenUnit: String,
        val tokenAmount: Long,
        val decimalPlaces: Int? = null,
        val assetType: String,
        val assetCategory: String,
        val assetUnitQuantity: BigDecimal? = null
    )

    data class AssetCreationResponse(
        val tokenCreationRequestId: Long,
        val tokenName: String,
        val tokenUnit: String,
        val tokenAmount: Long,
        val decimalPlaces: String? = null,
        val requestState: String,
        val requestDate: Instant,
        val completedState: String? = null,
        val dateOfProvision: Instant? = null,
        val provisionAccount: String? = null,
        val provisionedTokenId: String? = null
    )

    enum class RequestState {
        PENDING,
        APPROVED,
        FAILED
    }

    data class AssetNameResponse(
        val assetName: String
    )

    data class ListAssetNames(
        val tokenListName: String,
        val assetNameList: MutableList<String>? = mutableListOf(),
        val assetUnitList: MutableList<String>? = mutableListOf(),
        val assetAmountList: MutableList<String>? = mutableListOf(),
        val assetProvisionList: MutableList<String>? = mutableListOf(),
        val assetTypeList: MutableList<String>? = mutableListOf(),
        val assetCategoryList: MutableList<String>? = mutableListOf(),
        val assetUnitQuantityList: MutableList<String>? = mutableListOf(),
    )
}
