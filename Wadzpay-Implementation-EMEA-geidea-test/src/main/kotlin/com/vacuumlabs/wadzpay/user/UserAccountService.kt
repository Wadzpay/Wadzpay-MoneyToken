package com.vacuumlabs.wadzpay.user

import com.vacuumlabs.PASSCODE_PASSPHRASE
import com.vacuumlabs.wadzpay.algocutomtoken.models.WadzpayMinter
import com.vacuumlabs.wadzpay.algocutomtoken.models.WadzpayMinterRepository
import com.vacuumlabs.wadzpay.asset.AssetService
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.configuration.EncryptionKeyConfiguration
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import com.vacuumlabs.wadzpay.issuance.IssuanceCommonController
import com.vacuumlabs.wadzpay.issuance.IssuanceConfigurationService
import com.vacuumlabs.wadzpay.issuance.IssuancePaymentService
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.kyc.models.VerificationStatus
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.merchant.model.CustomerOfflineWrongPasswordEntry
import com.vacuumlabs.wadzpay.merchant.model.CustomerOfflineWrongPasswordEntryRepository
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.EncryptionService
import com.vacuumlabs.wadzpay.services.S3Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.collections.ArrayList

@Service
class UserAccountService(
    val userAccountRepository: UserAccountRepository,
    val ledgerService: LedgerService,
    val cognitoService: CognitoService,
    val encryptionService: EncryptionService,
    val s3Service: S3Service,
    val encryptionKeyConfiguration: EncryptionKeyConfiguration,
    val userPasscodeRepository: UserPasscodeRepository,
    val emailSMSSenderService: EmailSMSSenderService,
    val issuanceConfigurationService: IssuanceConfigurationService,
    val customerOfflineWrongPasswordEntryRepository: CustomerOfflineWrongPasswordEntryRepository,
    val wadzpayMinterRepository: WadzpayMinterRepository,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val issuancePaymentService: IssuancePaymentService,
    @org.springframework.context.annotation.Lazy
    @Autowired
    val assetService: AssetService

) {

    @Transactional
    fun createUserAccount(cognitoUsername: String, email: String, phoneNumber: String): UserAccount {
        val userAccount = UserAccount(cognitoUsername, email, phoneNumber)
        userAccountRepository.save(userAccount)
        userAccount.accounts = ledgerService.createAccounts(userAccount)
        return userAccountRepository.save(userAccount)
    }

    fun getUserAccountByEmail(email: String): UserAccount {
        return userAccountRepository.getByEmail(email) ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }
    fun findByEmail(email: String): UserAccount? {
        return userAccountRepository.findByEmail(email)
    }

    fun findByBearerToken(token: String): UserAccount? {
        return userAccountRepository.findByBearerToken(token)
    }
/*This function is used for retrieve data by the customerId */
    fun getUserAccountByCustomerId(customerId: String, institutionId: String?): UserAccount {
        if (institutionId != null) {
            return issuanceConfigurationService.getUserAccountByIssuanceBank(customerId, institutionId)
        } else {
            val userAccountList = userAccountRepository.getByCustomerIdIgnoreCase(customerId)
            if (!userAccountList.isNullOrEmpty()) {
                return userAccountRepository.getByCustomerIdIgnoreCase(customerId)!![0]
            }
            throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
        }
    }

    fun getUserAccountByCustomerIdWithNull(customerId: String, institutionId: String?): UserAccount? {
        if (institutionId != null) {
            return issuanceConfigurationService.getUserAccountByIssuanceBankWithNull(customerId, institutionId)
        } else {
            val userAccountList = userAccountRepository.getByCustomerIdIgnoreCase(customerId)
            if (!userAccountList.isNullOrEmpty()) {
                return userAccountRepository.getByCustomerIdIgnoreCase(customerId)!![0]
            }
            return null
        }
    }

    fun findUserAccountByEmail(email: String): UserAccount {
        val toLowerEmail = email.toLowerCase()
        return userAccountRepository.getByEmail(toLowerEmail) ?: throw UserNotFoundException.create(
            ErrorCodes.USER_NOT_FOUND,
            Throwable()
        )
    }

    fun getUserAccount(userAccountId: Long): UserAccount {
        return userAccountRepository.findById(userAccountId)
            .orElseThrow { EntityNotFoundException(ErrorCodes.USER_NOT_FOUND) }
    }

    fun getUserAccountByCognitoUsername(cognitoUsername: String): UserAccount {
        return userAccountRepository.getByCognitoUsername(cognitoUsername)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }
    fun saveUserAccount(userAccount: UserAccount): UserAccount {
        return userAccountRepository.save(userAccount)
    }
    /*fun saveUserAccount(userAccount: UserAccount):UserAccount{
        return userAccountRepository.save(userAccount)
    }*/
    fun getUserAccountByPhoneNumber(phoneNumber: String): UserAccount {
        return userAccountRepository.getByPhoneNumber(phoneNumber)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun getUserAccountByFirstName(firstName: String): UserAccount {
        return userAccountRepository.getByFirstName(firstName)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun getUserAccountByLastName(lastName: String): UserAccount {
        return userAccountRepository.getByLastName(lastName)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun getUserAccountByMerchantId(merchantId: Long): ArrayList<DashboardMerchantList> {
        val merchnats = userAccountRepository.getByMerchantId(merchantId)
            ?: throw EntityNotFoundException(ErrorCodes.ACCOUNT_NOT_FOUND)
        val dashboardMerchantList = ArrayList<DashboardMerchantList>()
        for (accounts in merchnats) {
            val active = cognitoService.getUserState(accounts.email!!)
            dashboardMerchantList.add(DashboardMerchantList(accounts, active))
        }
        return dashboardMerchantList
    }

    data class DashboardMerchantList(val userAccount: UserAccount, val isActive: Boolean)

    fun isEmailAvailable(email: String): Boolean {
        return userAccountRepository.getByEmail(email) == null
    }

    fun isPhoneAvailable(phone: String): Boolean {
        return userAccountRepository.getByPhoneNumber(phone) == null
    }

    fun deleteUser(userAccount: UserAccount) {
        userAccountRepository.delete(userAccount)
    }

    fun extractUserAccount(request: CreatePeerToPeerTransactionRequest, senderAccount: UserAccount): UserAccount? {
        return if (request.receiverUsername != null) {
            userAccountRepository.getByCognitoUsername(request.receiverUsername)
        } else if (request.receiverEmail != null) {
            if (issuancePaymentService.checkEmail(request.receiverEmail)) {
                userAccountRepository.getByEmail(request.receiverEmail)
            } else {
                val issuanceBanksUserEntry = issuancePaymentService.issuanceWalletService.getIssuanceBankMapping(senderAccount)
                getUserAccountByCustomerId(
                    request.receiverEmail,
                    issuanceBanksUserEntry?.issuanceBanksId?.institutionId
                )
            }
        } else if (request.receiverPhone != null) {
            userAccountRepository.getByPhoneNumber(request.receiverPhone)
        } else {
            null
        }
    }

    fun updateUserAccount(userAccount: UserAccount, notificationStatus: Boolean): UpdateUserNotificationStatusResponse {
        userAccount.notificationStatus = notificationStatus
        val userAccountData = userAccountRepository.save(userAccount)
        return UpdateUserNotificationStatusResponse(notificationStatus = userAccountData.notificationStatus)
    }
    fun updateUserAccountToken(userAccount: UserAccount, notificationStatus: Boolean): UpdateUserNotificationStatusResponse {
        userAccount.notificationStatus = notificationStatus
        val userAccountData = userAccountRepository.save(userAccount)
        return UpdateUserNotificationStatusResponse(notificationStatus = userAccountData.notificationStatus)
    }
    fun getUserAccountByEmailLike(email: String): ArrayList<UserAccount> {
        return userAccountRepository.findByEmailLike(email) ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun getUserAccountByPhoneNumberLike(phoneNumber: String): ArrayList<UserAccount> {
        return userAccountRepository.findByPhoneLike(phoneNumber)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun getUserAccountByFirstNameLike(firstName: String): ArrayList<UserAccount> {
        return userAccountRepository.findByFirstNameLike(firstName)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun getUserAccountByLastNameLike(lastName: String): ArrayList<UserAccount> {
        return userAccountRepository.findByLastNameLike(lastName)
            ?: throw EntityNotFoundException(ErrorCodes.USER_NOT_FOUND)
    }

    fun findAllUserAccount(): MutableIterable<UserAccount> {
        return userAccountRepository.findAll()
    }

    fun getUserAccountByKycVerified(verificationStatus: VerificationStatus): MutableIterable<UserAccount>? {
        return userAccountRepository.getByKycVerified(verificationStatus)
    }

    fun createPasscode(request: Passcode, userAccount: UserAccount): SavePasscodeResponse {
        if (request.passcodeHash.split("::").size === 3) {
            val decryptPasscode = encryptionService.decrypt(
                request.passcodeHash.split("::")[1],
                request.passcodeHash.split("::")[0],
                PASSCODE_PASSPHRASE,
                request.passcodeHash.split("::")[2]
            )
            val sDecryptPasscode = String(decryptPasscode, StandardCharsets.UTF_8)
            val saltKey = encryptionKeyConfiguration.version1
            val isPasscodeSaved = checkPasscodeSet(userAccount)
            val encryptedHash = encryptionService.generatePwd(sDecryptPasscode, saltKey)
            val savedPasscode = userPasscodeRepository.getByUserAccount(userAccount)
            savedPasscode?.forEach { data ->
                if (data.isActive) {
                    data.isActive = false
                    data.modifiedBy = userAccount
                    data.modifiedDate = Instant.now()
                    userPasscodeRepository.save(data)
                }
            }
            if (encryptedHash != null) {
                val userPasscode =
                    UserPasscode(
                        userAccount = userAccount,
                        passcodeHash = encryptedHash,
                        saltVersion = "version1",
                        isActive = true,
                        createdBy = userAccount,
                        createdDate = Instant.now()
                    )
                userPasscodeRepository.save(userPasscode)
            }
            val savePasscodeResponse = SavePasscodeResponse()
            if (isPasscodeSaved.isPasscodeSet) {
                savePasscodeResponse.message = ErrorCodes.PASSCODE_UPDATED_SUCCESSFULLY
            } else {
                savePasscodeResponse.message = ErrorCodes.PASSCODE_SET_SUCCESSFULLY
            }
            return savePasscodeResponse
        } else {
            throw EntityNotFoundException(ErrorCodes.SALT_KEY_NOT_FOUND)
        }
    }

    fun verifyPasscode(passcode: String, userAccount: UserAccount): Boolean {
        if (passcode.split("::").size === 3) {
            val decryptPasscode = encryptionService.decrypt(
                passcode.split("::")[1],
                passcode.split("::")[0],
                "passcode",
                passcode.split("::")[2]
            )
            val sDecryptPasscode = String(decryptPasscode, StandardCharsets.UTF_8)
            val saltKey = encryptionKeyConfiguration.version1
            if (saltKey != null) {
                var savedPasscodeHash: String? = null
                val requestHash = encryptionService.generatePwd(sDecryptPasscode, saltKey)
                val savedPasscode = userPasscodeRepository.getByUserAccount(userAccount)
                savedPasscode?.forEach { data ->
                    if (data.isActive) {
                        savedPasscodeHash = data.passcodeHash
                    }
                }
                if (savedPasscodeHash != null) {
                    return requestHash.equals(savedPasscodeHash)
                }
                throw EntityNotFoundException(ErrorCodes.PASSCODE_NOT_FOUND)
            } else {
                throw EntityNotFoundException(ErrorCodes.SALT_KEY_NOT_FOUND)
            }
        }
        throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
    }

    fun decryptPasscode(passcode: String, userAccount: UserAccount): SavePasscodeResponse {
        println("passcode ==>" + passcode)
        println("passcode ==>" + passcode.split("::").size)
        if (passcode.split("::").size == 3) {
            val decryptPasscode = encryptionService.decrypt(
                passcode.split("::")[1],
                passcode.split("::")[0],
                "passcode",
                passcode.split("::")[2]
            )
            val sDecryptPasscode = String(decryptPasscode, StandardCharsets.UTF_8)
            return SavePasscodeResponse(
                message = sDecryptPasscode
            )
        }
        throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
    }

    fun verifyPasscodeHash(passcodeHashReq: String, userAccount: UserAccount): Boolean {
        val saltKey = encryptionKeyConfiguration.version1
        var savedPasscodeHash: String? = null
        val savedPasscode = userPasscodeRepository.getByUserAccount(userAccount)
        savedPasscode?.forEach { data ->
            if (data.isActive) {
                savedPasscodeHash = data.passcodeHash
            }
        }
        if (savedPasscodeHash != null) {
            return passcodeHashReq == savedPasscodeHash
        }
        throw EntityNotFoundException(ErrorCodes.PASSCODE_NOT_FOUND)
    }

    fun uploadImage(request: Passcode): ImageResponse {
        s3Service.uploadImage(
            null,
            null
        )
        throw EntityNotFoundException(ErrorCodes.BAD_REQUEST)
    }

    fun checkPasscodeSet(userAccount: UserAccount): ProfileDetailResponse {
        val savedPasscode = userPasscodeRepository.getByUserAccount(userAccount)
        var isPasscodeSet = false
        if (savedPasscode != null && savedPasscode.size > 0) {
            isPasscodeSet = true
        }
        val isActivationFeeDeducted = issuanceConfigurationService.checkActivationFeeAlreadyDeducted(
            IssuanceCommonController.WalletFeeType.WF_001.walletFeeId, userAccount
        )
        val isRunningLow = issuanceConfigurationService.checkWalletBalanceRunningLow(IssuanceCommonController.WalletFeeType.WF_005.walletFeeId, userAccount)
        return ProfileDetailResponse(
            isPasscodeSet = isPasscodeSet,
            isActivationFeeCharged = isActivationFeeDeducted,
            isBalanceRunningLow = isRunningLow,
            lowMaintainWalletBalance = issuanceConfigurationService.getLowMaintainWalletBalance(
                userAccount
            ),
            isP2PEnabled = issuanceConfigurationService.getP2PEnableDisableDetails(userAccount),
            fiatCurrency = issuanceConfigurationService.getIssuerInstitutionSupportedFiatCurrency(userAccount)
        )
    }

    fun encryptPasscode(passCode: String): String {
        val saltKey = encryptionKeyConfiguration.version1
        val encryptedHash = encryptionService.generatePwd(passCode, saltKey)
        return encryptedHash.toString()
        throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
    }

    fun verifyPasscodeMerchant(
        passcode: String,
        userAccount: UserAccount,
        emailId: String,
        phoneNumber: String,
        firstName: String,
        uuid: UUID?
    ): Boolean {
        if (passcode.split("::").size === 3) {
            /*val decryptPasscode = encryptionService.decrypt(
                passcode.split("::")[1],
                passcode.split("::")[0],
                "passcode",
                passcode.split("::")[2]
            )*/
            val decryptPasscode = encryptionService.decryptMerchant(
                passcode.split("::")[1],
                passcode.split("::")[0],
                "passcode",
                passcode.split("::")[2],
                emailId,
                phoneNumber,
                firstName,
                uuid
            )
            val sDecryptPasscode = String(decryptPasscode, StandardCharsets.UTF_8)
            val saltKey = encryptionKeyConfiguration.version1
            var savedPasscodeHash: String? = null
            val requestHash = encryptionService.generatePwd(sDecryptPasscode, saltKey)
            val savedPasscode = userPasscodeRepository.getByUserAccount(userAccount)
            savedPasscode?.forEach { data ->
                if (data.isActive) {
                    savedPasscodeHash = data.passcodeHash
                }
            }
            if (savedPasscodeHash != null) {
                return requestHash.equals(savedPasscodeHash)
            }
            throw EntityNotFoundException(ErrorCodes.PASSCODE_NOT_FOUND)
        }
        sendInvalidPasscodeAlert(
            emailId,
            phoneNumber,
            firstName,
            uuid,
            Instant.now()
        )
        throw EntityNotFoundException(ErrorCodes.INVALID_ENCRYPTED_PASSCODE)
    }

    fun sendInvalidPasscodeAlert(
        emailId: String,
        mobileNumber: String,
        name: String,
        uuid: UUID?,
        alertDate: Instant
    ) {
        sendEmailAlert(emailId, mobileNumber, name, uuid, alertDate)
        sendSMSAlert(emailId, mobileNumber, name, uuid, alertDate)
    }

    fun sendEmailAlert(
        emailId: String,
        mobileNumber: String,
        name: String,
        uuid: UUID?,
        alertDate: Instant
    ) {
        emailSMSSenderService.sendEmail(
            "Customer Offline Invalid Passcode Alert",
            emailSMSSenderService.userOfflineAlertEmailBody(
                name,
                mobileNumber,
                uuid,
                alertDate
            ),
            emailId,
            "contact@wadzpay.com"
        )
    }

    fun sendSMSAlert(
        emailId: String,
        mobileNumber: String,
        name: String,
        uuid: UUID?,
        alertDate: Instant
    ) {
        val strSmsText: String = "Dear Customer" + ",\n" +
            "We regret to inform you that your Passcode Invalid Attempt with below details \n" +
            "UUID - " + uuid + ",\n" +
            "Date: " + formattingDate(alertDate)

        emailSMSSenderService.sendMobileSMS(
            mobileNumber,
            strSmsText
        )
    }
    fun formattingDate(date: Instant): String? {
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(date)
    }

    fun getPasscodeTitle(userAccount: UserAccount): WalletTitleResponse {
        var createPasscode = TitleSubTitleResponse(
            title = "Create Passcode",
            subtitle = "This will secure your payment verification with new 6-digit passcode."
        )
        var confirmPasscode = TitleSubTitleResponse(
            title = "Confirm Passcode",
            subtitle = "This will secure your payment verification with new 6-digit passcode."
        )
        var passcodeScreenTextResponse = PasscodeScreenTextResponse(
            createPasscode = createPasscode,
            confirmPasscode = confirmPasscode
        )
        return WalletTitleResponse(
            passcodeScreen = passcodeScreenTextResponse
        )
    }

    fun saveWrongPasswordEntry(customerOfflineWrongPasswordEntry: CustomerOfflineWrongPasswordEntry) {
        customerOfflineWrongPasswordEntryRepository.save(customerOfflineWrongPasswordEntry)
    }

    /* TODO : wallet address creation of issuer bank */
    @Transactional
    fun createUserAccountIssuer(
        cognitoUsername: String,
        email: String,
        phoneNumber: String,
        issuanceBank: IssuanceBanks
    ): UserAccount {
        val userAccount = UserAccount(cognitoUsername, email, phoneNumber, issuanceBanks = issuanceBank)
        userAccount.customerType = CustomerType.ISSUER.toString()
        userAccount.createdDate = Instant.now()
        userAccountRepository.save(userAccount)
        userAccount.accounts = ledgerService.createAccounts(userAccount)
        return userAccountRepository.save(userAccount)
    }

    fun saveWadzpayMinter(request: WadzpayMinterRequest): WadzpayMinter {
        val wadzpayMinter = WadzpayMinter(
            assetName = request.assetName.toString(),
            assetMintAmount = request.assetMintAmount,
            assetMintBaseUnit = request.assetMintBaseUnit,
            assetUrl = request.assetUrl,
            wadzpayMinterAddress = request.wadzpayMinterAddress
        )

        return wadzpayMinterRepository.save(wadzpayMinter)
    }

    @Transactional
    fun createUserAccountForPrivateBC(
        cognitoUsername: String,
        email: String?,
        phoneNumber: String?,
        customerId: String?,
        userAccountIssuer: UserAccount?
    ): UserAccount {
        val userAccount = UserAccount(cognitoUsername = cognitoUsername, email = email, phoneNumber = phoneNumber, customerId = customerId)
        userAccountRepository.save(userAccount)
        if (userAccountIssuer != null) {
            val assets = assetService.getAssetForInstitutionOfUser(userAccountIssuer, null)
            if (assets != null) {
                userAccount.accounts = ledgerService.createAccountsForPrivateBC(userAccount, assets)
            }
        } else {
            userAccount.accounts = ledgerService.createAccounts(userAccount)
        }
        return userAccountRepository.save(userAccount)
    }

    fun verifyEmailOnAWS(email: String): Boolean {
        cognitoService.verifyEmail(email)
        return true
    }
}
