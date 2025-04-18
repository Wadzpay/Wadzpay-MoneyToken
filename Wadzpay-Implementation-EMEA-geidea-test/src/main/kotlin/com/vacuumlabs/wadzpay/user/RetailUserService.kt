package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.COMMON_PASSWORD
import com.vacuumlabs.EMAIL_FORMATE
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.issuance.IssuancePaymentService
import com.vacuumlabs.wadzpay.issuance.IssuanceWalletService
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.kyc.models.VerificationStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RetailUserService(
    val issuanceWalletService: IssuanceWalletService,
    val userAccountRepository: UserAccountRepository,
    val cognitoService: CognitoService,
    val userAccountService: UserAccountService,
    val redisService: RedisService,
    val issuancePaymentService: IssuancePaymentService,
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository
) {
    fun retailUserRegistration(customerId: String, userAccount: UserAccount): RetailUserController.RegistrationResponse {
        val registrationResponse = walletUserRegistration(customerId, userAccount)
        val issuanceBanksUserEntry = issuanceWalletService.getIssuanceBankMapping(registrationResponse)
        return RetailUserController.RegistrationResponse(
            createdDate = registrationResponse.createdDate,
            status = TransactionStatus.SUCCESSFUL.toString(),
            walletId = issuanceBanksUserEntry?.walletId
        )
    }

    fun walletUserRegistration(
        customerId: String,
        userAccountIssuer: UserAccount
    ): UserAccount {
        var userAccount = walletUserRegistration(null, null, customerId, userAccountIssuer)
        userAccount.kycVerified = VerificationStatus.APPROVED_VERIFIED
        userAccount = userAccountRepository.save(userAccount)
        issuanceWalletService.issuanceWalletRegistration(
            userAccount,
            userAccountIssuer.issuanceBanks!!,
            null
        )
        return userAccount
    }

    fun walletUserRegistration(email: String?, phoneNumber: String?, customerId: String?, userAccountIssuer: UserAccount): UserAccount {
        var customerId = customerId
        if (!customerId.isNullOrEmpty()) {
            if (issuancePaymentService.checkEmail(customerId)) {
                val userAccountByEmail = userAccountRepository.getByEmailIgnoreCase(customerId)
                if (userAccountByEmail != null) {
                    throw DuplicateEntityException(ErrorCodes.CUSTOMER_ID_ALREADY_EXISTS)
                }
            } else {
                val userAccountByCustomerID = userAccountService.getUserAccountByCustomerIdWithNull(
                    customerId,
                    userAccountIssuer.issuanceBanks?.institutionId
                )
                if (userAccountByCustomerID != null) {
                    val issuanceBanksUserEntry =
                        issuanceBanksUserEntryRepository.getByUserAccountId(userAccountByCustomerID)
                    if (issuanceBanksUserEntry != null) {
                        println(issuanceBanksUserEntry.issuanceBanksId)
                    }
                    println(userAccountIssuer.issuanceBanks)
                    if (issuanceBanksUserEntry != null && issuanceBanksUserEntry.issuanceBanksId == userAccountIssuer.issuanceBanks) {
                        throw DuplicateEntityException(ErrorCodes.CUSTOMER_ID_ALREADY_EXISTS)
                    }
                }
                customerId = customerId.toUpperCase()
            }
        }
        if (!phoneNumber.isNullOrEmpty()) {
            val userAccountByPhone = userAccountRepository.getByPhoneNumber(phoneNumber)
            if (userAccountByPhone != null) {
                throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
            }
        }
        if (!email.isNullOrEmpty()) {
            val userAccountByEmail = userAccountRepository.getByEmail(email)
            if (userAccountByEmail != null) {
                throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
            }
        }
        var email = email
        println("email 1==> " + email)
        if (email.isNullOrEmpty() && !issuancePaymentService.checkEmail(customerId!!)) {
            val institutionId = userAccountIssuer.issuanceBanks?.institutionId
            email = "$customerId@$institutionId$EMAIL_FORMATE"
            println("email 2==> " + email)
        } else {
            email = customerId
        }
        email = email?.toLowerCase()
        println("email 3==> " + email)
        try {
            val cognitoUsername = cognitoService.register(email!!, phoneNumber, COMMON_PASSWORD, true)
            redisService.deleteRegistrationEntity(email, phoneNumber)
            val userAccount =
                userAccountService.createUserAccountForPrivateBC(cognitoUsername, email, phoneNumber, customerId, null)
            userAccount.customerType = CustomerType.WALLET_USER.toString()
            userAccount.createdDate = Instant.now()
            userAccountService.userAccountRepository.save(userAccount)
            return userAccount
        } catch (e: Exception) {
            throw EntityNotFoundException(ErrorCodes.WRONG_INPUT)
        }
    }
}
