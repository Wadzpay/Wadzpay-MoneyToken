package com.vacuumlabs.wadzpay.asset

import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.asset.models.AssetCreationRequest
import com.vacuumlabs.wadzpay.asset.models.AssetCreationRequestRepository
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.AlgoCustomTokenConfiguration
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletUserController
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.LedgerInitializerService
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.Account
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class AssetService(
    val assetCreationRequestRepository: AssetCreationRequestRepository,
    val algoCutomeTokenService: AlgoCutomeTokenService,
    val ledgerInitializerService: LedgerInitializerService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val ledgerService: LedgerService,
    val issuanceWalletService: IssuanceWalletService,
    val userAccountService: UserAccountService,
    val algoCustomTokenConfiguration: AlgoCustomTokenConfiguration
) {
    fun requestAssetCreation(
        request: AssetCustomController.AssetCreationRequestData,
        userAccount: UserAccount
    ): AssetCustomController.AssetCreationResponse {
        val requestedData = assetCreationRequestRepository.getByAssetName(request.tokenName)
        if (requestedData != null) {
            throw BadRequestException(ErrorCodes.TOKEN_NAME_ALREADY_AVAILABLE)
        }
        val assetCreationRequest = AssetCreationRequest(
            assetName = request.tokenName,
            assetUnit = request.tokenUnit,
            assetAmount = request.tokenAmount,
            decimalPlaces = request.decimalPlaces.toString(),
            requesterId = userAccount,
            requestState = AssetCustomController.RequestState.PENDING,
            requestDate = Instant.now(),
            assetType = request.assetType,
            assetCategory = request.assetCategory,
            assetUnitQuantity = request.assetUnitQuantity
        )
        val requestSavedData = assetCreationRequestRepository.save(assetCreationRequest)

        return AssetCustomController.AssetCreationResponse(
            tokenCreationRequestId = requestSavedData.assetCreationRequestId,
            tokenName = requestSavedData.assetName,
            tokenUnit = requestSavedData.assetUnit,
            tokenAmount = requestSavedData.assetAmount,
            decimalPlaces = requestSavedData.decimalPlaces,
            requestState = requestSavedData.requestState.toString(),
            requestDate = requestSavedData.requestDate,
            completedState = requestSavedData.completedState,
            dateOfProvision = requestSavedData.dateOfProvision,
            provisionAccount = requestSavedData.provisionAccount,
            provisionedTokenId = requestSavedData.provisionedTokenId
        )
    }

    fun getAllAssetCreationRequest(
        requestSate: AssetCustomController.RequestState,
        userAccount: UserAccount
    ): MutableList<AssetCustomController.AssetCreationResponse>? {
        val resData = mutableListOf<AssetCustomController.AssetCreationResponse>()
        assetCreationRequestRepository.getByRequestState(requestSate)?.forEach { data ->
            val assetData = AssetCustomController.AssetCreationResponse(
                tokenCreationRequestId = data.assetCreationRequestId,
                tokenName = data.assetName,
                tokenUnit = data.assetUnit,
                tokenAmount = data.assetAmount,
                decimalPlaces = data.decimalPlaces,
                requestState = data.requestState.toString(),
                requestDate = data.requestDate,
                completedState = data.completedState,
                dateOfProvision = data.dateOfProvision,
                provisionAccount = data.provisionAccount,
                provisionedTokenId = data.provisionedTokenId
            )
            resData.add(assetData)
        }
        return resData
    }

    fun createWalletAddressForIssuerToAsset(tokenName: String, issuerUserAccount: UserAccount, endCustomerUserAccount: UserAccount?): String {
        val requestedData = assetCreationRequestRepository.getByRequestStateAndRequesterIdAndAssetName(AssetCustomController.RequestState.APPROVED, issuerUserAccount, tokenName)
        if (requestedData != null) {
            val data = requestedData[0]
            if (data.requesterId.account.owner != null) {
                var accountOwner = data.requesterId.account.owner
                /* TODO: This call will create sub account with blockchain address */
                if (endCustomerUserAccount != null) {
                    accountOwner = endCustomerUserAccount.account.owner
                }
                ledgerService.createAccountsForPrivateBC1(accountOwner!!, tokenName)
            }

            return "success"
        } else {
            throw BadRequestException(ErrorCodes.TOKEN_NOT_FOUND)
        }
    }

    fun createWalletAddressForUserAccount(assetName: String, issuerUserAccount: UserAccount, endCustomerUserAccount: UserAccount): MutableList<Account> {
        val requestedData = assetCreationRequestRepository.getByRequestStateAndRequesterIdAndAssetName(AssetCustomController.RequestState.APPROVED, issuerUserAccount, assetName)
        if (requestedData != null) {
            return ledgerService.createAccountsForPrivateBC1(endCustomerUserAccount.account.owner!!, assetName)
        } else {
            throw BadRequestException(ErrorCodes.TOKEN_NOT_FOUND)
        }
    }

    fun provisionToAsset(tokenName: String, userAccount: UserAccount): AssetCustomController.AssetCreationResponse {
        var requestedData = assetCreationRequestRepository.getByRequestStateAndAssetName(AssetCustomController.RequestState.PENDING, tokenName)
        if (requestedData != null) {
            requestedData.completedState = AssetCustomController.RequestState.PENDING.toString()
            requestedData = assetCreationRequestRepository.save(requestedData)
            val creatorAddress = algoCustomTokenConfiguration.creatorAddress
            val bcRes = requestedData.decimalPlaces?.let {
                algoCutomeTokenService.getNewAlgoToken(
                    tokenName, creatorAddress, requestedData!!.assetAmount.toString(),
                    it.toInt(),
                    requestedData!!.assetUnit
                )
            }
            if (bcRes != null) {
                println("Asset Request complete")
                requestedData.dateOfProvision = Instant.now()
                requestedData.provisionedTokenId = bcRes
                requestedData.requestState = AssetCustomController.RequestState.APPROVED
                requestedData.provisionUserId = userAccount
                requestedData.provisionAccount = creatorAddress
                requestedData.completedState = AssetCustomController.RequestState.APPROVED.toString()
                requestedData = assetCreationRequestRepository.save(requestedData)
                /* TODO: This call will make entry in DB table asset */
                ledgerInitializerService.createAccountsForPrivateAsset(tokenName)
            } else {
                requestedData.completedState = AssetCustomController.RequestState.FAILED.toString()
                requestedData = assetCreationRequestRepository.save(requestedData)
            }
            return AssetCustomController.AssetCreationResponse(
                tokenCreationRequestId = requestedData.assetCreationRequestId,
                tokenName = requestedData.assetName,
                tokenUnit = requestedData.assetUnit,
                tokenAmount = requestedData.assetAmount,
                decimalPlaces = requestedData.decimalPlaces,
                requestState = requestedData.requestState.toString(),
                requestDate = requestedData.requestDate,
                completedState = requestedData.completedState,
                dateOfProvision = requestedData.dateOfProvision,
                provisionAccount = requestedData.provisionAccount,
                provisionedTokenId = requestedData.provisionedTokenId
            )
        } else {
            throw BadRequestException(ErrorCodes.TOKEN_NOT_FOUND)
        }
    }

    fun getAssetForInstitutionOfUser(userAccount: UserAccount, tokenType: String?): MutableList<AssetCreationRequest>? {
        var userAccount = userAccount
        if (userAccount.issuanceBanks == null) {
            val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            val issuanceBank = issuanceBanksUserEntry?.issuanceBanksId
            userAccount = issuanceBank?.email?.let { userAccountService.getUserAccountByEmail(it) }!!
        }
        return if (!tokenType.isNullOrEmpty()) {
            assetCreationRequestRepository.getByRequestStateAndRequesterIdAndAssetName(
                AssetCustomController.RequestState.APPROVED,
                userAccount,
                tokenType
            )
        } else {
            assetCreationRequestRepository.getByRequestStateAndRequesterId(
                AssetCustomController.RequestState.APPROVED,
                userAccount
            )
        }
    }

    fun getWalletUserInstitutionDeatils(userAccount: UserAccount): UserAccount {
        var userAccount = userAccount
        if (userAccount.issuanceBanks == null) {
            val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            val issuanceBank = issuanceBanksUserEntry?.issuanceBanksId
            userAccount = issuanceBank?.email?.let { userAccountService.getUserAccountByEmail(it) }!!
        }
        return userAccount
    }
    fun refreshAssetCache() {
        println("inside refresh cache start")
        val assetCache = algoCutomeTokenService.getAssetsOnAlgoNtFromCache()
        println("inside refresh cache complete")
    }

    fun validateAsset(assetName: String, userAccount: UserAccount): Boolean {
        val requestedData = assetCreationRequestRepository.getByRequestStateAndRequesterIdAndAssetName(AssetCustomController.RequestState.APPROVED, userAccount, assetName)
        println("requestedData ==> $requestedData")
        return if (requestedData != null && requestedData.isEmpty()) return false else true
    }

    fun getAssetListByIssuance(userAccount: UserAccount): MutableList<AssetCreationRequest>? {
        return assetCreationRequestRepository.getByRequestStateAndRequesterId(AssetCustomController.RequestState.APPROVED, userAccount)
    }

    fun validateTokenAmount(
        noOfTokens: BigDecimal,
        typeOfTransaction: IssuanceWalletUserController.TransactionTypeList,
        userAccount: UserAccount,
        tokenType: String
    ): Boolean {
        /* val apiAction = apiCallCodeRepository.getByApiAction(typeOfTransaction.toString())
         if (apiAction != null) {
             val assetQuantity = assetQuantityRepository.getAssetQuantitiesByApiCallCodeAndIssuanceBanksAndAsset(
                 apiAction.apiCode?.toLong(),
                 userAccount.issuanceBanks,
                 tokenType
             ) */
            /*var minAmount: BigDecimal? = null
            if (!assetQuantity.isNullOrEmpty()) {
                assetQuantity.forEach { data ->
                    minAmount = data.minTransferAmount
                }
            }
            println("noOfTokens ==> $noOfTokens")
            println("minAmount ==> $minAmount")
            return if (minAmount != null) {
                noOfTokens >= minAmount
            } else {
                true
            }*/
        return true
    }

    fun getAssetTokenList(
        requestSate: AssetCustomController.RequestState,
        userAccount: UserAccount
    ): AssetCustomController.ListAssetNames {
        var assetCreationList: MutableList<AssetCreationRequest>?
//        assetCreationList = assetCreationRequestRepository.findAll() as MutableList<AssetCreationRequest>
//        assetCreationList = assetCreationRequestRepository.getByRequestState(requestSate)
        assetCreationList = assetCreationRequestRepository.getByRequestStateAndRequesterId(requestSate, userAccount)
        var assetNameList: MutableList<String>? = mutableListOf()
        var assetUnitList: MutableList<String>? = mutableListOf()
        var assetAmountList: MutableList<String>? = mutableListOf()
        var assetProvisionList: MutableList<String>? = mutableListOf()
        var assetTypeList: MutableList<String>? = mutableListOf()
        var assetCategoryList: MutableList<String>? = mutableListOf()
        var assetUnitQuantityList: MutableList<String>? = mutableListOf()
        if (assetCreationList != null) {
            for (i in assetCreationList.indices) {
                assetNameList?.add(assetCreationList[i].assetName)
                assetUnitList?.add(assetCreationList[i].assetUnit)
                assetAmountList?.add(assetCreationList[i].assetAmount.toString())
                assetProvisionList?.add(assetCreationList[i].provisionedTokenId.toString())
                assetTypeList?.add(assetCreationList[i].assetType.toString())
                assetCategoryList?.add(assetCreationList[i].assetCategory.toString())
                assetUnitQuantityList?.add(assetCreationList[i].assetUnitQuantity.toString())
            }
        }
        println(assetNameList?.size)
        println(assetNameList)

        val assetNameData = AssetCustomController.ListAssetNames(
            tokenListName = "Token List",
            assetNameList = assetNameList,
            assetUnitList = assetUnitList,
            assetAmountList = assetAmountList,
            assetProvisionList = assetProvisionList,
            assetTypeList = assetTypeList,
            assetCategoryList = assetCategoryList,
            assetUnitQuantityList = assetUnitQuantityList,
        )
        println(assetNameData.tokenListName)
        return assetNameData
    }
}
