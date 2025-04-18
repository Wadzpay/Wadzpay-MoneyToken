package com.vacuumlabs.wadzpay.accountowner

import com.vacuumlabs.MERCHANT_API_VERSION
import com.vacuumlabs.SALT_KEY
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.wadzpay.a.EncoderUtil
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCustomTokenWallet
import com.vacuumlabs.wadzpay.algocutomtoken.AlgoCutomeTokenService
import com.vacuumlabs.wadzpay.algocutomtoken.models.BcTransInfo
import com.vacuumlabs.wadzpay.bitgo.BitGoWallet
import com.vacuumlabs.wadzpay.bitgo.WalletService
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import com.vacuumlabs.wadzpay.issuance.IssuanceConfigurationService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.Status
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.ledger.model.CryptoAddress
import com.vacuumlabs.wadzpay.ledger.model.Subaccount
import com.vacuumlabs.wadzpay.ledger.model.SubaccountRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionDirection
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.model.getDirection
import com.vacuumlabs.wadzpay.ledger.service.GetAcceptApproveRejectRequest
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionSettlementListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.OrderService
import com.vacuumlabs.wadzpay.merchant.model.CustomerOfflineWrongPasswordEntry
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import com.vacuumlabs.wadzpay.pos.PosService
import com.vacuumlabs.wadzpay.pos.PosTransactionRepository
import com.vacuumlabs.wadzpay.user.BcTransactionIdRequest
import com.vacuumlabs.wadzpay.user.CreatePeerToPeerTransactionRequest
import com.vacuumlabs.wadzpay.user.LoadCustomTokenRequest
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.user.UserInitializerService
import com.vacuumlabs.wadzpay.viewmodels.RefundTransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.SettlementReport
import com.vacuumlabs.wadzpay.viewmodels.TransactionSettlement
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toViewModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.Principal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import kotlin.streams.toList

@RestController
@RequestMapping
@Tag(name = "User account and merchant methods")
@Validated
class AccountOwnerController(
    val transactionService: TransactionService,
    val accountOwnerService: AccountOwnerService,
    val userAccountService: UserAccountService,
    val ledgerService: LedgerService,
    val orderService: OrderService,
    val walletService: WalletService,
    val posService: PosService,
    val bitGoWallet: BitGoWallet,
    val algoCustomTokenWallet: AlgoCustomTokenWallet,
    val algoCutomeTokenService: AlgoCutomeTokenService,
    val subaccountRepository: SubaccountRepository,
    val issuanceWalletService: IssuanceWalletService,
    val ledgerServiceWadzPay: LedgerService,
    val posTransactionRepository: PosTransactionRepository,
    val userInitializerService: UserInitializerService,
    val issuanceConfigurationService: IssuanceConfigurationService
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    //
    @GetMapping(
        value = [
            "merchant/transaction/search"
        ]
    )
    @Operation(summary = "Get list of transactions for authenticated user or merchant")
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
    fun getSearchTransactions(
        @Valid request: GetTransactionListRequest,
        principal: Authentication,
    ): List<TransactionViewModel> {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        if (request.status == null) {
            request.status =
                mutableListOf(
                    TransactionStatus.OVERPAID,
                    TransactionStatus.UNDERPAID,
                    TransactionStatus.SUCCESSFUL,
                    TransactionStatus.IN_PROGRESS,
                    TransactionStatus.FAILED
                )
        }

        val result = transactionService.getTransactionViewModels(accountOwner, request)

        var result1: ArrayList<TransactionViewModel> = ArrayList()

        if (request.posId != null) {
            result.forEach {
                if (it.fiatAmount != BigDecimal.ZERO && it.posId == request.posId) {
                    result1.add(it)
                }
            }
        } else {
            result.forEach {
                if (it.fiatAmount != BigDecimal.ZERO) {
                    result1.add(it)
                }
            }
        }

        if (request.page != null) {
            val pageNo = request.page
            result1 = result1.stream().skip(pageNo * 10L).limit(10).toList() as ArrayList<TransactionViewModel>
        }

        return result1
    }

    @GetMapping(
        value = [
            "pos/merchant/settlement"
        ]
    )
    @Operation(summary = "Get list of transactions for authenticated merchantDashboard")
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
    fun settlementReport(
        principal: Authentication,
        settlementRequestBody: SettlementRequestBody,
    ): List<SettlementReport> {

        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return transactionService.getSettlementReport(
            accountOwner,
            settlementRequestBody.from,
            settlementRequestBody.to,
            settlementRequestBody.posId
        )
    }

    @GetMapping(
        value = [
            "pos/merchant/refundSettlement"
        ]
    )
    @Operation(summary = "Get list of all transactions for authenticated merchantDashboard")
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
    fun refundSettlementReport(
        principal: Authentication,
        settlementRequestBody: SettlementRequestBody,
    ): String {

        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        transactionService.getRefundSettlementReport(
            accountOwner,
            settlementRequestBody.from,
            settlementRequestBody.to,
            settlementRequestBody.posId
        )
        return "Available In Next Release"
    }

    @GetMapping(
        value = [
            "user/transactions/{transactionId}",
            "$MERCHANT_API_VERSION/merchant/transactions/{transactionId}",
            "merchant/transactions/{transactionId}"
        ]
    )
    @Operation(summary = "Get a single transaction for authenticated user or merchant")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.MERCHANT_NOT_FOUND}, ${ErrorCodes.TRANSACTION_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getUserTransaction(@PathVariable transactionId: UUID, principal: Authentication): TransactionViewModel {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val transactionViewModel = transactionService.getByIdAndOwner(transactionId, accountOwner).let {
            it.toViewModel(direction = it.getDirection(accountOwner))
        }
        transactionViewModel.feeConfigData = transactionService.getFeeData(transactionViewModel.uuid)
        return transactionViewModel
    }

    @GetMapping(
        value = [
            "user/transactions",
            "$MERCHANT_API_VERSION/merchant/transactions",
            "merchantDashboard/transactions"
        ]
    )
    @Operation(summary = "Get list of transactions for authenticated user or merchant")
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
    fun getTransactions(
        @Valid request: GetTransactionListRequest,
        principal: Authentication,
    ): List<TransactionViewModel>? {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val temp = mutableListOf(
            TransactionType.BUY,
            TransactionType.EXTERNAL_SEND,
            TransactionType.SELL,
            TransactionType.PEER_TO_PEER,
            TransactionType.EXTERNAL_RECEIVE,
            TransactionType.MERCHANT,
            TransactionType.OFF_RAMP,
            TransactionType.ON_RAMP,
            TransactionType.ORDER,
            TransactionType.POS,
            TransactionType.REFUND,
            TransactionType.SWAP,
            TransactionType.OTHER,
            TransactionType.SERVICE_FEE,
            TransactionType.WALLET_FEE
        )
        temp.remove(TransactionType.SYNC_BLOCKCHAIN)
        if (request.asset?.contains(CurrencyUnit.USDT) == true && request.type.isNullOrEmpty()) {
            request.type = temp
        } else if (request.asset?.contains(CurrencyUnit.SART) == true && request.type.isNullOrEmpty()) {
            temp.add(TransactionType.DEPOSIT)
            temp.add(TransactionType.WITHDRAW)
            request.type = temp
        } else {
            if (request.asset?.contains(CurrencyUnit.USDT) == true) {
                if (request.type?.contains(TransactionType.WITHDRAW) == true || request.type?.contains(TransactionType.DEPOSIT) == true || request.type?.contains(
                        TransactionType.FIAT_PEER_TO_PEER
                    ) == true
                ) {
                    return listOf()
                }
            }
        }
        /*This requirement for CBD - Need to search on basis of amount, amount and fiat amount both */
        if (request.appSearch == true && !request.search.isNullOrEmpty()) {
            temp.add(TransactionType.DEPOSIT)
            temp.add(TransactionType.WITHDRAW)
            if (request.type == null) {
                request.type = temp
            }
            if (checkNumber(request.search!!)) {
                request.amount = request.search!!.toBigDecimal()
                request.search = null
                if (request.type == null) {
                    request.type = temp
                }
                val response: MutableList<TransactionViewModel> = transactionService.getTransactionViewModels(
                    accountOwner,
                    request
                ) as MutableList<TransactionViewModel>
                request.fiatAmount = request.amount
                request.amount = null
                val fiatTypeList = mutableListOf(TransactionType.DEPOSIT, TransactionType.WITHDRAW)
                request.type = fiatTypeList
                val resultFiatAmount: MutableList<TransactionViewModel> = mutableListOf()
                transactionService.getTransactionViewModels(
                    accountOwner,
                    request
                ) as MutableList<TransactionViewModel>
                response.addAll(resultFiatAmount)
                response.sortedByDescending { list -> list.createdAt }
                return response
            } else if (checkUUID(request.search!!)) {
                request.uuid = UUID.fromString(request.search)
                request.search = null
            } else {
                temp.add(TransactionType.DEPOSIT)
                temp.add(TransactionType.WITHDRAW)
                request.search = request.search!!.toLowerCase()
            }
        }
        return transactionService.getTransactionViewModels(accountOwner, request)
    }

    @GetMapping("/merchant/refund/transactions")
    @Operation(summary = "Get list of transactions for Refund merchantDashboard")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun refundTransactionReport(
        principal: Authentication,
        @Valid request: GetTransactionListRequest,
    ): List<RefundTransactionViewModel> {
        println("refundTransactionReport")
        if (request.status == null) {
            request.status =
                mutableListOf(TransactionStatus.OVERPAID, TransactionStatus.UNDERPAID, TransactionStatus.SUCCESSFUL)
        }

        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return transactionService.getRefundTransactionViewModels(accountOwner, request)
    }

    @PostMapping("/merchant/transaction/approve")
    @Operation(summary = "Accept Or Reject Refund Request from  MerchantDashboard for Acceptance and Approval")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acceptOrApproveRefundableTransaction(
        principal: Authentication,
        @RequestBody request: GetAcceptApproveRejectRequest,
    ): RefundTransactionViewModel? {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return transactionService.updateAcceptApproveRejectDetails(request, accountOwner, principal)
    }

    @PostMapping("/merchant/transaction/approveQAR")
    @Operation(summary = "Accept Or Reject Refund Request from  MerchantDashboard for Acceptance and Approval")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun acceptOrApproveRefundableTransactionQAR(
        principal: Authentication,
        @RequestBody request: GetAcceptApproveRejectRequest,
    ): RefundTransactionViewModel? {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        return transactionService.updateAcceptApproveRejectDetailsQAR(request, accountOwner, principal)
    }

    @PostMapping("/merchant/refund/approve")
    @Operation(summary = "Accept Or Reject Refund Request from  MerchantDashboard for Acceptance only")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = ErrorCodes.INVALID_WALLET_ADDRESS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun refundOneLevelApproveRejectTransaction(
        principal: Authentication,
        @RequestBody request: GetAcceptApproveRejectRequest,
    ): RefundTransactionViewModel? {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return transactionService.updateOneLevelApproveRejectTransaction(request, accountOwner, principal)
    }

    @GetMapping(
        value = [
            "user/algotokenbalances",
            "$MERCHANT_API_VERSION/merchant/algotokenbalances",
            "merchantDashboard/algotokenbalances"
        ]
    )
    @Operation(summary = "Get balance for each asset which authenticated algo user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of balances",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun algoCustomTokenBalance(principal: Authentication): Map<String, BigDecimal> {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val balanceMap: HashMap<String, BigDecimal> = HashMap<String, BigDecimal>()
        // balanceMap[CurrencyUnit.SART.toString()] = BigDecimal( 120)
        // logger.info("algo Cutom  token balance from controller")
        // for (subacc in accountOwner.getAccount(AccountType.MAIN).subaccounts) {
        // subacc.asset.toString

        val subacc = accountOwner.account.getSubaccountByAsset(CurrencyUnit.SART)
        // val subacc = accountOwner.account.subaccounts.find { it.reference.value.contains("SART", true) }
        // logger.info("sub acc info :${subacc?.reference} ")
        val addrs = subacc.address?.address
        if (addrs == null) {
            // val bal = algoCutomeTokenService.getAlgoTokenBalance("abcd")
            balanceMap[CurrencyUnit.SART.toString()] = BigDecimal(0)
        } else {
            val bal1 = algoCutomeTokenService.getAlgoTokenBalance(addrs)
            balanceMap[CurrencyUnit.SART.toString()] = BigDecimal(bal1)
            ledgerService.syncBlockChainBalance(accountOwner)
        }

        // }
        return balanceMap
    }

    @GetMapping(
        value = [
            "user/balances",
            "$MERCHANT_API_VERSION/merchant/balances",
            "merchantDashboard/balances"
        ]
    )
    @Operation(summary = "Get balance for each asset which authenticated user or merchant holds")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of balances",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getBalances(principal: Authentication): Map<String, BigDecimal> {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        val digitalBalance = ledgerService.getBalances(accountOwner)
        var digitalBalanceMap: HashMap<String, BigDecimal> = HashMap<String, BigDecimal>()
        val itr2 = digitalBalance.keys.iterator()
        while (itr2.hasNext()) {
            val key = itr2.next()
            var value = digitalBalance[key]
            if (value != null) {
                if (value.stripTrailingZeros().scale() > 8) {
                    value = value.setScale(8, RoundingMode.FLOOR)
                    digitalBalanceMap[key] = value
                } else {
                    digitalBalanceMap[key] = value
                }
            } else {
                digitalBalanceMap = digitalBalance as HashMap<String, BigDecimal>
            }
        }
        return digitalBalanceMap
    }

    @GetMapping(
        value = [
            "user/order/{orderId}",
            "$MERCHANT_API_VERSION/merchant/order/{orderId}",
            "merchantDashboard/order/{orderId}"
        ]
    )
    @Operation(summary = "Get order by it's id")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = ErrorCodes.ORDER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getOrder(@PathVariable orderId: UUID): Order {
        return orderService.getOrderByUuid(orderId)
    }

    @PostMapping(
        value = [
            "user/account/loadcustomtokens",
            "$MERCHANT_API_VERSION/merchant/loadcustomtokens",
            "merchantDashboard/admin/loadcustomtokens"
        ]
    )
    @Operation(summary = "load custom tokens SART")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction successful"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS},${ErrorCodes.CANNOT_SEND_MERCHANT_TO_MERCHANT}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun loadAlgoCustomTokens(
        @Valid @RequestBody request: LoadCustomTokenRequest,
        principal: Authentication,
    ): String {
        try {
            if (request.asset != CurrencyUnit.SART)
                return "Error : Invalid Asset"
            val userAccount = accountOwnerService.extractAccount(principal, useMerchant = true)
            val userAddress = userAccount.accounts[0].getSubaccountByAsset(CurrencyUnit.SART).address?.address
            return if (userAddress != null) {
                val transId = algoCutomeTokenService.loadCustomTokensToAccount(userAddress, request.amount.toString())
                transId
            } else {
                "Fail"
            }
        } catch (e: Exception) {
            logger.info("excception while loadAlgoCustomTokens : $e")
            return e.toString()
        }
    }

    @PostMapping(
        value = [
            "user/account/refundcustomtokens",
            "$MERCHANT_API_VERSION/merchant/refundcustomtokens",
            "merchantDashboard/admin/refundcustomtokens"
        ]
    )
    @Operation(summary = "Redeem unspent custom tokens SART")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction successful"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS},${ErrorCodes.CANNOT_SEND_MERCHANT_TO_MERCHANT}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun refundAlgoCustomTokens(
        @Valid @RequestBody request: LoadCustomTokenRequest,
        principal: Authentication,
    ): String {
        try {
            if (request.asset != CurrencyUnit.SART)
                return "Error : Invalid Asset"
            val userAccount = accountOwnerService.extractAccount(principal, useMerchant = true)
            val userAddress = userAccount.account.getSubaccountByAsset(CurrencyUnit.SART).address!!.address
            val transId = algoCustomTokenWallet.refundUnspentCustomTokens(userAddress, request.amount.toString())
            return "success and Transaction ID :$transId"
        } catch (e: Exception) {
            logger.info("excception while refundAlgoCustomTokens : $e")
            return e.toString()
        }
    }

    @PostMapping(
        value = [
            "user/account/blockchaintransaction",
            "$MERCHANT_API_VERSION/merchant/blockchaintransaction",
            "merchantDashboard/admin/blockchaintransaction"
        ]
    )
    @Operation(summary = "Block Chain Transaction Info")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction successful"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.BAD_REQUEST,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun bcTransactionInfo(
        @Valid @RequestBody request: BcTransactionIdRequest,
        principal: Authentication,
    ): BcTransInfo {
        return run {
            val bcTransactionInfo = algoCutomeTokenService.algoTransactionInfo(request.bcTransId)
            bcTransactionInfo ?: throw NullPointerException("Blockchain Transaction NOT found")
        }
    }

    @PostMapping(
        value = [
            "user/account/p2p_transaction",
            "$MERCHANT_API_VERSION/merchant/p2p_transaction",
            "merchantDashboard/admin/p2p_transaction"
        ]
    )
    @Operation(summary = "Create peer-to-peer transaction")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction successful"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS},${ErrorCodes.CANNOT_SEND_MERCHANT_TO_MERCHANT}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createPeerToPeerTransaction(
        @Valid @RequestBody request: CreatePeerToPeerTransactionRequest,
        principal: Authentication,
    ): TransactionViewModel {
        val senderAccount = accountOwnerService.extractAccount(principal, useMerchant = true)
        val senderUser = userAccountService.getUserAccountByEmail(principal.name)
        val receiverAccount = userAccountService.extractUserAccount(request, senderUser)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        var issuanceBank: IssuanceBanks? = null
        if (request.asset == CurrencyUnit.SART) {
            ledgerServiceWadzPay.getBalances(receiverAccount)
            val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(receiverAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED_RECIPIENT)
            }

            val senderIBank = issuanceWalletService.getIssuanceBankMapping(senderUser)
            if (senderIBank != null) {
                if (senderIBank.status == Status.DISABLED) {
                    throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
                }
                if (senderIBank.issuanceBanksId.p2pTransfer != null && senderIBank.issuanceBanksId.p2pTransfer == false) {
                    throw EntityNotFoundException(ErrorCodes.P2P_TRANSFER_DISABLED)
                }
                issuanceBank = senderIBank.issuanceBanksId
            }
            if (issuanceBanksUserEntry != null) {
                if (issuanceBanksUserEntry.issuanceBanksId != issuanceBank) {
                    throw EntityNotFoundException(ErrorCodes.OTHER_BANK_TRANSFER_NOT_ALLOWED)
                }
            } else {
                throw EntityNotFoundException(ErrorCodes.OTHER_BANK_TRANSFER_NOT_ALLOWED)
            }
            val isActivationFeeDeducted = issuanceConfigurationService.checkActivationFeeAlreadyDeducted(
                IssuanceCommonController.WalletFeeType.WF_001.walletFeeId, receiverAccount
            )
            if (!isActivationFeeDeducted) {
                throw EntityNotFoundException(ErrorCodes.RECIPIENT_WALLET_NOT_ACTIVATED)
            }
        }
        logger.info("algo transfer for p2p transaction")
        val transaction = algoCutomeTokenService.sendToExternalWalletWadzpayPrivateBlockChain(
            senderAccount,
            receiverAccount,
            request.amount,
            request.asset,
            receiverAccount.account.getSubaccountByAsset(request.asset).address!!.address,
            request.description,
            TransactionType.PEER_TO_PEER,
            issuanceBanks = issuanceBank
        )
        if (request.feeConfigData != null && issuanceBank != null) {
            userInitializerService.saveFeeDetails(
                transaction, request.feeConfigData, issuanceBank,
                senderAccount as UserAccount
            )
        }

        transactionService.showPushNotifications(transaction)
        return transaction.toViewModel(direction = TransactionDirection.OUTGOING)
    }

    @PostMapping(
        value = [
            "user/account/sendDigitalCurrencyToExternalWallet",
            "$MERCHANT_API_VERSION/merchant/sendDigitalCurrencyToExternalWallet",
            "merchantDashboard/admin/sendDigitalCurrencyToExternalWallet"
        ]
    )
    @Operation(summary = "Send Digital Currency To External wallet")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction successful"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS}, ${ErrorCodes.EXTERNAL_SEND_INSIDE_THE_SYSTEM}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = "${ErrorCodes.WALLET_NOT_AVAILABLE}, ${ErrorCodes.UNKNOWN_WALLET_ERROR}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun sendExternalTransaction(
        @Valid @RequestBody request: CreateExternalTransactionRequest,
        principal: Authentication,
    ): TransactionViewModel {
        val senderAccount = accountOwnerService.extractAccount(principal, useMerchant = true)
        if (request.asset != CurrencyUnit.SART) {
            val transaction = walletService.sendToExternalWallet(
                senderAccount,
                request.amount,
                request.asset,
                request.receiverAddress,
                request.description, TransactionType.EXTERNAL_SEND
            )
            return transaction.toViewModel(direction = TransactionDirection.OUTGOING)
        } else {
            val receiverAccount = algoCutomeTokenService.getSubAccountByAddress(request.receiverAddress, request.asset)
            val senderAddress = senderAccount.account.getSubaccountByAsset(CurrencyUnit.SART).address?.address
            if (request.receiverAddress == senderAddress) {
                throw BadRequestException(ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME)
            }
            var issuanceBank: IssuanceBanks? = null
            val userAccount = userAccountService.getUserAccountByEmail(principal.name)
            val senderIBank = issuanceWalletService.getIssuanceBankMapping(userAccount)
            if (senderIBank != null) {
                if (senderIBank.status == Status.DISABLED) {
                    throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
                }
                issuanceBank = senderIBank.issuanceBanksId
            }
            if (request.uuid != null) {
                val transaction = posService.posTransactionSart(
                    senderAccount,
                    request.receiverAddress,
                    request.amount,
                    request.asset,
                    request.uuid,
                    issuanceBank,
                    TransactionMode.CUSTOMER_MERCHANT_ONLINE,
                    null,
                    TransactionStatus.SUCCESSFUL
                )
                if (transaction != null) {
                    return transaction.toViewModel(direction = TransactionDirection.OUTGOING)
                }
            } else if (request.merchantId != null && request.posId != null) {
                val transaction = posService.merchantOfflinePosTransactionSart(
                    senderAccount,
                    receiverAccount,
                    request.receiverAddress,
                    request.amount,
                    request.asset,
                    request.merchantId,
                    request.posId,
                    issuanceBank,
                    TransactionStatus.SUCCESSFUL
                )
                if (transaction != null) {
                    return transaction.toViewModel(direction = TransactionDirection.OUTGOING)
                }
            } else {
                if (senderIBank != null && senderIBank.issuanceBanksId.p2pTransfer != null && senderIBank.issuanceBanksId.p2pTransfer == false) {
                    throw EntityNotFoundException(ErrorCodes.P2P_TRANSFER_DISABLED)
                }
                val receiverUserAccount = receiverAccount.account.getSubaccountByAsset(CurrencyUnit.SART).userAccountId
                val receiverMapping = receiverUserAccount?.let { issuanceWalletService.getIssuanceBankMapping(it) }
                if (receiverMapping != null && receiverMapping.status == Status.DISABLED) {
                    throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED_RECIPIENT)
                }
                if (receiverMapping != null) {
                    if (receiverMapping.issuanceBanksId != issuanceBank) {
                        throw EntityNotFoundException(ErrorCodes.OTHER_BANK_TRANSFER_NOT_ALLOWED)
                    }
                } else {
                    throw EntityNotFoundException(ErrorCodes.OTHER_BANK_TRANSFER_NOT_ALLOWED)
                }
                val transaction = algoCutomeTokenService.sendToExternalWalletWadzpayPrivateBlockChain(
                    senderAccount,
                    receiverAccount,
                    request.amount,
                    request.asset,
                    request.receiverAddress,
                    request.description,
                    TransactionType.PEER_TO_PEER,
                    issuanceBanks = issuanceBank
                )
                return transaction.toViewModel(direction = TransactionDirection.OUTGOING)
            }
            throw EntityNotFoundException("Transaction with BlockChain Address ${request.receiverAddress} not found")
        }
    }

    @GetMapping(
        value = [
            "user/account/algocustomaddresses",
            "$MERCHANT_API_VERSION/merchant/algocustomaddresses",
            "merchantDashboard/algocustomaddresses"
        ]
    )
    @Operation(summary = "Gets address")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK")
    )
    fun getSARTAddresses(principal: Authentication): CryptoAddress? {
        val account = accountOwnerService.extractAccount(principal, useMerchant = true)
        var addressval = ""
        var subacc: Subaccount
        try {
            subacc = account.account.getSubaccountByAsset(CurrencyUnit.SART)
            logger.info("sub account found for SART")
        } catch (e: Exception) {
            val Dref = DualReference(UUID.randomUUID().toString())
            subacc = Subaccount(
                account = account.account,
                reference = Dref,
                asset = CurrencyUnit.SART.toString()
            )
            logger.info("sub account in exception creation")
        }
        // var cAddress : CryptoAddress
        if (subacc.address == null) {
            // cAddress = algoCustomTokenWallet.createAddress(CurrencyUnit.SART)
            logger.info("Create address for sub account")
            subacc.address = algoCustomTokenWallet.createAddress(CurrencyUnit.SART.toString())
            logger.info("Create address for sub account : successfull")
            // subacc.address = algoCustomTokenWallet.createAddress(CurrencyUnit.SART)
            subaccountRepository.save(subacc)
        }
        val cryptoAdrs: CryptoAddress = subacc.address!!
        addressval = algoCustomTokenWallet.accountInfoFromMnu(subacc.address!!.address)!!.Address
        cryptoAdrs.address = addressval
        // return subacc.address
        return cryptoAdrs
    }

    @GetMapping(
        value = [
            "user/account/addresses",
            "$MERCHANT_API_VERSION/merchant/addresses",
            "merchantDashboard/addresses"
        ]
    )
    @Operation(summary = "Gets address")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK")
    )
    fun getAddresses(principal: Authentication): List<CryptoAddress?> {
        val account = accountOwnerService.extractAccount(principal, useMerchant = true)
        val address = account.account.subaccounts.map { it.address }
        val logger = LoggerFactory.getLogger(javaClass)
        logger.info(address.toString())
        logger.info("getaddress :$address")
        for (addres in address) {
            if (addres?.address == "") {
                logger.info(addres.toString())
                for (subAcc in account.account.subaccounts) {
                    logger.info(subAcc.toString())
                    if (subAcc.asset == addres.asset) {
                        if (addres.asset == CurrencyUnit.SART.toString()) {
                            logger.info("getAddresses for SART")
                            val newAddress = algoCustomTokenWallet.createAddressNotSave(addres.asset)
                            addres.address = newAddress
                            addres.owner = subAcc
                            subAcc.address = addres
                            // cryptoAddressRepository.save(addres)
                            subaccountRepository.save(subAcc)
                        } else {
                            val newAddress = bitGoWallet.createAddressNotSave(addres.asset)
                            addres.address = newAddress
                            addres.owner = subAcc
                            subAcc.address = addres
                            // cryptoAddressRepository.save(addres)
                            subaccountRepository.save(subAcc)
                        }
                    }
                }
            }
        }
        return account.account.subaccounts.map { it.address }
    }

    //
    @PostMapping("/merchant/refund/approveAlgo")
    @Operation(summary = "Accept Or Reject Refund Request from  MerchantDashboard for Acceptance only")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = ErrorCodes.INVALID_WALLET_ADDRESS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun refundOneLevelApproveRejectTransactionAlgo(
        principal: Authentication,
        @RequestBody request: GetAcceptApproveRejectRequest,
    ): RefundTransactionViewModel? {
        println("@894 refundOneLevelApproveRejectTransactionAlgo")
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return transactionService.updateOneLevelApproveRejectTransactionAlgo(request, accountOwner, principal)
    }

    //
    @PostMapping("/merchant/refund/approveQAR")
    @Operation(summary = "Accept Or Reject Refund Request from  MerchantDashboard for Acceptance only")
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
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.TRANSACTION_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = ErrorCodes.INVALID_WALLET_ADDRESS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun refundOneLevelApproveRejectTransactionAlgoQAR(
        principal: Authentication,
        @RequestBody request: GetAcceptApproveRejectRequest,
    ): RefundTransactionViewModel? {
        println("@894 refundOneLevelApproveRejectTransactionAlgo")
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        return transactionService.updateOneLevelApproveRejectTransactionAlgoQAR(request, accountOwner, principal)
    }

    fun refundAlgoCustomTokensAlgo(
        @Valid @RequestBody request: LoadCustomTokenRequest,
        principal: Authentication,
    ): String {
        try {
            if (request.asset != CurrencyUnit.SART) return "Error : Invalid Asset"
            val userAccount = accountOwnerService.extractAccount(principal, useMerchant = true)
            val userAddress = userAccount.account.getSubaccountByAsset(CurrencyUnit.SART).address!!.address
            val transId = algoCustomTokenWallet.refundUnspentCustomTokens(userAddress, request.amount.toString())
            return "success and Transaction ID :$transId"
        } catch (e: Exception) {
            logger.info("excception while refundAlgoCustomTokens : $e")
            return e.toString()
        }
    }

    @GetMapping("/user/walletDetails")
    @Operation(summary = "Get current logged in wallet details.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Wallet details")
    fun getV1MerchantDetails(principal: Authentication): UserAccount {
        return userAccountService.getUserAccountByEmail(principal.name)
    }

    @PostMapping(
        value = [
            "user/getEncryptedQR"
        ]
    )
    @Operation(summary = "Encrypt Using SaltKey")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Encrypted Data"
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getEncryptedQR(
        principal: Authentication,
        @RequestBody encryptRequest: EncryptQRRequest
    ): String {
        return EncoderUtil.getEncoded(SALT_KEY, encryptRequest.data)
    }

    @PostMapping(
        value = [
            "user/getEncryptedStaticQR"
        ]
    )
    @Operation(summary = "Encrypt Static QR Using SaltKey")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Encrypted Data"
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getEncryptedStaticQR(
        principal: Authentication,
        @RequestBody encryptRequest: EncryptQRRequest
    ): String {
        try {
            val strStaticData: String = encryptRequest.data
            println("strData  ==> $strStaticData")
            val staticDataArray = strStaticData.split("|")
            val transactionIdIndex = staticDataArray.indexOfFirst { it.startsWith("transactionID:") }
            val blockchainAddressIndex = staticDataArray.indexOfFirst { it.startsWith("blockchainAddress:") }
            val typeIndex = staticDataArray.indexOfFirst { it.startsWith("type:") }
            val transactionAmountIndex = staticDataArray.indexOfFirst { it.startsWith("transactionAmount:") }
            val merchantIdIndex = staticDataArray.indexOfFirst { it.startsWith("merchantId:") }
            val posIdIndex = staticDataArray.indexOfFirst { it.startsWith("posId:") }
            val merchantDisplayNameIndex = staticDataArray.indexOfFirst { it.startsWith("merchantDisplayName:") }

            if (transactionIdIndex != -1 || blockchainAddressIndex != -1 || typeIndex != -1 || transactionAmountIndex != -1 || merchantIdIndex != -1 || posIdIndex != -1 || merchantDisplayNameIndex != -1) {
                // Extract the value of "posId" using substring after the colon
                val transactionIdValue = staticDataArray[transactionIdIndex].substringAfter(":")
                println("transactionIdValue ==> $transactionIdValue")
                if (transactionIdValue != "00000") {
                    println("BadRequestException transactionIdIndex")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                }

                val blockchainAddressValue = staticDataArray[blockchainAddressIndex].substringAfter(":")
                println("blockchainAddressValue ==> $blockchainAddressValue.")
                if (blockchainAddressValue.length != 58) {
                    println("BadRequestException blockchainAddressValue")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                }

                val typeValue = staticDataArray[typeIndex].substringAfter(":")
                println("typeValue ==>  $typeValue")
                if (typeValue != "SART") {
                    println("BadRequestException typeValue")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                }

                val amountVal = staticDataArray[transactionAmountIndex].substringAfter(":")
                println("amountVal ==> $amountVal")
                /* val amountValBigDecimal = amountVal.toBigDecimal()
                println("amountInt ==> $amountValBigDecimal")
                if (amountValBigDecimal > BigDecimal.ZERO) {
                    println("BadRequestException amountValBigDecimal")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                } */
                if (amountVal != "0.00") {
                    println("BadRequestException amountVal")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                }

                val posIdValue = staticDataArray[posIdIndex].substringAfter(":")
                println("posIdValue ==>  $posIdValue")
                if (posIdValue != "00000") {
                    println("BadRequestException posIdValue")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                }
                val merchantIdValue: String = staticDataArray[merchantIdIndex].substringAfter(":")
                val merchantNameValue: String = staticDataArray[merchantDisplayNameIndex].substringAfter(":")
                val cryptoAddressVal = getMerchantDetails(principal, merchantIdValue, merchantNameValue)
                println("cryptoAddressVal ==>  $cryptoAddressVal")
                if (blockchainAddressValue != cryptoAddressVal?.address) {
                    println("BadRequestException cryptoAddressVal")
                    throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
                }
            } else {
                println("value found in the data.")
                throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
            }
        } catch (e: Exception) {
            println("Exception@1112 ==> $e")
            throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
        }
        return EncoderUtil.getEncoded(SALT_KEY, encryptRequest.data)
    }

    fun getMerchantDetails(
        principal: Authentication,
        merchantIdValue: String,
        merchantNameValue: String
    ): CryptoAddress? {
        val account = accountOwnerService.extractAccount(principal, useMerchant = true)
        var addressval = ""
        var subacc: Subaccount

        try {
            subacc = account.account.getSubaccountByAsset(CurrencyUnit.SART)
            logger.info("sub account found for SART")
        } catch (e: Exception) {
            val Dref = DualReference(UUID.randomUUID().toString())
            subacc = Subaccount(
                account = account.account,
                reference = Dref,
                asset = CurrencyUnit.SART.toString()
            )
            logger.info("sub account in exception creation")
        }
        var merchantAccountVar = subacc?.account?.owner as Merchant
        if (merchantIdValue != merchantAccountVar.id.toString()) {
            throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
        }

        if (merchantNameValue != merchantAccountVar.name.toString()) {
            throw BadRequestException(ErrorCodes.INVALID_INPUT_FORMAT)
        }

        // var cAddress : CryptoAddress
        if (subacc.address == null) {
            // cAddress = algoCustomTokenWallet.createAddress(CurrencyUnit.SART)
            logger.info("Create address for sub account")
            subacc.address = algoCustomTokenWallet.createAddress(CurrencyUnit.SART.toString())
            logger.info("Create address for sub account : successfull")
            // subacc.address = algoCustomTokenWallet.createAddress(CurrencyUnit.SART)
            subaccountRepository.save(subacc)
        }
        var cryptoAdrs: CryptoAddress = subacc.address!!
        addressval = algoCustomTokenWallet.accountInfoFromMnu(subacc.address!!.address)!!.Address
        cryptoAdrs.address = addressval
        // return subacc.address
        return cryptoAdrs
    }

    @PostMapping(
        value = [
            "user/getDecryptedQR"
        ]
    )
    @Operation(summary = "Decrypt Using Salt Key")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Decrypted Data"
        ),
        ApiResponse(
            responseCode = "403",
            description = ErrorCodes.UNAUTHORIZED,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_QR,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getDecryptedQR(
        principal: Authentication,
        @RequestBody request: EncryptQRRequest
    ): String {
        try {
            return EncoderUtil.getDecoded(SALT_KEY, request.data)
        } catch (e: Exception) {
            throw BadRequestException(ErrorCodes.INVALID_QR)
        }
    }

    //    customer offline
    @PostMapping(
        value = [
            "user/account/merchantSendToExternalWallet",
            "$MERCHANT_API_VERSION/merchant/merchantSendToExternalWallet",
            "merchantDashboard/admin/merchantSendToExternalWallet"
        ]
    )
    @Operation(summary = "Send To External wallet Merchant")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Transaction successful"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_AMOUNT_TOO_MANY_DECIMAL_PLACES},${ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO},${ErrorCodes.INVALID_EMAIL}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND}, ${ErrorCodes.SUBACCOUNT_NOT_FOUND} , ${ErrorCodes.WALLET_DISABLED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = "${ErrorCodes.INSUFFICIENT_FUNDS}, ${ErrorCodes.EXTERNAL_SEND_INSIDE_THE_SYSTEM}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "503",
            description = "${ErrorCodes.WALLET_NOT_AVAILABLE}, ${ErrorCodes.UNKNOWN_WALLET_ERROR}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun sendExternalTransactionMerchant(
        @RequestParam(required = true, name = "passCodeHash") passCodeHash: String,
        @Valid @RequestBody request: MerchantPaymentRequest,
        principal: Principal,
    ): TransactionViewModel {
        val userAccountDetails = algoCutomeTokenService.getUserAccountFromWalletAddress(request.walletUserAddress)
        var emailIdVar: String? = userAccountDetails.email
        var phoneNumberVar: String? = userAccountDetails.phoneNumber
        println("email ==> $emailIdVar")
        println("phoneNumber ==> $phoneNumberVar")
        val requestedPayment = posTransactionRepository.getByUuid(request.uuid.toString()!!)
        println("@1087 $requestedPayment")
        if (request.walletUserAddress == null) {
            throw BadRequestException(ErrorCodes.INVALID_WALLET_ADDRESS_MANDATE)
        }
        val dAmountRequest: Double = request.amount.toDouble()
        val dAmountDB: Double = requestedPayment?.amountcrypto!!.toDouble()
        println("amount Double ==>  $dAmountRequest , $dAmountDB @1270")
        println("amount ==>  ${request.amount} , ${requestedPayment?.amountcrypto} @1271")
        /*if (request.amount != requestedPayment?.amountcrypto) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT)
        }*/
        if (dAmountRequest != dAmountDB) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT)
        }
        val receiverAccount = userAccountService.getUserAccountByEmail(principal.name)
        var senderAccount = emailIdVar?.let { userAccountService.getUserAccountByEmail(it) }
        println("@1099 senderAccount ==> $senderAccount")
        val sAddress = senderAccount?.account?.getSubaccountByAsset(CurrencyUnit.SART)?.address?.address
        println("@1116 senderAccount ==> $sAddress ")
        if (emailIdVar == null) {
            val dataByAddress = algoCutomeTokenService.getSubAccountByAddress(request.walletUserAddress, request.asset)
            senderAccount = dataByAddress as UserAccount
            println("senderAccount ==> $senderAccount")
        }
        /********************    Validate  Start   ****************************/
        if (senderAccount != null) {
            /* Here code for transaction limit  End */
            val alreadyUsedPasscode = ledgerService.getTransactionByPasscodeHash(passCodeHash)
            if (alreadyUsedPasscode != null && alreadyUsedPasscode.size > 0) {
                throw BadRequestException(ErrorCodes.PASSCODE_HASH_ALREADY_USED)
            }
            println(
                emailIdVar + "\n" +
                    senderAccount.phoneNumber + "\n" +
                    senderAccount.firstName.toString() + "\n" +
                    request.uuid
            )
            if (!userAccountService.verifyPasscodeMerchant(
                    passCodeHash,
                    senderAccount,
                    emailIdVar!!,
                    senderAccount.phoneNumber!!,
                    senderAccount.firstName.toString(),
                    request.uuid
                )
            ) {
                userAccountService.sendInvalidPasscodeAlert(
                    emailIdVar,
                    senderAccount.phoneNumber!!,
                    senderAccount.firstName.toString(),
                    request.uuid,
                    Instant.now()
                )

                userAccountService.saveWrongPasswordEntry(
                    CustomerOfflineWrongPasswordEntry(
                        senderId = senderAccount.id.toString(),
                        senderEmail = senderAccount.email,
                        senderName = senderAccount.firstName,
                        receiverId = receiverAccount.merchant?.id.toString(),
                        receiverEmail = receiverAccount.email,
                        receiverName = receiverAccount.merchant?.name
                    )
                )

                throw BadRequestException(ErrorCodes.PASSCODE_NOT_MATCH)
            }
        }
        /********************   Validate  End     ****************************/
        val receiverAddress =
            receiverAccount.merchant?.account?.getSubaccountByAsset(CurrencyUnit.SART)?.address?.address
        println("receiverAddress ==> $receiverAddress")
        println("request.walletUserAddress ==> " + request.walletUserAddress)
        if (request.walletUserAddress == receiverAddress) {
            throw EntityNotFoundException(ErrorCodes.SENDER_RECEIVER_WALLET_ADDRESS_CANT_BE_SAME)
        }
        var issuanceBank: IssuanceBanks? = null
        var issuanceBanksUserEntry = senderAccount?.let { issuanceWalletService.getIssuanceBankMapping(it) }
        if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
            throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
        }
        if (issuanceBanksUserEntry != null) {
            issuanceBank = issuanceBanksUserEntry.issuanceBanksId
        }
        if (request.uuid != null && senderAccount != null && receiverAddress != null) {
            val transaction =
                posService.posTransactionSart(
                    senderAccount,
                    receiverAddress,
                    request.amount,
                    request.asset,
                    request.uuid,
                    issuanceBank,
                    TransactionMode.CUSTOMER_OFFLINE,
                    passCodeHash,
                    TransactionStatus.SUCCESSFUL
                )
            if (transaction != null) {
                return transaction.toViewModel(direction = TransactionDirection.OUTGOING)
            }
        }
        throw EntityNotFoundException("Transaction with BlockChain Address ${request.walletUserAddress} not found")
    }

    @GetMapping(
        value = [
            "user/fetchWalletBalance"
        ]
    )
    @Operation(summary = "Get balance for Assigned asset")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of balances",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAlgoBalances(
        @Valid request: AlgoBalancesRequest,
        principal: Authentication
    ): AlgoBalancesResponse? {
        var userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val issuerUserAccount = userAccount
        var accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        // println("request.customerEmail ==> " + request.customerEmail.isNullOrEmpty())
        // println("request.customerPhoneNumber ==> " + request.customerPhoneNumber.isNullOrEmpty())
        if (!request.customerId.isNullOrEmpty()) {
            userAccount = userAccountService.getUserAccountByCustomerId(
                request.customerId,
                issuerUserAccount.issuanceBanks?.institutionId
            )
            accountOwner = userAccount.account.owner!!
        } /* else if (!request.customerPhoneNumber.isNullOrEmpty()) {
            userAccount = userAccountService.getUserAccountByPhoneNumber(request.customerPhoneNumber)
            accountOwner = userAccount.account.owner!!
        } else if (!request.customerEmail.isNullOrEmpty()) {
            userAccount = userAccountService.getUserAccountByEmail(request.customerEmail)
            accountOwner = userAccount.account.owner!!
        }*/
        // if (!request.customerID.isNullOrEmpty() || !request.customerEmail.isNullOrEmpty() || !request.customerPhoneNumber.isNullOrEmpty()) {
        if (!request.customerId.isNullOrEmpty()) {
            val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(userAccount)
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.status == Status.DISABLED) {
                throw EntityNotFoundException(ErrorCodes.WALLET_DISABLED)
            }
            if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.issuanceBanksId != issuerUserAccount.issuanceBanks) {
                throw EntityNotFoundException(ErrorCodes.CUSTOMER_NOT_FOUND)
            }
        }
        return AlgoBalancesResponse(
            customerId = userAccount.customerId,
            customerEmail = userAccount.email,
            customerPhoneNumber = userAccount.phoneNumber,
            customerName = if (userAccount.issuanceBanks == null) userAccount.firstName + " " + if (userAccount.lastName != null) userAccount.lastName else " " else userAccount.issuanceBanks!!.bankName,
            tokenName = ledgerService.getAlgoBalances(accountOwner, userAccount, request)
        )
    }

    @GetMapping(
        value = [
            "pos/merchant/settlementReport"
        ]
    )
    @Operation(summary = "Get list of transactions for authenticated merchantDashboard")
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
    fun settlementReportNew(
        principal: Authentication,
        @RequestParam(required = true, name = "days") settlementReportDays: SettlementReportDays,
        settlementRequestBody: SettlementRequestBody,
    ): List<SettlementReport> {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)

        println("settlementReportDays ==>  $settlementReportDays ")
        var numberOfDays = 0
        if (settlementReportDays == SettlementReportDays.DAY) {
            numberOfDays = 1
        } else if (settlementReportDays == SettlementReportDays.WEEK) {
            numberOfDays = 7
        } else if (settlementReportDays == SettlementReportDays.MONTH) {
            numberOfDays = 30
        } else if (settlementReportDays == SettlementReportDays.YEAR) {
            numberOfDays = 365
        }
//        val instant = Instant.parse("2018-12-30T19:34:50.63Z")
        val instantNow = Instant.parse(Instant.now().toString())
        val instantDaysRemove = instantNow.minus(numberOfDays.toLong(), ChronoUnit.DAYS)
        println("Instant after subtracting DAYS: $instantDaysRemove")
        var fromSettlement: Instant = instantDaysRemove
        var toSettlement: Instant = Instant.now()
        if (settlementReportDays != SettlementReportDays.CUSTOM) {
            return transactionService.getSettlementReport(
                accountOwner,
                fromSettlement,
                toSettlement,
                settlementRequestBody.posId
            )
        } else {
            return transactionService.getSettlementReport(
                accountOwner,
                settlementRequestBody.from,
                settlementRequestBody.to,
                settlementRequestBody.posId
            )
        }
    }

//
    @GetMapping(
        value = [
            "merchant/transaction/settlement"
        ]
    )
    @Operation(summary = "Get list of transactions for authenticated user or merchant")
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
    fun getSettlementTransactions(
        @RequestParam(required = true, name = "days") settlementReportDays: SettlementReportDays,
        @Valid request: GetTransactionSettlementListRequest,
        principal: Authentication,
    ): List<TransactionSettlement> {
        val accountOwner = accountOwnerService.extractAccount(principal, useMerchant = true)
        if (request.status == null) {
            request.status =
                mutableListOf(
                    TransactionStatus.OVERPAID,
                    TransactionStatus.UNDERPAID,
                    TransactionStatus.SUCCESSFUL
                )
        }
        if (settlementReportDays != SettlementReportDays.CUSTOM) {
            println("settlementReportDays ==>  $settlementReportDays ")
            var numberOfDays = 0
            if (settlementReportDays == SettlementReportDays.DAY) {
                numberOfDays = 1
            } else if (settlementReportDays == SettlementReportDays.WEEK) {
                numberOfDays = 7
            } else if (settlementReportDays == SettlementReportDays.MONTH) {
                numberOfDays = 30
            } else if (settlementReportDays == SettlementReportDays.YEAR) {
                numberOfDays = 365
            }
//        val instant = Instant.parse("2018-12-30T19:34:50.63Z")
            val instantNow = Instant.parse(Instant.now().toString())
            val instantDaysRemove = instantNow.minus(numberOfDays.toLong(), ChronoUnit.DAYS)
            println("Instant after subtracting DAYS: $instantDaysRemove")
            var fromSettlement: Instant = instantDaysRemove
            var toSettlement: Instant = Instant.now()
            request.dateFrom = fromSettlement
            request.dateTo = toSettlement
        } else {
        }
        val result = transactionService.getTransactionSettlementViewModels(accountOwner, request)

        var result1: ArrayList<TransactionSettlement> = ArrayList()
        result.forEach {
            if (it.fiatAmount != BigDecimal.ZERO) {
                result1.add(it)
            }
        }

        /*if (request.page != null) {
            val pageNo = request.page
            result1 = result1.stream().skip(pageNo * 10L).limit(10).toList() as ArrayList<TransactionSettlement>
        }*/
        return result1
    }
    //
    @GetMapping("/userVerify")
    @Operation(summary = "Get user Details")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User found"),
        ApiResponse(
            responseCode = "400",
            description = "${ErrorCodes.INVALID_EMAIL}, ${ErrorCodes.INVALID_PHONE_NUMBER}, ${ErrorCodes.PARAMETERS_NOT_SPECIFIED}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun userVerify(
        @Email(message = ErrorCodes.INVALID_EMAIL)
        @RequestParam(value = "email", required = false) email: String?,
        principal: Authentication,
    ): UserAccount {
        if (email != principal.name) {
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
        email?.let {
            return userAccountService.findUserAccountByEmail(email)
        }
        throw BadRequestException(ErrorCodes.PARAMETERS_NOT_SPECIFIED)
    }
}

data class SettlementRequestBody(val from: Instant, val to: Instant, val posId: String?)

data class CreateExternalTransactionRequest(
    val uuid: UUID? = null,
    val amount: BigDecimal,
    val asset: CurrencyUnit,
    val receiverAddress: String,
    val description: String? = null,
    val merchantId: Long? = null,
    val posId: String? = null
)

fun checkUUID(str: String): Boolean {
    val patternRegx = Regex("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$")
    return str.matches(patternRegx)
}

fun checkNumber(str: String): Boolean {
    return str.matches("-?\\d+(\\.\\d+)?".toRegex())
}

data class EncryptQRRequest(val data: String)

data class MerchantPaymentRequest(
    val uuid: UUID? = null,
    val amount: BigDecimal,
    val asset: CurrencyUnit,
    val walletUserAddress: String,
    val description: String? = null
)

data class AlgoBalancesRequest(
    val customerId: String? = null,
    /* val customerEmail: String? = null,
     val customerPhoneNumber: String? = null,*/
    val tokenName: String? = null
)

data class AlgoBalancesResponse(
    val customerId: String? = null,
    val customerEmail: String? = null,
    val customerPhoneNumber: String? = null,
    val customerName: String? = null,
    val tokenName: Map<String, BigDecimal>? = null,
    val transactionDate: Instant = Instant.now()
)

enum class SettlementReportDays {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    CUSTOM
}
