package com.vacuumlabs.wadzpay.ledger.service

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvBindByPosition
import com.opencsv.bean.CsvDate
import com.vacuumlabs.wadzpay.accountowner.BDORegisterRequest
import com.vacuumlabs.wadzpay.accountowner.BDOWithdrawByCardRequest
import com.vacuumlabs.wadzpay.accountowner.BDOWithdrawByWalletRequest
import com.vacuumlabs.wadzpay.accountowner.BDOWithdrawRequest
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.BDOWithdrawal
import com.vacuumlabs.wadzpay.ledger.model.BDOWithdrawalRepository
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.user.UserAccountService
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class BDOWithdrawalService(
    val userAccountService: UserAccountService,
    val bdoWithdrawalRepository: BDOWithdrawalRepository,
) {

    fun bdoWithdrawByCardTransaction(
        request: BDOWithdrawByCardRequest,
        accountOwner: AccountOwner,
        principal: Authentication,
    ): BDOWithdrawal? {
        var bdoWithdrawal: BDOWithdrawal? = null
        if (request.cardNumber != null && request.cardNumber.isNotEmpty()) {
            bdoWithdrawal = bdoWithdrawalRepository.findByCardNumber(request.cardNumber)
        }

        if (bdoWithdrawal != null) {
            return sendExternalWallet(
                BDOWithdrawRequest(
                    cardNumber = request.cardNumber,
                    walletAddress = bdoWithdrawal.walletAddress,
                    amount = request.amount,
                    asset = bdoWithdrawal.asset,
                    created_at = Instant.now()
                ),
                bdoWithdrawal
            )
        } else {
            throw BadRequestException(ErrorCodes.WALLET_ADDRESS_NOT_EXIST)
        }
    }

    fun bdoWithdrawByWalletTransaction(
        request: BDOWithdrawByWalletRequest,
        accountOwner: AccountOwner,
        principal: Authentication,
    ): BDOWithdrawal? {
        var bdoWithdrawal: BDOWithdrawal? = null
        if (request.walletAddress != null && request.walletAddress.isNotEmpty()) {
            bdoWithdrawal = bdoWithdrawalRepository.findByWalletAddress(request.walletAddress)
        }

        if (bdoWithdrawal != null) {
            return sendExternalWallet(
                BDOWithdrawRequest(
                    cardNumber = bdoWithdrawal.cardNumber,
                    walletAddress = request.walletAddress,
                    amount = request.amount,
                    asset = bdoWithdrawal.asset,
                    created_at = Instant.now()
                ),
                bdoWithdrawal
            )
        } else {
            throw BadRequestException(ErrorCodes.WALLET_ADDRESS_NOT_EXIST)
        }
    }

    private fun sendExternalWallet(request: BDOWithdrawRequest, bdoWithdrawal: BDOWithdrawal): BDOWithdrawal {
        if (request.amount <= BigDecimal.ZERO) {
            throw BadRequestException(ErrorCodes.INVALID_AMOUNT_NEGATIVE_OR_ZERO)
        } else {
            bdoWithdrawal.amount = request.amount
            if (bdoWithdrawal.totalAmount >= request.amount) {
                bdoWithdrawal.totalAmount = bdoWithdrawal.totalAmount - request.amount
                bdoWithdrawal.status = TransactionStatus.SUCCESSFUL

                return bdoWithdrawalRepository.save(bdoWithdrawal)
            } else {
                throw BadRequestException(ErrorCodes.INSUFFICIENT_FUNDS)
            }
        }
    }

    fun bdoRegisterNewWallet(request: BDORegisterRequest, accountOwner: AccountOwner, principal: Authentication): BDOWithdrawalViewModel? {
        if (request.cardNumber != null && request.walletAddress != null) {
            var bdoWithdrawal = BDOWithdrawal(
                cardNumber = request.cardNumber,
                walletAddress = request.walletAddress,
                totalAmount = request.totalAmount,
                amount = BigDecimal(0),
                asset = request.asset,
                status = TransactionStatus.NEW,
                createdAt = request.createdAt
            )
            bdoWithdrawal = bdoWithdrawalRepository.save(bdoWithdrawal)

            return BDOWithdrawalViewModel(
                cardNumber = bdoWithdrawal.cardNumber,
                walletAddress = bdoWithdrawal.walletAddress,
                totalAmount = bdoWithdrawal.totalAmount,
                amount = bdoWithdrawal.amount,
                asset = bdoWithdrawal.asset,
                status = bdoWithdrawal.status,
                createdAt = bdoWithdrawal.createdAt
            )
        } else {
            throw BadRequestException(ErrorCodes.WALLET_ADDRESS_NOT_EXIST)
        }
    }
}

data class BDOWithdrawalViewModel(
    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "card_number")
    val cardNumber: String,

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "wallet_address ")
    val walletAddress: String?,

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "total_amount")
    val totalAmount: BigDecimal?,

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "amount")
    val amount: BigDecimal?,

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "asset")
    val asset: CurrencyUnit,

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "status")
    val status: TransactionStatus,

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Created_at")
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    val createdAt: Instant,
)
