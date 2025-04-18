package com.vacuumlabs.wadzpay.issuance

import com.vacuumlabs.COMMON_PASSWORD
import com.vacuumlabs.ROUNDING_LIMIT
import com.vacuumlabs.vuba.ledger.service.LedgerService
import com.vacuumlabs.wadzpay.auth.Role
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.emailSms.service.EmailSMSSenderService
import com.vacuumlabs.wadzpay.issuance.models.ConversionRateAdjustmentViewModel
import com.vacuumlabs.wadzpay.issuance.models.ConversionRateViewModel
import com.vacuumlabs.wadzpay.issuance.models.InstitutionManagementFieldConfig
import com.vacuumlabs.wadzpay.issuance.models.InstitutionManagementFieldConfigRepository
import com.vacuumlabs.wadzpay.issuance.models.InstitutionRoleMasterRepository
import com.vacuumlabs.wadzpay.issuance.models.InstitutionUserManagement
import com.vacuumlabs.wadzpay.issuance.models.InstitutionUserManagementRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksSubBankEntry
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksSubBankEntryRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceConversionRate
import com.vacuumlabs.wadzpay.issuance.models.IssuanceConversionRateAdjustment
import com.vacuumlabs.wadzpay.issuance.models.IssuanceConversionRateAdjustmentRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceConversionRateRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceTransactionLimitConfig
import com.vacuumlabs.wadzpay.issuance.models.IssuanceTransactionLimitConfigRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceTransactionType
import com.vacuumlabs.wadzpay.issuance.models.IssuanceTransactionTypeRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfig
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfigRepository
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletConfigViewModel
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletFeeType
import com.vacuumlabs.wadzpay.issuance.models.IssuanceWalletFeeTypeRepository
import com.vacuumlabs.wadzpay.issuance.models.TransactionLimitConfigViewModel
import com.vacuumlabs.wadzpay.issuance.models.toViewModel
import com.vacuumlabs.wadzpay.kyc.models.VerificationStatus
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.ledger.model.TransactionMode
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.ledger.service.GetTransactionListRequest
import com.vacuumlabs.wadzpay.ledger.service.TransactionService
import com.vacuumlabs.wadzpay.merchant.Invitation
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.services.CognitoService
import com.vacuumlabs.wadzpay.services.RedisService
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.user.UserAccountService
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Field
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.streams.toList

@Service
class IssuanceService(
    val cognitoService: CognitoService,
    val redisService: RedisService,
    val appConfig: AppConfig,
    val issuanceBanksRepository: IssuanceBanksRepository,
    val issuanceBanksSubBankEntryRepository: IssuanceBanksSubBankEntryRepository,
    val emailSMSSenderService: EmailSMSSenderService,
    val issuanceConversionRateRepository: IssuanceConversionRateRepository,
    val issuanceConversionRateAdjustmentRepository: IssuanceConversionRateAdjustmentRepository,
    val issuanceTransactionTypeRepository: IssuanceTransactionTypeRepository,
    val issuanceWalletFeeTypeRepository: IssuanceWalletFeeTypeRepository,
    val issuanceWalletConfigRepository: IssuanceWalletConfigRepository,
    val issuanceTransactionLimitConfigRepository: IssuanceTransactionLimitConfigRepository,
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository,
    val issuanceGraphService: IssuanceGraphService,
    @Lazy val issuanceWalletService: IssuanceWalletService,
    val institutionRoleMasterRepository: InstitutionRoleMasterRepository,
    val institutionUserManagementRepository: InstitutionUserManagementRepository,
    val institutionManagementFieldConfigRepository: InstitutionManagementFieldConfigRepository,
    val userAccountService: UserAccountService,
    val ledgerService: LedgerService,
    val issuanceConfigurationService: IssuanceConfigurationService,
    val transactionService: TransactionService
) {
    fun createIssuanceBank(createIssuanceBank: IssuanceCommonController.CreateIssuanceBank): IssuanceBanks? {

        // check if the phone number is present and verified
        if (redisService.getPhoneVerified(createIssuanceBank.phoneNumber) == null ||
            redisService.getPhoneVerified(createIssuanceBank.phoneNumber).equals(RedisService.REQUESTED)
        ) {
            //  throw BadRequestException(ErrorCodes.UNVERIFIED_PHONE_NUMBER)
        }

        // check if email is present
        if (redisService.getEmailVerifiedForPhone(createIssuanceBank.email, createIssuanceBank.phoneNumber) == null) {
            // throw EntityNotFoundException(ErrorCodes.EMAIL_DOES_NOT_EXISTS)
        }

        // check if there is no other account with this email
        if (!cognitoService.isEmailAvailable(createIssuanceBank.email)) {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }

        val emailVerifiedStatus =
            redisService.getEmailVerifiedForPhone(createIssuanceBank.email, createIssuanceBank.phoneNumber)

        if (emailVerifiedStatus != RedisService.VERIFIED) {
            if (appConfig.production) {
                // twilioService.verifyOTPCode(email, code)
            }

            // redisService.setEmailForPhoneVerified(email, phoneNumber)
        }
        val savedIssuanceBanks = issuanceBanksRepository.getAllBy()
        if (!savedIssuanceBanks.isNullOrEmpty()) {
            throw BadRequestException(ErrorCodes.USERS_ALREADY_IN_DATABASE)
        }
        val cognitoUsername = cognitoService.register(
            createIssuanceBank.email,
            createIssuanceBank.phoneNumber,
            createIssuanceBank.password,
            false
        )
        var issuanceBanks = IssuanceBanks(
            bankName = createIssuanceBank.institutionName,
            countryCode = createIssuanceBank.countryCode.toString(),
            timeZone = createIssuanceBank.timeZone,
            defaultCurrency = if (createIssuanceBank.defaultCurrency != null) createIssuanceBank.defaultCurrency.toString() else null,
            phoneNumber = createIssuanceBank.phoneNumber,
            email = createIssuanceBank.email,
            cognitoUsername = cognitoUsername,
            bankLogo = createIssuanceBank.institutionLogo,
            destinationFiatCurrency = createIssuanceBank.destinationFiatCurrency,
            isActive = true
        )
        issuanceBanks = issuanceBanksRepository.save(issuanceBanks)
        getInvitation(createIssuanceBank.email)?.let {
            cognitoService.addToGroupIssuance(issuanceBanks, it.role)
        }
        val issuanceBanksSubBankEntry = IssuanceBanksSubBankEntry(
            issuanceBanksId = issuanceBanks,
            parentBankId = issuanceBanks,
            isAccessible = true,
            isActive = true
        )

        issuanceBanksSubBankEntryRepository.save(issuanceBanksSubBankEntry)
        userAccountService.createUserAccountIssuer(cognitoUsername, createIssuanceBank.email, createIssuanceBank.phoneNumber, issuanceBanks)
        return issuanceBanks
    }

    fun getInvitation(email: String): Invitation? {
        val invitation = redisService.getInvitation(email) ?: return null
        val issuanceBankId = invitation.substringBeforeLast(',').toLong()
        val issuanceBank = issuanceBanksRepository.findById(issuanceBankId)
            .orElseThrow { EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND) }
        val role = Role.valueOf(invitation.substringAfterLast(','))

        return issuanceBank.bankName?.let {
            Invitation(
                it,
                role
            )
        }
    }

    fun getIssuanceBankAccountByEmail(email: String): IssuanceBanks {
        val toLowerEmail = email.toLowerCase()
        return issuanceBanksRepository.getByEmail(toLowerEmail)
            ?: throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
    }

    @Transactional
    fun institutionDetailsRegistration(
        institutionRegisterData: IssuanceCommonController.InstitutionRegisterData,
        issuanceBank: IssuanceBanks
    ): IssuanceBanks? {
        var issuanceBankSaveData = IssuanceBanks(
            isActive = true
        )
        if (institutionRegisterData.id != null && institutionRegisterData.id!! > 0) {
            issuanceBankSaveData = issuanceBanksRepository.getById(institutionRegisterData.id!!)!!
        }
        /* ------------------ Save Primary Business Details ------------------*/
        issuanceBankSaveData.institutionId = if (institutionRegisterData.institutionId?.value != null) institutionRegisterData.institutionId?.value.toString() else issuanceBankSaveData.institutionId
        issuanceBankSaveData.bankName = if (institutionRegisterData.institutionName?.value != null) institutionRegisterData.institutionName?.value.toString() else issuanceBankSaveData.bankName
        issuanceBankSaveData.institutionAbbreviation = if (institutionRegisterData.institutionAbbreviation?.value != null) institutionRegisterData.institutionAbbreviation?.value.toString() else issuanceBankSaveData.institutionAbbreviation
        issuanceBankSaveData.institutionDescription = if (institutionRegisterData.institutionDescription?.value != null) institutionRegisterData.institutionDescription?.value.toString() else issuanceBankSaveData.institutionDescription
        issuanceBankSaveData.bankLogo = if (institutionRegisterData.institutionLogo?.value != null) institutionRegisterData.institutionLogo?.value.toString() else issuanceBankSaveData.bankLogo
        issuanceBankSaveData.institutionRegion = if (institutionRegisterData.institutionRegion?.value != null) institutionRegisterData.institutionRegion?.value.toString() else issuanceBankSaveData.institutionRegion
        issuanceBankSaveData.timeZone = if (institutionRegisterData.institutionTimeZone?.value != null) institutionRegisterData.institutionTimeZone?.value.toString() else issuanceBankSaveData.timeZone
        issuanceBankSaveData.defaultCurrency = if (institutionRegisterData.defaultCurrency?.value != null) institutionRegisterData.defaultCurrency?.value.toString() else issuanceBankSaveData.defaultCurrency
        issuanceBankSaveData.destinationFiatCurrency = if (institutionRegisterData.destinationFiatCurrency?.value != null) FiatCurrencyUnit.valueOf(institutionRegisterData.destinationFiatCurrency?.value.toString()) else issuanceBankSaveData.destinationFiatCurrency
        issuanceBank.activationDate = if (institutionRegisterData.activationDate?.value != null) convertDate(institutionRegisterData.activationDate?.value.toString()) else null
        issuanceBankSaveData = issuanceBanksRepository.save(issuanceBankSaveData)
        /* ------------------ Save Institution Address Details ------------------*/

        issuanceBankSaveData.addressLine1 = if (institutionRegisterData.addressLine1?.value != null) institutionRegisterData.addressLine1?.value.toString() else issuanceBankSaveData.addressLine1
        issuanceBankSaveData.addressLine2 = if (institutionRegisterData.addressLine2?.value != null) institutionRegisterData.addressLine2?.value.toString() else issuanceBankSaveData.addressLine2
        issuanceBankSaveData.addressLine3 = if (institutionRegisterData.addressLine3?.value != null) institutionRegisterData.addressLine3?.value.toString() else issuanceBankSaveData.addressLine3
        issuanceBankSaveData.city = if (institutionRegisterData.city?.value != null) institutionRegisterData.city?.value.toString() else issuanceBankSaveData.city
        issuanceBankSaveData.provice = if (institutionRegisterData.provice?.value != null) institutionRegisterData.provice?.value.toString() else issuanceBankSaveData.provice
        issuanceBankSaveData.country = if (institutionRegisterData.country?.value != null) institutionRegisterData.country?.value.toString() else issuanceBankSaveData.country
        issuanceBankSaveData.postalCode = if (institutionRegisterData.postalCode?.value != null) institutionRegisterData.postalCode?.value.toString() else issuanceBankSaveData.postalCode

        issuanceBankSaveData.companyType = if (institutionRegisterData.companyType?.value != null) institutionRegisterData.companyType?.value.toString() else issuanceBankSaveData.companyType
        issuanceBankSaveData.industryType = if (institutionRegisterData.industryType?.value != null) institutionRegisterData.industryType?.value.toString() else issuanceBankSaveData.industryType

        issuanceBankSaveData = issuanceBanksRepository.save(issuanceBankSaveData)
        /* ------------------ Save Contact Person Details ------------------*/

        issuanceBankSaveData.primaryContactFirstName = if (institutionRegisterData.primaryContactFirstName?.value != null) institutionRegisterData.primaryContactFirstName?.value.toString() else issuanceBankSaveData.primaryContactFirstName
        issuanceBankSaveData.primaryContactMiddleName = if (institutionRegisterData.primaryContactMiddleName?.value != null) institutionRegisterData.primaryContactMiddleName?.value.toString() else issuanceBankSaveData.primaryContactMiddleName
        issuanceBankSaveData.primaryContactLastName = if (institutionRegisterData.primaryContactLastName?.value != null)institutionRegisterData.primaryContactLastName?.value.toString() else issuanceBankSaveData.primaryContactLastName
        issuanceBankSaveData.primaryContactEmailId = if (institutionRegisterData.primaryContactEmailId?.value != null) institutionRegisterData.primaryContactEmailId?.value.toString() else issuanceBankSaveData.primaryContactEmailId
        issuanceBankSaveData.primaryContactPhoneNumber = if (institutionRegisterData.primaryContactPhoneNumber?.value != null) institutionRegisterData.primaryContactPhoneNumber?.value.toString() else issuanceBankSaveData.primaryContactPhoneNumber
        issuanceBankSaveData.primaryContactDesignation = if (institutionRegisterData.primaryContactDesignation?.value != null) institutionRegisterData.primaryContactDesignation?.value.toString() else issuanceBankSaveData.primaryContactDesignation
        issuanceBankSaveData.primaryContactDepartment = if (institutionRegisterData.primaryContactDepartment?.value != null) institutionRegisterData.primaryContactDepartment?.value.toString() else issuanceBankSaveData.primaryContactDepartment

        issuanceBankSaveData = issuanceBanksRepository.save(issuanceBankSaveData)
        /* ------------------ Save Other Details ------------------*/

        issuanceBankSaveData.customerOfflineTransaction = if (institutionRegisterData.customerOfflineTransaction?.value != null) institutionRegisterData.customerOfflineTransaction?.value.toString().toBoolean() else issuanceBankSaveData.customerOfflineTransaction
        issuanceBankSaveData.merchantOfflineTransaction = if (institutionRegisterData.merchantOfflineTransaction?.value != null) institutionRegisterData.merchantOfflineTransaction?.value.toString().toBoolean() else issuanceBankSaveData.merchantOfflineTransaction
        issuanceBankSaveData.institutionStatus = if (institutionRegisterData.institutionStatus?.value != null)institutionRegisterData.institutionStatus?.value.toString().toBoolean() else issuanceBankSaveData.institutionStatus
        issuanceBankSaveData.approvalWorkFlow = if (institutionRegisterData.approvalWorkFlow?.value != null) institutionRegisterData.approvalWorkFlow?.value.toString().toBoolean() else issuanceBankSaveData.approvalWorkFlow
        issuanceBankSaveData.p2pTransfer = if (institutionRegisterData.p2pTransfer?.value != null)institutionRegisterData.p2pTransfer?.value.toString().toBoolean() else issuanceBankSaveData.p2pTransfer

        /*Save Issuance Details Start*/
        issuanceBankSaveData = issuanceBanksRepository.save(issuanceBankSaveData)

        /*Issuance Mapping Details Start*/
        val savedMappingData = issuanceBanksSubBankEntryRepository.getByIssuanceBanksId(issuanceBankSaveData)
        if (savedMappingData == null) {
            val issuanceBanksSubBankEntry = IssuanceBanksSubBankEntry(
                issuanceBanksId = issuanceBankSaveData,
                parentBankId = issuanceBank,
                isAccessible = true,
                isActive = true
            )
            issuanceBanksSubBankEntryRepository.save(issuanceBanksSubBankEntry)
        }

        /*Issuance Mapping Details End*/

        /*Institution User Management Details Start*/
        if (institutionRegisterData.adminEmailId != null && institutionRegisterData.adminPhoneNumber != null) {
            val roleMaster = institutionRoleMasterRepository.getByRoleName("Admin")
            var institutionUserManagement = institutionUserManagementRepository.getByEmail(institutionRegisterData.adminEmailId!!.value.toString())
            if (institutionUserManagement == null) {
                institutionUserManagement = InstitutionUserManagement(
                    isActive = true,
                    issuanceBanksId = issuanceBankSaveData,
                    roleId = roleMaster!!,
                    cognitoUsername = null,
                    createdBy = issuanceBank
                )
                if (!cognitoService.isEmailAvailable(institutionRegisterData.adminEmailId!!.value.toString())) {
                    throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
                }
                if (!cognitoService.isPhoneAvailable(institutionRegisterData.adminPhoneNumber!!.value.toString())) {
                    throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
                }
            } else {
                institutionUserManagement.modifiedDate = Instant.now()
                institutionUserManagement.modifiedBy = issuanceBank
            }
            institutionUserManagement.firstName = institutionRegisterData.adminFirstName?.value.toString()
            institutionUserManagement.middleName = institutionRegisterData.adminMiddleName?.value.toString()
            institutionUserManagement.lastName = institutionRegisterData.adminLastName?.value.toString()
            institutionUserManagement.email = if (institutionUserManagement.email != null) institutionUserManagement.email else institutionRegisterData.adminEmailId?.value.toString()
            institutionUserManagement.phoneNumber = if (institutionUserManagement.phoneNumber != null) institutionUserManagement.phoneNumber else institutionRegisterData.adminPhoneNumber?.value.toString()
            institutionUserManagement.department = institutionRegisterData.adminDepartment?.value.toString()
            institutionUserManagement = institutionUserManagementRepository.save(institutionUserManagement)
            if (institutionRegisterData.isFinalSave != null && institutionRegisterData.isFinalSave == true) {
                if (institutionUserManagement.id != null) {
                    /* ------------------ Save Admin Person Details ------------------*/

                    issuanceBankSaveData.email = if (institutionUserManagement.email != null) institutionUserManagement.email else institutionRegisterData.adminEmailId?.value.toString()
                    issuanceBankSaveData.phoneNumber = if (institutionUserManagement.phoneNumber != null) institutionUserManagement.phoneNumber else institutionRegisterData.adminPhoneNumber?.value.toString()
                    issuanceBankSaveData.countryCode = if (institutionRegisterData.adminCountryCode?.value != null) institutionRegisterData.adminCountryCode?.value.toString() else issuanceBankSaveData.countryCode

                    if (institutionUserManagement.cognitoUsername == null) {
                        val cognitoUsername = cognitoService.register(
                            institutionRegisterData.adminEmailId!!.value.toString(),
                            institutionRegisterData.adminPhoneNumber!!.value.toString(),
                            COMMON_PASSWORD,
                            false
                        )
                        issuanceBankSaveData.cognitoUsername = cognitoUsername
                        issuanceBanksRepository.save(issuanceBankSaveData)
                        institutionUserManagement.cognitoUsername = cognitoUsername
                        institutionUserManagementRepository.save(institutionUserManagement)
                    }
                }
            }
        }
        /*  ================ ********** Save Institution Management field* **********================ */
        // saveInstitutionManagementFieldConfig(issuanceBank, issuanceBankSaveData, institutionRegisterData)
        return issuanceBankSaveData
    }

    fun saveInstitutionManagementFieldConfig(
        actionIssuanceBank: IssuanceBanks,
        issuanceBank: IssuanceBanks,
        institutionRegisterData: IssuanceCommonController.InstitutionRegisterData
    ): Boolean? {
        var savedData = institutionManagementFieldConfigRepository.getByIssuanceBanksId(issuanceBanksId = issuanceBank)
        val institutionManagementFieldConfig = InstitutionManagementFieldConfig(
            issuanceBanksId = issuanceBank,
            isActive = true,
            createdBy = actionIssuanceBank,
            createdDate = Instant.now()
        )
        val fld: Array<Field> = institutionRegisterData::class.java.declaredFields
        for (i in fld.indices) {
            if (fld[i].name != "id" && fld[i].name != "isFinalSave") {
                saveInsManagementFieldConfig(
                    institutionManagementFieldConfig, addUnderscoreInString(fld[i].name),
                    fld[i].get(i)
                )
            }
        }
        return true
    }

    fun saveInsManagementFieldConfig(
        institutionManagementFieldConfig: InstitutionManagementFieldConfig,
        addUnderscoreInString: String?,
        institutionObject: Any
    ): Boolean? {
        institutionManagementFieldConfig.fieldNameKey = addUnderscoreInString
        institutionManagementFieldConfig.isMandatory =
            institutionObject.toString().toBoolean()
        institutionManagementFieldConfig.isAllowShow =
            institutionObject.toString().toBoolean()
        institutionManagementFieldConfig.isAllowEdit =
            institutionObject.toString().toBoolean()
        // institutionManagementFieldConfigRepository.save(institutionManagementFieldConfig)

        return true
    }

    @Transactional
    fun issuanceSignup(
        createIssuanceBank: IssuanceCommonController.CreateIssuanceBank,
        issuanceBank: IssuanceBanks
    ): IssuanceBanks? {

        // check if there is no other account with this email
        if (!cognitoService.isEmailAvailable(createIssuanceBank.email)) {
            throw DuplicateEntityException(ErrorCodes.EMAIL_ALREADY_EXISTS)
        }

        // check if there is no other account with this email
        if (!cognitoService.isPhoneAvailable(createIssuanceBank.phoneNumber)) {
            throw DuplicateEntityException(ErrorCodes.PHONE_NUMBER_ALREADY_EXISTS)
        }

        val cognitoUsername = cognitoService.register(
            createIssuanceBank.email,
            createIssuanceBank.phoneNumber,
            createIssuanceBank.password,
            true
        )

        var issuanceBanks = IssuanceBanks(
            bankName = createIssuanceBank.institutionName,
            countryCode = createIssuanceBank.countryCode.toString(),
            timeZone = createIssuanceBank.timeZone,
            defaultCurrency = if (createIssuanceBank.defaultCurrency != null) createIssuanceBank.defaultCurrency.toString() else null,
            phoneNumber = createIssuanceBank.phoneNumber,
            email = createIssuanceBank.email,
            cognitoUsername = cognitoUsername,
            bankLogo = createIssuanceBank.institutionLogo,
            isActive = true,
            destinationFiatCurrency = createIssuanceBank.destinationFiatCurrency,
            fiatCurrency = createIssuanceBank.fiatCurrency,
            p2pTransfer = createIssuanceBank.p2pTransfer
        )
        issuanceBanks = issuanceBanksRepository.save(issuanceBanks)
        val institutionId = UUID.randomUUID()
        issuanceBanks.institutionId = institutionId.toString()
        issuanceBanks = issuanceBanksRepository.save(issuanceBanks)
        val issuanceBanksSubBankEntry = IssuanceBanksSubBankEntry(
            issuanceBanksId = issuanceBanks,
            parentBankId = issuanceBank,
            isAccessible = true,
            isActive = true
        )
        issuanceBanksSubBankEntryRepository.save(issuanceBanksSubBankEntry)
        userAccountService.createUserAccountIssuer(cognitoUsername, createIssuanceBank.email, createIssuanceBank.phoneNumber, issuanceBanks)
        return issuanceBanks
    }

    fun fetchPrentBankDetails(
        issuanceBank: IssuanceBanks
    ): MutableList<IssuanceBanksSubBankEntry>? {
        return issuanceBanksSubBankEntryRepository.getByParentBankId(issuanceBank)
    }

    fun fiatExchangeRates(from: FiatCurrencyUnit): MutableMap<FiatCurrencyUnit, BigDecimal> {
        val mapList = mutableMapOf(FiatCurrencyUnit.SAR to BigDecimal.ZERO)
        when (from) {
            FiatCurrencyUnit.USD -> {
                mapList[FiatCurrencyUnit.SAR] = (3.75).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.SGD -> {
                mapList[FiatCurrencyUnit.SAR] = (2.87).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.AED -> {
                mapList[FiatCurrencyUnit.SAR] = (1.02).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.EUR -> {
                mapList[FiatCurrencyUnit.SAR] = (4.10).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.GBP -> {
                mapList[FiatCurrencyUnit.SAR] = (4.60).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.IDR -> {
                mapList[FiatCurrencyUnit.SAR] = (0.00024).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.INR -> {
                mapList[FiatCurrencyUnit.SAR] = (0.046).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.PHP -> {
                mapList[FiatCurrencyUnit.SAR] = (0.070).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.PKR -> {
                mapList[FiatCurrencyUnit.SAR] = (0.014).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.SAR -> {
                mapList[FiatCurrencyUnit.SAR] = (1).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.THB -> {
                mapList[FiatCurrencyUnit.SAR] = (0.11).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.VND -> {
                mapList[FiatCurrencyUnit.SAR] = (0.00016).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.BHD -> {
                mapList[FiatCurrencyUnit.SAR] = (9.95).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.MYR -> {
                mapList[FiatCurrencyUnit.SAR] = (0.88).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            FiatCurrencyUnit.QAR -> {
                mapList[FiatCurrencyUnit.SAR] = (1.03).toBigDecimal().round(MathContext(8, RoundingMode.UP))
            }

            else -> {
                BigDecimal.ONE
            }
        }
        return mapList
    }

    fun getConversionRates(issuanceBank: IssuanceBanks): List<ConversionRateViewModel> {
        val conversionRate = issuanceConversionRateRepository.getByIssuanceBanksId(issuanceId = issuanceBank)
        val rateData = mutableListOf<ConversionRateViewModel>()
        updateConversionRatesValidTo(issuanceBank)
        if (conversionRate != null) {
            var rates = conversionRate.map {
                it.toViewModel()
            }
            rates.forEach { data ->
                if (data.isActive && data.validFrom < Instant.now()) {
                    data.currentActive = true
                }
            }
            rates = rates.filter { e -> e.isActive } as MutableList<ConversionRateViewModel>
            return rates.sortedBy { list -> list.validFrom }
        }
        return rateData
    }

    fun addConversionRates(
        issuanceBank: IssuanceBanks,
        request: IssuanceCommonController.AddConversionRate
    ): ConversionRateViewModel? {
        var rates = issuanceConversionRateRepository.getByIssuanceBanksId(issuanceBank)
        rates?.forEach { data ->
            if (data.id == request.id) {
                data.validTo = changeTimeZone(Instant.now(), issuanceBank)
                data.isActive = false
                if (request.isActive == true) {
                    issuanceConversionRateRepository.save(data).toViewModel()
                } else {
                    return issuanceConversionRateRepository.save(data).toViewModel()
                }
            }
        }
        rates = issuanceConversionRateRepository.getByIssuanceBanksId(issuanceBank)
        rates?.forEach { data ->
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val saveValidFrom = formatter.format(Date.from(data.validFrom))
            val requestValidTo = formatter.format(request.validFrom)
            if (data.currencyTo == request.currencyTo && data.currencyFrom == request.currencyFrom && saveValidFrom == requestValidTo && data.isActive) {
                throw EntityNotFoundException(ErrorCodes.CONVERSION_RATE_ALREADY_ADDED)
            }
        }
        val addIssuanceConversionRate = IssuanceConversionRate(
            issuanceBanksId = issuanceBank,
            currencyFrom = request.currencyFrom,
            currencyTo = request.currencyTo,
            baseRate = request.baseRate,
            validFrom = request.validFrom.toInstant(),
            createdBy = issuanceBank,
            createdAt = changeTimeZone(Instant.now(), issuanceBank)
        )
        return issuanceConversionRateRepository.save(addIssuanceConversionRate).toViewModel()
    }

    fun addConversionRatesAdjustment(issuanceBank: IssuanceBanks, request: IssuanceCommonController.AddConversionRateAdjustment): ConversionRateAdjustmentViewModel? {
        var rates = issuanceConversionRateAdjustmentRepository.getByIssuanceBanksId(issuanceBank)
        rates?.forEach { data ->
            if (data.id == request.id) {
                data.validTo = changeTimeZone(Instant.now(), issuanceBank)
                data.isActive = false
                if (request.isActive == true) {
                    issuanceConversionRateAdjustmentRepository.save(data)
                } else {
                    return issuanceConversionRateAdjustmentRepository.save(data).toViewModel()
                }
            }
        }
        rates = issuanceConversionRateAdjustmentRepository.getByIssuanceBanksId(issuanceBank)
        rates?.forEach { data ->
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val saveValidFrom = formatter.format(Date.from(data.validFrom))
            val requestValidTo = formatter.format(request.validFrom)
            if (data.currencyTo == request.currencyTo && data.currencyFrom == request.currencyFrom && saveValidFrom == requestValidTo && data.isActive && data.type == request.markType) {
                throw EntityNotFoundException(ErrorCodes.CONVERSION_RATE_ADJUSTMENT_ALREADY_ADDED)
            }
        }
        val addIssuanceConversionRateAdjustment = IssuanceConversionRateAdjustment(
            issuanceBanksId = issuanceBank,
            currencyFrom = request.currencyFrom,
            currencyTo = request.currencyTo,
            percentage = request.percentage,
            validFrom = request.validFrom.toInstant(),
            type = request.markType,
            createdBy = issuanceBank,
            createdAt = changeTimeZone(Instant.now(), issuanceBank)
        )
        return issuanceConversionRateAdjustmentRepository.save(addIssuanceConversionRateAdjustment).toViewModel()
    }

    fun getConversionRatesAdjustment(issuanceBank: IssuanceBanks, request: IssuanceCommonController.SearchRateAdjustment): List<ConversionRateAdjustmentViewModel> {
        val conversionRateAdj = issuanceConversionRateAdjustmentRepository.getByIssuanceBanksId(issuanceBanksId = issuanceBank)
        val rateData = mutableListOf<ConversionRateAdjustmentViewModel>()
        updateConversionRatesAdjustmentValidTo(issuanceBank, request.markType)
        if (conversionRateAdj != null) {
            var rates = conversionRateAdj.map {
                it.toViewModel()
            }
            val dateNow = changeTimeZone(Instant.now(), issuanceBank)
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            val dateNowFormat = formatter.format(Date.from(dateNow))
            rates.forEach { data ->
                if (data.isActive && data.validFrom < Instant.now()) {
                    data.currentActive = true
                }
            }
            rates = rates.filter { e -> e.isActive == true } as MutableList<ConversionRateAdjustmentViewModel>
            if (request.markType != null) {
                rates = rates.filter { e -> e.type == request.markType } as MutableList<ConversionRateAdjustmentViewModel>
            }
            return rates.sortedBy { list -> list.validFrom }
        }
        return rateData
    }

    fun getTransactionType(issuanceBank: IssuanceBanks): List<IssuanceCommonController.TransactionTypeResponse>? {
        var transactionTypeList = issuanceTransactionTypeRepository.getByIssuanceBanksId(issuanceBank)
        if (transactionTypeList.isNullOrEmpty()) {
            transactionTypeList = issuanceTransactionTypeRepository.findAll() as List<IssuanceTransactionType>
        }
        val transactionTypeDataResponse: MutableList<IssuanceCommonController.TransactionTypeResponse> = mutableListOf()
        transactionTypeList.forEach { tData ->
            transactionTypeDataResponse.add(
                IssuanceCommonController.TransactionTypeResponse(
                    id = tData.id,
                    transactionTypeId = tData.transactionTypeId,
                    transactionType = tData.transactionType
                )
            )
        }
        return transactionTypeDataResponse
    }

    fun getWalletFeeType(issuanceBank: IssuanceBanks): MutableIterable<IssuanceCommonController.WalletFeeTypeResponse> {
        var walletFeeTypeList = issuanceWalletFeeTypeRepository.getByIssuanceBanksId(issuanceBank)
        if (walletFeeTypeList.isNullOrEmpty()) {
            walletFeeTypeList = issuanceWalletFeeTypeRepository.findAll() as List<IssuanceWalletFeeType>
        }
        val walletFeeTypeResponse: MutableList<IssuanceCommonController.WalletFeeTypeResponse> = mutableListOf()
        walletFeeTypeList.forEach { tData ->
            walletFeeTypeResponse.add(
                IssuanceCommonController.WalletFeeTypeResponse(
                    id = tData.id,
                    walletFeeId = tData.walletFeeId,
                    feeType = tData.feeType
                )
            )
        }
        return walletFeeTypeResponse
    }

    fun getWalletFeeConfig(issuanceBank: IssuanceBanks): List<IssuanceWalletConfigViewModel>? {
        val walletConfig = issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = issuanceBank)
        var rateData = mutableListOf<IssuanceWalletConfigViewModel>()
        if (walletConfig != null) {
            var data = walletConfig.map {
                it.toViewModel()
            }
            data = data.filter { e -> e.isActive }
            data.forEach { tData ->
                IssuanceCommonController.Frequency.values().forEach { fData ->
                    if (tData.frequency == fData.frequencyId) {
                        tData.frequencyStr = fData.frequencyType
                    }
                }
                IssuanceCommonController.WalletFeeType.values().forEach { wData ->
                    if (tData.walletFeeId == wData.walletFeeId) {
                        tData.walletFeeType = wData.walletFeeType
                    }
                }
            }
            rateData = data as MutableList<IssuanceWalletConfigViewModel>
        }
        return rateData
    }

    fun saveWalletFeeConfig(issuanceBank: IssuanceBanks, addWalletFeeConfig: IssuanceCommonController.AddWalletFeeConfig): IssuanceWalletConfigViewModel? {
        var frequency: String? = null
        if (addWalletFeeConfig.frequency != null) {
            frequency = addWalletFeeConfig.frequency.toString().trim()
        }
        var walletConfig = issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBank)
        walletConfig?.forEach { data ->
            if (addWalletFeeConfig.id != null && data.id != addWalletFeeConfig.id) {
                if ((data.frequency == frequency) && (data.walletFeeId == addWalletFeeConfig.walletConfigType.toString()) && data.isActive && (data.fiatCurrency == addWalletFeeConfig.fiatCurrency)) {
                    throw EntityNotFoundException(ErrorCodes.WALLET_FEE_CONFIG_ALREADY_ADDED)
                }
            }
            if (data.id == addWalletFeeConfig.id) {
                data.isActive = false
                data.modifiedDate = changeTimeZone(Instant.now(), issuanceBank)
                data.modifiedBy = issuanceBank
                issuanceWalletConfigRepository.save(data)
                if (addWalletFeeConfig.isActive != null && addWalletFeeConfig.isActive == false) {
                    return issuanceWalletConfigRepository.save(data).toViewModel()
                }
            }
        }
        walletConfig?.forEach { data ->
            if (addWalletFeeConfig.id != null && data.id != addWalletFeeConfig.id) {
                if ((data.frequency == frequency) && (data.walletFeeId == addWalletFeeConfig.walletConfigType.toString()) && data.isActive && (data.fiatCurrency == addWalletFeeConfig.fiatCurrency)) {
                    throw EntityNotFoundException(ErrorCodes.WALLET_FEE_CONFIG_ALREADY_ADDED)
                }
            }
        }
        walletConfig = issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBank)
        walletConfig?.forEach { data ->
            if (data.frequency == frequency && data.walletFeeId == addWalletFeeConfig.walletConfigType.toString() && data.isActive && data.fiatCurrency == addWalletFeeConfig.fiatCurrency) {
                throw EntityNotFoundException(ErrorCodes.WALLET_FEE_CONFIG_ALREADY_ADDED)
            }
        }
        val addReq = IssuanceWalletConfig(
            issuanceBanksId = issuanceBank,
            fiatCurrency = if (addWalletFeeConfig.fiatCurrency != null) addWalletFeeConfig.fiatCurrency.toString() else FiatCurrencyUnit.MYR.toString(),
            walletFeeId = addWalletFeeConfig.walletConfigType.toString(),
            frequency = if (addWalletFeeConfig.frequency != null) addWalletFeeConfig.frequency.toString() else null,
            feeValue = addWalletFeeConfig.value,
            minValue = if (addWalletFeeConfig.minimum != null) addWalletFeeConfig.minimum.setScale(ROUNDING_LIMIT, RoundingMode.UP) else null,
            maxValue = if (addWalletFeeConfig.maximum != null) addWalletFeeConfig.maximum.setScale(ROUNDING_LIMIT, RoundingMode.UP) else null,
            createdBy = issuanceBank,
            createdDate = changeTimeZone(Instant.now(), issuanceBank)
        )
        val resData = issuanceWalletConfigRepository.save(addReq).toViewModel()
        IssuanceCommonController.Frequency.values().forEach { data ->
            if (resData.frequency == data.frequencyId) {
                resData.frequencyStr = data.frequencyType
            }
        }
        IssuanceCommonController.WalletFeeType.values().forEach { data ->
            if (resData.walletFeeId == data.walletFeeId) {
                resData.walletFeeType = data.walletFeeType
            }
        }
        return resData
    }

    fun getTransactionLimitConfig(issuanceBank: IssuanceBanks): List<TransactionLimitConfigViewModel>? {
        val transactionLimit = issuanceTransactionLimitConfigRepository.getByIssuanceBanksId(issuanceBanksId = issuanceBank)
        var rateData = mutableListOf<TransactionLimitConfigViewModel>()
        if (transactionLimit != null) {
            var data = transactionLimit.map {
                it.toViewModel()
            }
            data = data.filter { e -> e.isActive }
            data.forEach { tData ->
                IssuanceCommonController.Frequency.values().forEach { data ->
                    if (tData.frequency == data.frequencyId) {
                        tData.frequencyStr = data.frequencyType
                    }
                }
                IssuanceCommonController.TransactionLoadingType.values().forEach { data ->
                    if (tData.transactionTypeId == data.transactionId) {
                        tData.transactionType = data.transactionType
                    }
                }
            }
            rateData = data as MutableList<TransactionLimitConfigViewModel>
        }
        return rateData
    }

    fun saveTransactionLimitConfig(issuanceBank: IssuanceBanks, addTransactionLimitConfig: IssuanceCommonController.AddTransactionLimitConfig): TransactionLimitConfigViewModel? {
        var transactionLimit = issuanceTransactionLimitConfigRepository.getByIssuanceBanksId(issuanceBank)
        transactionLimit?.forEach { data ->
            if (data.id != addTransactionLimitConfig.id) {
                if ((addTransactionLimitConfig.frequency != null && data.frequency == addTransactionLimitConfig.frequency.toString()) &&
                    (addTransactionLimitConfig.fiatCurrency != null && data.fiatCurrency == addTransactionLimitConfig.fiatCurrency.toString()) &&
                    (data.transactionTypeId == addTransactionLimitConfig.transactionType.toString()) && data.isActive
                ) {
                    throw EntityNotFoundException(ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED)
                } else if (addTransactionLimitConfig.frequency == null && data.frequency == null &&
                    data.transactionTypeId == addTransactionLimitConfig.transactionType.toString() && data.isActive &&
                    (addTransactionLimitConfig.fiatCurrency != null && data.fiatCurrency == addTransactionLimitConfig.fiatCurrency.toString())
                ) {
                    throw EntityNotFoundException(ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED)
                }
            }
            if (data.id == addTransactionLimitConfig.id) {
                data.isActive = false
                data.modifiedDate = changeTimeZone(Instant.now(), issuanceBank)
                data.modifiedBy = issuanceBank
                issuanceTransactionLimitConfigRepository.save(data)
                if (addTransactionLimitConfig.isActive == false) {
                    return issuanceTransactionLimitConfigRepository.save(data).toViewModel()
                }
            }
        }
        transactionLimit?.forEach { data ->
            if (data.id == addTransactionLimitConfig.id) {
                data.isActive = false
                data.modifiedDate = changeTimeZone(Instant.now(), issuanceBank)
                data.modifiedBy = issuanceBank
                issuanceTransactionLimitConfigRepository.save(data)
                if (addTransactionLimitConfig.isActive == false) {
                    return issuanceTransactionLimitConfigRepository.save(data).toViewModel()
                }
            }
        }
        transactionLimit = issuanceTransactionLimitConfigRepository.getByIssuanceBanksId(issuanceBank)
        transactionLimit?.forEach { data ->
            if ((addTransactionLimitConfig.frequency != null && data.frequency == addTransactionLimitConfig.frequency.toString()) &&
                (addTransactionLimitConfig.fiatCurrency != null && data.fiatCurrency == addTransactionLimitConfig.fiatCurrency.toString()) &&
                (data.transactionTypeId == addTransactionLimitConfig.transactionType.toString()) && data.isActive
            ) {
                throw EntityNotFoundException(ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED)
            } else if (addTransactionLimitConfig.frequency == null && data.frequency == null &&
                data.transactionTypeId == addTransactionLimitConfig.transactionType.toString() && data.isActive &&
                (addTransactionLimitConfig.fiatCurrency != null && data.fiatCurrency == addTransactionLimitConfig.fiatCurrency.toString())
            ) {
                throw EntityNotFoundException(ErrorCodes.TRANSACTION_LIMIT_ALREADY_ADDED)
            }
        }

        val addReq = IssuanceTransactionLimitConfig(
            issuanceBanksId = issuanceBank,
            fiatCurrency = if (addTransactionLimitConfig.fiatCurrency != null) addTransactionLimitConfig.fiatCurrency.toString() else FiatCurrencyUnit.MYR.toString(),
            transactionTypeId = addTransactionLimitConfig.transactionType.toString(),
            frequency = if (addTransactionLimitConfig.frequency != null) addTransactionLimitConfig.frequency.toString() else null,
            count = if (addTransactionLimitConfig.transactionCount != null) addTransactionLimitConfig.transactionCount.toString() else null,
            minValue = if (addTransactionLimitConfig.minimum != null) addTransactionLimitConfig.minimum.setScale(ROUNDING_LIMIT, RoundingMode.UP) else null,
            maxValue = if (addTransactionLimitConfig.maximum != null) addTransactionLimitConfig.maximum.setScale(ROUNDING_LIMIT, RoundingMode.UP) else null,
            digitalCurrency = addTransactionLimitConfig.digitalCurrency,
            incrementalQuantity = addTransactionLimitConfig.incrementalQuantity,
            quantityUnit = addTransactionLimitConfig.quantityUnit,
            createdBy = issuanceBank,
            createdDate = changeTimeZone(Instant.now(), issuanceBank)
        )
        val resData = issuanceTransactionLimitConfigRepository.save(addReq).toViewModel()
        IssuanceCommonController.Frequency.values().forEach { data ->
            if (resData.frequency == data.frequencyId) {
                resData.frequencyStr = data.frequencyType
            }
        }
        IssuanceCommonController.TransactionLoadingType.values().forEach { data ->
            if (resData.transactionTypeId == data.transactionId) {
                resData.transactionType = data.transactionType
            }
        }
        return resData
    }

    fun fiatExchangeRatesNew(
        from: FiatCurrencyUnit,
        transactionType: IssuanceWalletUserController.TransactionType,
        userAccount: UserAccount
    ): MutableMap<FiatCurrencyUnit, BigDecimal>? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val dateNow = Instant.now()
        var markValue = BigDecimal.ZERO
        var baseRate: BigDecimal
        val markData = issuanceBanksUserEntry?.let { issuanceConversionRateAdjustmentRepository.getByIssuanceBanksId(it.issuanceBanksId) }
        var markDataAfterFilter: MutableList<IssuanceConversionRateAdjustment> = mutableListOf()
        if (markData != null && transactionType == IssuanceWalletUserController.TransactionType.LOAD) {
            markDataAfterFilter = markData.filter { e -> e.currencyFrom == from.toString() } as MutableList<IssuanceConversionRateAdjustment>
        }
        if (markData != null && transactionType == IssuanceWalletUserController.TransactionType.REFUND) {
            markDataAfterFilter = markData.filter { e -> e.currencyFrom == CurrencyUnit.SART.toString() } as MutableList<IssuanceConversionRateAdjustment>
        }
        if (markDataAfterFilter.isNotEmpty()) {
            var markType = IssuanceCommonController.MarkType.UP
            if (transactionType == IssuanceWalletUserController.TransactionType.REFUND) {
                markType = IssuanceCommonController.MarkType.DOWN
            }
            val smallerDateData = markDataAfterFilter.filter { e -> e.isActive && e.validFrom < dateNow && e.type == markType }
            val greaterDateData = markDataAfterFilter.filter { e -> e.isActive && e.validFrom > dateNow && e.type == markType }
            markValue = if (smallerDateData.isNotEmpty()) {
                val afterComparison = smallerDateData.stream()
                    .max(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                afterComparison.get().percentage!!
            } else (
                if (greaterDateData.isNotEmpty()) {
                    val afterComparison = greaterDateData.stream()
                        .min(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                    afterComparison.get().percentage
                } else {
                    BigDecimal.ZERO
                }
                )!!
        }
        val rates = issuanceBanksUserEntry?.let { issuanceConversionRateRepository.getByIssuanceBanksId(it.issuanceBanksId) }
        var ratesAfterFilter: MutableList<IssuanceConversionRate> = mutableListOf()
        if (rates != null) {
            ratesAfterFilter = rates.filter { e -> e.currencyFrom == from } as MutableList<IssuanceConversionRate>
        }
        val mapList = mutableMapOf(FiatCurrencyUnit.SAR to BigDecimal.ZERO)
        if (ratesAfterFilter.isNotEmpty()) {
            val smallerDateData = ratesAfterFilter.filter { e -> e.isActive && e.validFrom < dateNow }
            val greaterDateData = ratesAfterFilter.filter { e -> e.isActive && e.validFrom > dateNow }
            baseRate = if (smallerDateData.isNotEmpty()) {
                val afterComparison = smallerDateData.stream()
                    .max(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                afterComparison.get().baseRate
            } else (
                if (greaterDateData.isNotEmpty()) {
                    val afterComparison = greaterDateData.stream()
                        .min(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                    afterComparison.get().baseRate
                } else {
                    val fiatToCurrencyRate = fiatExchangeRates(from)
                    fiatToCurrencyRate[FiatCurrencyUnit.SAR]
                }
                )!!
        } else {
            val fiatToCurrencyRate = fiatExchangeRates(from)
            baseRate = fiatToCurrencyRate[FiatCurrencyUnit.SAR]!!
        }
        val divisionValue = 100
        if (markValue > BigDecimal.ZERO && baseRate > BigDecimal.ZERO && transactionType == IssuanceWalletUserController.TransactionType.LOAD) {
            val value = (baseRate * markValue) / divisionValue.toBigDecimal()
            baseRate = baseRate.minus(value)
        }
        if (markValue > BigDecimal.ZERO && baseRate > BigDecimal.ZERO && transactionType == IssuanceWalletUserController.TransactionType.REFUND) {
            val value = (baseRate * markValue) / divisionValue.toBigDecimal()
            baseRate = baseRate.plus(value)
        }
        mapList[FiatCurrencyUnit.SAR] = baseRate
        if (baseRate == BigDecimal.ZERO && markValue == BigDecimal.ZERO) {
            return fiatExchangeRates(from)
        }
        return mapList
    }

    fun getMarkUpDownDetails(
        from: FiatCurrencyUnit,
        transactionType: IssuanceWalletUserController.TransactionType,
        userAccount: UserAccount
    ): BigDecimal? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val dateNow = Instant.now()
        var markValue = BigDecimal.ZERO
        val markData = issuanceBanksUserEntry?.let { issuanceConversionRateAdjustmentRepository.getByIssuanceBanksId(it.issuanceBanksId) }
        var markDataAfterFilter: MutableList<IssuanceConversionRateAdjustment> = mutableListOf()
        if (markData != null) {
            markDataAfterFilter = markData.filter { e -> e.currencyFrom == from.toString() } as MutableList<IssuanceConversionRateAdjustment>
        }
        if (markDataAfterFilter.isNotEmpty()) {
            var markType = IssuanceCommonController.MarkType.UP
            if (transactionType == IssuanceWalletUserController.TransactionType.REFUND) {
                markType = IssuanceCommonController.MarkType.DOWN
            }
            val smallerDateData = markDataAfterFilter.filter { e -> e.isActive && e.validFrom < dateNow && e.type == markType }
            val greaterDateData = markDataAfterFilter.filter { e -> e.isActive && e.validFrom > dateNow && e.type == markType }
            markValue = if (smallerDateData.isNotEmpty()) {
                val afterComparison = smallerDateData.stream()
                    .max(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                afterComparison.get().percentage!!
            } else (
                if (greaterDateData.isNotEmpty()) {
                    val afterComparison = greaterDateData.stream()
                        .min(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                    afterComparison.get().percentage
                } else {
                    BigDecimal.ZERO
                }
                )!!
        }
        return markValue
    }
    fun getBaseRate(
        from: FiatCurrencyUnit,
        transactionType: IssuanceWalletUserController.TransactionType,
        userAccount: UserAccount
    ): BigDecimal {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val dateNow = Instant.now()
        var baseRate = BigDecimal.ZERO
        val rates = issuanceBanksUserEntry?.let { issuanceConversionRateRepository.getByIssuanceBanksId(it.issuanceBanksId) }
        var ratesAfterFilter: MutableList<IssuanceConversionRate> = mutableListOf()
        if (rates != null) {
            ratesAfterFilter = rates.filter { e -> e.currencyFrom == from } as MutableList<IssuanceConversionRate>
        }
        val mapList = mutableMapOf(FiatCurrencyUnit.SAR to BigDecimal.ZERO)
        if (ratesAfterFilter.isNotEmpty()) {
            val smallerDateData = ratesAfterFilter.filter { e -> e.isActive && e.validFrom < dateNow }
            val greaterDateData = ratesAfterFilter.filter { e -> e.isActive && e.validFrom > dateNow }
            baseRate = if (smallerDateData.isNotEmpty()) {
                val afterComparison = smallerDateData.stream()
                    .max(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                afterComparison.get().baseRate
            } else (
                if (greaterDateData.isNotEmpty()) {
                    val afterComparison = greaterDateData.stream()
                        .min(Comparator.comparingLong { x -> ChronoUnit.MINUTES.between(Instant.now(), x.validFrom) })
                    afterComparison.get().baseRate
                } else {
                    val fiatToCurrencyRate = fiatExchangeRates(from)
                    fiatToCurrencyRate[FiatCurrencyUnit.SAR]
                }
                )!!
        } else {
            val fiatToCurrencyRate = fiatExchangeRates(from)
            baseRate = fiatToCurrencyRate[FiatCurrencyUnit.SAR]!!
        }
        return baseRate
    }

    fun getWalletFeeDetails(
        transactionType: IssuanceCommonController.TransactionLoadingType,
        userAccount: UserAccount,
        accountOwner: AccountOwner
    ): List<IssuanceWalletUserController.FeeConfigDetails>? {
        val feeType: MutableList<IssuanceCommonController.WalletFeeType> = mutableListOf()
        if (transactionType.transactionId == IssuanceCommonController.TransactionLoadingType.TTC_002.toString()) {
            feeType.add(IssuanceCommonController.WalletFeeType.WF_001)
            feeType.add(IssuanceCommonController.WalletFeeType.WF_002)
        }
        if (transactionType.transactionId == IssuanceCommonController.TransactionLoadingType.TTC_001.toString()) {
            feeType.add(IssuanceCommonController.WalletFeeType.WF_001)
            feeType.add(IssuanceCommonController.WalletFeeType.WF_006)
        }
        if (transactionType.transactionId == IssuanceCommonController.TransactionLoadingType.TTC_006.toString()) {
            feeType.add(IssuanceCommonController.WalletFeeType.WF_004)
        }
        val feeDataResponse: MutableList<IssuanceWalletUserController.FeeConfigDetails> = mutableListOf()
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        if (issuanceBanksUserEntry != null) {
            feeType.forEach { feeData ->
                if (feeData.walletFeeId == IssuanceCommonController.WalletFeeType.WF_001.toString()) {
                    val feeConfigData = issuanceConfigurationService.getFeeConfigDetails(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.ONE_TIME.frequencyId)
                    if (feeConfigData != null) {
                        val request = GetTransactionListRequest(
                            asset = mutableSetOf(CurrencyUnit.SART.toString()),
                            type = mutableSetOf(TransactionType.DEPOSIT)
                        )
                        val transaction = userAccount.account.owner?.let { transactionService.getTransactions(it, request) }
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO && transaction.isNullOrEmpty()) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                }
                if (feeData.walletFeeId == IssuanceCommonController.WalletFeeType.WF_006.toString()) {
                    val feeConfigData = issuanceConfigurationService.getFeeConfigDetails(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.ONE_TIME.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                }
                if (feeData.walletFeeId == IssuanceCommonController.WalletFeeType.WF_004.toString()) {
                    val feeConfigData = issuanceConfigurationService.getFeeConfigDetails(userAccount, feeData.walletFeeId, null)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                }
                if (feeData.walletFeeId == IssuanceCommonController.WalletFeeType.WF_002.toString()) {
                    var feeConfigData = issuanceConfigurationService.getFeeConfigDetails(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.ONE_TIME.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                    feeConfigData = issuanceConfigurationService.getFeeConfigDetails(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.PER_TRANSACTION.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                    feeConfigData = issuanceConfigurationService.checkLoadingFee(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.DAILY.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                    feeConfigData = issuanceConfigurationService.checkLoadingFee(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.WEEKLY.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                    feeConfigData = issuanceConfigurationService.checkLoadingFee(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.MONTHLY.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                    feeConfigData = issuanceConfigurationService.checkLoadingFee(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.HALF_YEARLY.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                    feeConfigData = issuanceConfigurationService.checkLoadingFee(userAccount, feeData.walletFeeId, IssuanceCommonController.Frequency.YEARLY.frequencyId)
                    if (feeConfigData != null) {
                        if (feeConfigData.feeAmount!! > BigDecimal.ZERO) {
                            feeDataResponse.add(feeConfigData)
                        }
                    }
                }
            }
            if (feeDataResponse.size > 0) {
                return feeDataResponse
            }
        }
        return null
    }

    fun checkTransactionLimitConfig(
        transactionType: IssuanceCommonController.TransactionLoadingType,
        userAccount: UserAccount,
        accountOwner: AccountOwner
    ): IssuanceWalletUserController.TransactionLimitResponse? {
        val instant = Instant.now()
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        var fiatCurrency: String? = null
        if (issuanceBanksUserEntry?.issuanceBanksId?.fiatCurrency != null) {
            fiatCurrency = issuanceBanksUserEntry.issuanceBanksId.fiatCurrency.toString()
        }
        var transactionLimit = issuanceBanksUserEntry?.let {
            fiatCurrency?.let { it1 ->
                issuanceTransactionLimitConfigRepository.getByTransactionTypeIdAndIssuanceBanksIdAndFiatCurrency(
                    transactionType.transactionId, it.issuanceBanksId,
                    it1
                )
            }
        }
        if (transactionLimit != null) {
            transactionLimit = transactionLimit.filter { e -> e.isActive }
        }
        if (transactionLimit.isNullOrEmpty()) {
            if (transactionType.transactionId == IssuanceCommonController.TransactionLoadingType.TTC_004.transactionId) {
                transactionLimit = issuanceBanksUserEntry?.let { issuanceTransactionLimitConfigRepository.getByTransactionTypeIdAndIssuanceBanksId(IssuanceCommonController.TransactionLoadingType.TTC_003.toString(), it.issuanceBanksId) }
            } else if (transactionType.transactionId == IssuanceCommonController.TransactionLoadingType.TTC_005.transactionId) {
                transactionLimit = issuanceBanksUserEntry?.let { issuanceTransactionLimitConfigRepository.getByTransactionTypeIdAndIssuanceBanksId(IssuanceCommonController.TransactionLoadingType.TTC_003.toString(), it.issuanceBanksId) }
            }
        }
        val resData = IssuanceWalletUserController.TransactionLimitResponse()
        val feeConfig = getWalletFeeDetails(transactionType, userAccount, accountOwner)
        resData.feeConfig = feeConfig
        if (transactionLimit != null && issuanceBanksUserEntry != null) {
            var data = transactionLimit.map {
                it.toViewModel()
            }
            data = data.filter { e -> e.isActive }
            var minimumBalancePerTransaction: BigDecimal? = null
            val loadingCount = issuanceGraphService.totalTransactionDataForWalletUser(issuanceBanksUserEntry.issuanceBanksId, TransactionType.DEPOSIT, null, null, accountOwner, null)
            resData.initialLoading = loadingCount <= 0
            data.forEach { tData ->
                if (tData.isActive && tData.frequency == IssuanceCommonController.Frequency.PER_TRANSACTION.toString()) {
                    minimumBalancePerTransaction = tData.minValue?.toBigDecimal()
                }
            }
            data.forEach { tData ->
                if (tData.isActive) {
                    var startDate = issuanceBanksUserEntry.createdAt
                    // Define the timezone you are working with
                    val zoneId = ZoneId.systemDefault()
                    // Get the current date
                    val today = LocalDate.now(zoneId)
                    val transactionValue = IssuanceWalletUserController.TransactionLimitValue(
                        count = tData.transactionCount,
                        minimumBalance = tData.minValue?.toBigDecimal(),
                        maximumBalance = tData.maxValue?.toBigDecimal()
                    )
                    if (tData.frequency == IssuanceCommonController.Frequency.ONE_TIME.toString()) {
                        resData.ONE_TIME = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.PER_TRANSACTION.toString()) {
                        minimumBalancePerTransaction = tData.minValue?.toBigDecimal()
                        resData.PER_TRANSACTION = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.DAILY.toString()) {
                        // Start of the day (12:00 AM)
                        val startOfDay = today.atStartOfDay(zoneId).toInstant()
                        // End of the day (11:59:59.999 PM)
                        val endOfDay = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

                        val totalTransactionCount = totalTransactionCount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startOfDay,
                            endOfDay,
                            tData.frequency
                        )
                        val totalTransactionAmountRes = totalTransactionAmount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startOfDay,
                            endOfDay,
                            tData.frequency
                        )
                        var availableCount = if (tData.transactionCount != null) tData.transactionCount.minus(totalTransactionCount) else null
                        if (availableCount != null && availableCount < 0) {
                            availableCount = 0
                        }
                        var remainingMaximumBalance = if (tData.maxValue != null) tData.maxValue.toBigDecimal().minus(totalTransactionAmountRes) else null
                        if (remainingMaximumBalance != null && remainingMaximumBalance < BigDecimal.ZERO) {
                            remainingMaximumBalance = BigDecimal.ZERO
                        }
                        transactionValue.minimumBalance = minimumBalancePerTransaction
                        transactionValue.transactionCount = totalTransactionCount
                        transactionValue.transactionBalance = totalTransactionAmountRes
                        transactionValue.availableCount = availableCount
                        transactionValue.remainingMaximumBalance = remainingMaximumBalance
                        resData.DAILY = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.WEEKLY.toString()) {
                        // Find the Monday of the current week (start of the week)
                        val monday = today.with(DayOfWeek.MONDAY)
                        val startOfWeek = monday.atStartOfDay(zoneId).toInstant()

                        // Find the Sunday of the current week (end of the week)
                        val sunday = monday.plusDays(6)
                        val endOfWeek = sunday.atTime(LocalTime.MAX).atZone(zoneId).toInstant()

                        val totalTransactionCount = totalTransactionCount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startOfWeek,
                            endOfWeek,
                            tData.frequency
                        )
                        val totalTransactionAmountRes = totalTransactionAmount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startOfWeek,
                            endOfWeek,
                            tData.frequency
                        )
                        var availableCount = if (tData.transactionCount != null) tData.transactionCount.minus(totalTransactionCount) else null
                        if (availableCount != null && availableCount < 0) {
                            availableCount = 0
                        }
                        var remainingMaximumBalance = if (tData.maxValue != null) tData.maxValue.toBigDecimal().minus(totalTransactionAmountRes) else null
                        if (remainingMaximumBalance != null && remainingMaximumBalance < BigDecimal.ZERO) {
                            remainingMaximumBalance = BigDecimal.ZERO
                        }
                        transactionValue.minimumBalance = minimumBalancePerTransaction
                        transactionValue.transactionCount = totalTransactionCount
                        transactionValue.transactionBalance = totalTransactionAmountRes
                        transactionValue.availableCount = availableCount
                        transactionValue.remainingMaximumBalance = remainingMaximumBalance
                        resData.WEEKLY = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.MONTHLY.toString()) {
                        var endDate = startDate.plus(30, ChronoUnit.DAYS)
                        for (i in 1..24) {
                            if (endDate < instant) {
                                startDate = endDate
                                endDate = endDate.plus(30, ChronoUnit.DAYS)
                            } else {
                                break
                            }
                        }
                        val totalTransactionCount = totalTransactionCount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        val totalTransactionAmountRes = totalTransactionAmount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        var availableCount = if (tData.transactionCount != null) tData.transactionCount.minus(totalTransactionCount) else null
                        if (availableCount != null && availableCount < 0) {
                            availableCount = 0
                        }
                        var remainingMaximumBalance = if (tData.maxValue != null) tData.maxValue.toBigDecimal().minus(totalTransactionAmountRes) else null
                        if (remainingMaximumBalance != null && remainingMaximumBalance < BigDecimal.ZERO) {
                            remainingMaximumBalance = BigDecimal.ZERO
                        }
                        transactionValue.minimumBalance = minimumBalancePerTransaction
                        transactionValue.transactionCount = totalTransactionCount
                        transactionValue.transactionBalance = totalTransactionAmountRes
                        transactionValue.availableCount = availableCount
                        transactionValue.remainingMaximumBalance = remainingMaximumBalance
                        resData.MONTHLY = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.QUARTERLY.toString()) {
                        var endDate = startDate.plus(92, ChronoUnit.DAYS)
                        for (i in 1..8) {
                            if (endDate < instant) {
                                startDate = endDate
                                endDate = endDate.plus(92, ChronoUnit.DAYS)
                            } else {
                                break
                            }
                        }
                        val totalTransactionCount = totalTransactionCount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        val totalTransactionAmountRes = totalTransactionAmount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        var availableCount = if (tData.transactionCount != null) tData.transactionCount.minus(totalTransactionCount) else null
                        if (availableCount != null && availableCount < 0) {
                            availableCount = 0
                        }
                        var remainingMaximumBalance = if (tData.maxValue != null) tData.maxValue.toBigDecimal().minus(totalTransactionAmountRes) else null
                        if (remainingMaximumBalance != null && remainingMaximumBalance < BigDecimal.ZERO) {
                            remainingMaximumBalance = BigDecimal.ZERO
                        }
                        transactionValue.minimumBalance = minimumBalancePerTransaction
                        transactionValue.transactionCount = totalTransactionCount
                        transactionValue.transactionBalance = totalTransactionAmountRes
                        transactionValue.availableCount = availableCount
                        transactionValue.remainingMaximumBalance = remainingMaximumBalance
                        resData.QUARTERLY = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.HALF_YEARLY.toString()) {
                        var endDate = startDate.plus(183, ChronoUnit.DAYS)
                        for (i in 1..4) {
                            if (endDate < instant) {
                                startDate = endDate
                                endDate = endDate.plus(183, ChronoUnit.DAYS)
                            } else {
                                break
                            }
                        }
                        val totalTransactionCount = totalTransactionCount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        val totalTransactionAmountRes = totalTransactionAmount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        var availableCount = if (tData.transactionCount != null) tData.transactionCount.minus(totalTransactionCount) else null
                        if (availableCount != null && availableCount < 0) {
                            availableCount = 0
                        }
                        var remainingMaximumBalance = if (tData.maxValue != null) tData.maxValue.toBigDecimal().minus(totalTransactionAmountRes) else null
                        if (remainingMaximumBalance != null && remainingMaximumBalance < BigDecimal.ZERO) {
                            remainingMaximumBalance = BigDecimal.ZERO
                        }
                        transactionValue.minimumBalance = minimumBalancePerTransaction
                        transactionValue.transactionCount = totalTransactionCount
                        transactionValue.transactionBalance = totalTransactionAmountRes
                        transactionValue.availableCount = availableCount
                        transactionValue.remainingMaximumBalance = remainingMaximumBalance
                        resData.HALF_YEARLY = transactionValue
                    }
                    if (tData.frequency == IssuanceCommonController.Frequency.YEARLY.toString()) {
                        var endDate = startDate.plus(365, ChronoUnit.DAYS)
                        for (i in 1..2) {
                            if (endDate < instant) {
                                startDate = endDate
                                endDate = endDate.plus(365, ChronoUnit.DAYS)
                            } else {
                                break
                            }
                        }
                        val totalTransactionCount = totalTransactionCount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        val totalTransactionAmountRes = totalTransactionAmount(
                            accountOwner,
                            issuanceBanksUserEntry.issuanceBanksId,
                            tData.transactionTypeId,
                            startDate,
                            endDate,
                            tData.frequency
                        )
                        var availableCount = if (tData.transactionCount != null) tData.transactionCount.minus(totalTransactionCount) else null
                        if (availableCount != null && availableCount < 0) {
                            availableCount = 0
                        }
                        var remainingMaximumBalance = if (tData.maxValue != null) tData.maxValue.toBigDecimal().minus(totalTransactionAmountRes) else null
                        if (remainingMaximumBalance != null && remainingMaximumBalance < BigDecimal.ZERO) {
                            remainingMaximumBalance = BigDecimal.ZERO
                        }
                        transactionValue.minimumBalance = minimumBalancePerTransaction
                        transactionValue.transactionCount = totalTransactionCount
                        transactionValue.transactionBalance = totalTransactionAmountRes
                        transactionValue.availableCount = availableCount
                        transactionValue.remainingMaximumBalance = remainingMaximumBalance
                        resData.YEARLY = transactionValue
                    }
                }
            }
        }
        return resData
    }

    fun totalTransactionCount(
        accountOwner: AccountOwner,
        issuanceBank: IssuanceBanks,
        type: String,
        startDate: Instant?,
        endDate: Instant?,
        frequency: String?
    ): Int {
        var transactionType = TransactionType.DEPOSIT
        var transactionMode: TransactionMode ? = null
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_001.toString() || type == IssuanceCommonController.TransactionLoadingType.TTC_002.toString()) {
            transactionType = TransactionType.DEPOSIT
            transactionMode = null
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_004.toString()) {
            transactionType = TransactionType.OTHER
            transactionMode = TransactionMode.MERCHANT_OFFLINE
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_003.toString()) {
            transactionType = TransactionType.POS
            transactionMode = TransactionMode.CUSTOMER_MERCHANT_ONLINE
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_007.toString()) {
            transactionType = TransactionType.PEER_TO_PEER
            transactionMode = null
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_005.toString()) {
            transactionType = TransactionType.POS
            transactionMode = TransactionMode.CUSTOMER_OFFLINE
        }

        val transactionSize = issuanceGraphService.totalTransactionDataForWalletUser(issuanceBank, transactionType, startDate, endDate, accountOwner, transactionMode)
        if (transactionSize > 0) {
            if (type == IssuanceCommonController.TransactionLoadingType.TTC_002.toString()) {
                val tobeMinus = issuanceGraphService.totalTransactionDeposit(
                    issuanceBank,
                    transactionType,
                    startDate,
                    endDate,
                    accountOwner,
                    frequency
                )
                return transactionSize.minus(tobeMinus)
            }
            return transactionSize
        }
        return 0
    }
    fun totalTransactionAmount(
        accountOwner: AccountOwner,
        issuanceBank: IssuanceBanks,
        type: String,
        startDate: Instant,
        endDate: Instant,
        frequency: String?
    ): BigDecimal {
/* Weekly data */
        var transactionType = TransactionType.DEPOSIT
        var transactionMode: TransactionMode ? = null
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_001.toString() && type == IssuanceCommonController.TransactionLoadingType.TTC_002.toString()) {
            transactionType = TransactionType.DEPOSIT
            transactionMode = null
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_004.toString()) {
            transactionType = TransactionType.OTHER
            transactionMode = TransactionMode.MERCHANT_OFFLINE
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_003.toString()) {
            transactionType = TransactionType.POS
            transactionMode = TransactionMode.CUSTOMER_MERCHANT_ONLINE
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_007.toString()) {
            transactionType = TransactionType.PEER_TO_PEER
            transactionMode = null
        }
        if (type == IssuanceCommonController.TransactionLoadingType.TTC_005.toString()) {
            transactionType = TransactionType.POS
            transactionMode = TransactionMode.CUSTOMER_OFFLINE
        }
        var transactionAmount: BigDecimal
        transactionAmount = if (type == IssuanceCommonController.TransactionLoadingType.TTC_002.toString()) {
            issuanceGraphService.totalTransactionFiatAmount(issuanceBank, transactionType, endDate, startDate, accountOwner)
        } else {
            issuanceGraphService.totalTransactionDigitalAmount(issuanceBank, transactionType, endDate, startDate, accountOwner, transactionMode)
        }
        if (transactionAmount > BigDecimal.ZERO) {
            if (type == IssuanceCommonController.TransactionLoadingType.TTC_002.toString()) {
                val toBeMinus = issuanceGraphService.totalInitialTransactionFiatAmount(issuanceBank, transactionType, endDate, startDate, accountOwner, frequency)
                return transactionAmount.minus(toBeMinus)
            }
            return transactionAmount
        }
        return BigDecimal.ZERO
    }

    fun changeTimeZone(date: Instant, issuanceBank: IssuanceBanks): Instant {
        val issuanceZone = ZoneId.of(issuanceBank.timeZone)
        return date.atZone(issuanceZone).toInstant()
    }

    fun getInstitutionDetails(issuanceBank: IssuanceBanks, request: IssuanceCommonController.InstitutionRequest): IssuanceCommonController.InstitutionResponse? {
        var issuanceBankDataList = mutableListOf<IssuanceCommonController.InstitutionRegisterData>()
        var issuanceBankList = mutableListOf<IssuanceBanks>()
        issuanceBanksSubBankEntryRepository.getByParentBankId(issuanceBank)?.forEach { data ->
            if (data.issuanceBanksId != issuanceBank) {
                issuanceBankList.add(data.issuanceBanksId)
            }
        }
        issuanceBankList.forEach { data ->
            val institutionRegisterData = IssuanceCommonController.InstitutionRegisterData(
                id = data.id
            )
            if (data.institutionId != null) {
                institutionRegisterData.institutionId = setInstitutionDetails(data.institutionId, data.id)
            }
            if (data.bankName != null) {
                institutionRegisterData.institutionName = setInstitutionDetails(data.bankName, data.id)
            }
            if (data.institutionAbbreviation != null) {
                institutionRegisterData.institutionAbbreviation = setInstitutionDetails(
                    data.institutionAbbreviation,
                    data.id
                )
            }
            if (data.institutionDescription != null) {
                institutionRegisterData.institutionDescription = setInstitutionDetails(
                    data.institutionDescription,
                    data.id
                )
            }
            if (data.bankLogo != null) {
                institutionRegisterData.institutionLogo = setInstitutionDetails(data.bankLogo, data.id)
            }
            if (data.institutionRegion != null) {
                institutionRegisterData.institutionRegion = setInstitutionDetails(data.institutionRegion, data.id)
            }
            if (data.timeZone != null) {
                institutionRegisterData.institutionTimeZone = setInstitutionDetails(data.timeZone, data.id)
            }
            if (data.defaultCurrency != null) {
                institutionRegisterData.defaultCurrency = setInstitutionDetails(data.defaultCurrency.toString(), data.id)
            }
            if (data.destinationFiatCurrency != null) {
                institutionRegisterData.destinationFiatCurrency = setInstitutionDetails(data.destinationFiatCurrency.toString(), data.id)
            }
            if (data.companyType != null) {
                institutionRegisterData.companyType = setInstitutionDetails(data.companyType, data.id)
            }
            if (data.industryType != null) {
                institutionRegisterData.industryType = setInstitutionDetails(data.industryType, data.id)
            }
            if (data.activationDate != null) {
                institutionRegisterData.activationDate = setInstitutionDetails(data.activationDate.toString(), data.id)
            }
            if (data.addressLine1 != null) {
                institutionRegisterData.addressLine1 = setInstitutionDetails(data.addressLine1, data.id)
            }
            if (data.addressLine2 != null) {
                institutionRegisterData.addressLine2 = setInstitutionDetails(data.addressLine2, data.id)
            }
            if (data.addressLine3 != null) {
                institutionRegisterData.addressLine3 = setInstitutionDetails(data.addressLine3, data.id)
            }
            if (data.city != null) {
                institutionRegisterData.city = setInstitutionDetails(data.city, data.id)
            }
            if (data.provice != null) {
                institutionRegisterData.provice = setInstitutionDetails(data.provice, data.id)
            }
            if (data.country != null) {
                institutionRegisterData.country = setInstitutionDetails(data.country, data.id)
            }
            if (data.postalCode != null) {
                institutionRegisterData.postalCode = setInstitutionDetails(data.postalCode, data.id)
            }
            if (data.primaryContactFirstName != null) {
                institutionRegisterData.primaryContactFirstName = setInstitutionDetails(
                    data.primaryContactFirstName,
                    data.id
                )
            }
            if (data.primaryContactMiddleName != null) {
                institutionRegisterData.primaryContactMiddleName = setInstitutionDetails(
                    data.primaryContactMiddleName,
                    data.id
                )
            }
            if (data.primaryContactLastName != null) {
                institutionRegisterData.primaryContactLastName = setInstitutionDetails(
                    data.primaryContactLastName,
                    data.id
                )
            }
            if (data.primaryContactEmailId != null) {
                institutionRegisterData.primaryContactEmailId = setInstitutionDetails(
                    data.primaryContactEmailId,
                    data.id
                )
            }
            if (data.primaryContactPhoneNumber != null) {
                institutionRegisterData.primaryContactPhoneNumber = setInstitutionDetails(
                    data.primaryContactPhoneNumber,
                    data.id
                )
            }
            if (data.primaryContactDesignation != null) {
                institutionRegisterData.primaryContactDesignation = setInstitutionDetails(
                    data.primaryContactDesignation,
                    data.id
                )
            }
            if (data.primaryContactDepartment != null) {
                institutionRegisterData.primaryContactDepartment = setInstitutionDetails(
                    data.primaryContactDepartment,
                    data.id
                )
            }
            /********   Set Admin Details  Start  **********/
            institutionRegisterData.adminFirstName = setInstitutionDetails(
                data.email,
                data.id
            )
            institutionRegisterData.adminMiddleName = setInstitutionDetails(
                data.email,
                data.id
            )
            institutionRegisterData.adminLastName = setInstitutionDetails(
                data.email,
                data.id
            )
            institutionRegisterData.adminEmailId = setInstitutionDetails(data.email, data.id)
            institutionRegisterData.adminCountryCode = setInstitutionDetails(data.countryCode, data.id)
            institutionRegisterData.adminPhoneNumber = setInstitutionDetails(
                data.phoneNumber,
                data.id
            )
            institutionRegisterData.adminDepartment = setInstitutionDetails(
                data.email,
                data.id
            )
            /********   Set Admin Details  Start  **********/
            if (data.customerOfflineTransaction != null) {
                institutionRegisterData.customerOfflineTransaction = setInstitutionDetails(
                    data.customerOfflineTransaction.toString(),
                    data.id
                )
            }
            if (data.merchantOfflineTransaction != null) {
                institutionRegisterData.merchantOfflineTransaction = setInstitutionDetails(
                    data.merchantOfflineTransaction.toString(),
                    data.id
                )
            }
            if (data.institutionStatus != null) {
                institutionRegisterData.institutionStatus = setInstitutionDetails(
                    data.institutionStatus.toString(),
                    data.id
                )
            }
            if (data.approvalWorkFlow != null) {
                institutionRegisterData.approvalWorkFlow = setInstitutionDetails(
                    data.approvalWorkFlow.toString(),
                    data.id
                )
            }
            if (data.p2pTransfer != null) {
                institutionRegisterData.p2pTransfer = setInstitutionDetails(data.p2pTransfer.toString(), data.id)
            }
            issuanceBankDataList.add(institutionRegisterData)
        }

        val pagination = IssuanceWalletService.Pagination(
            current_page = request.page,
            total_records = issuanceBankDataList.size,
            total_pages = issuanceWalletService.calculateTotalNoPages(issuanceBankDataList.size.toDouble(), request.limit.toDouble())
        )
        if (request.page != null && request.page > 0) {
            val pageNo = request.page - 1
            issuanceBankDataList =
                issuanceBankDataList.stream().skip(pageNo * request.limit).limit(request.limit).toList() as MutableList<IssuanceCommonController.InstitutionRegisterData>
        }
        return IssuanceCommonController.InstitutionResponse(
            issuanceBankDataList.size,
            issuanceBankDataList,
            pagination
        )
    }

    fun setInstitutionDetails(data: String?, id: Long): IssuanceCommonController.InstitutionRegisterObject? {
        val issuanceBank = issuanceBanksRepository.getById(id)
        if (data != null) {
            val replacedData = addUnderscoreInString(data)
            if (issuanceBank != null) {
                var configFieldData = institutionManagementFieldConfigRepository.getByIssuanceBanksId(issuanceBank)
            }
            return IssuanceCommonController.InstitutionRegisterObject(
                value = data,
                isMandatoryField = true,
                isShow = true,
                isEdit = true
            )
        }
        return null
    }

    fun addUnderscoreInString(s: String): String? {
        return s.replace(
            String.format(
                "%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ).toRegex(),
            "_"
        )
    }

    fun convertDate(dateS: String): Instant? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatterDate = formatter.parse(dateS)
        return formatterDate.toInstant()
    }

    fun runScheduler(userAccount: UserAccount): Boolean? {
        val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
        val feeConfigData =
            issuanceBanksUserEntry?.let { issuanceWalletConfigRepository.getByIssuanceBanksId(issuanceBanksId = it.issuanceBanksId) }
        val schedulerFee =
            feeConfigData?.filter { e -> e.isActive && e.walletFeeId == IssuanceCommonController.WalletFeeType.WF_003.walletFeeId }
        val dateNow = Instant.now()
        val formatterDate = SimpleDateFormat("yyyy-MM-dd")
        val dateNowFormat = formatterDate.format(Date.from(dateNow))
        schedulerFee?.forEach { tData ->
            val feeDataResponse: MutableList<IssuanceWalletUserController.FeeConfigDetails> = mutableListOf()
            if (tData.walletFeeId == IssuanceCommonController.WalletFeeType.WF_003.toString()) {
                var startDate = issuanceBanksUserEntry.createdAt
                var feeName: String? = null
                IssuanceCommonController.WalletFeeType.values().forEach { data ->
                    if (tData.walletFeeId == data.walletFeeId) {
                        feeName = data.walletFeeType
                    }
                }
                if (tData.frequency == IssuanceCommonController.Frequency.DAILY.frequencyId) {
                    val formatterTime = SimpleDateFormat("HH:mm:ss")
                    val startTime = formatterTime.format(Date.from(startDate))
                    val time: LocalTime = LocalTime.parse(startTime)
                    val todayDate = formatterDate.format(Date.from(Instant.now()))
                    val date: LocalDate = LocalDate.parse(todayDate)
                    var datetime: LocalDateTime = time.atDate(date)
                    var endDate = datetime.minus(24, ChronoUnit.HOURS)
                    if (endDate.atZone(ZoneId.systemDefault()).toInstant() < Instant.now()) {
                        endDate = datetime
                        datetime = endDate.plus(24, ChronoUnit.HOURS)
                    }
                    val isFeeDeducted = issuanceConfigurationService.checkIsFeeAlreadyDeducted(
                        endDate.atZone(ZoneId.systemDefault()).toInstant(),
                        datetime.atZone(ZoneId.systemDefault()).toInstant(),
                        tData.id,
                        userAccount,
                        feeName,
                        tData.frequency
                    )
                    if (isFeeDeducted != null && !isFeeDeducted) {
                        val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                            feeId = tData.id,
                            currencyType = issuanceConfigurationService.checkCurrencyUnit(
                                tData.fiatCurrency,
                                tData.walletFeeId,
                                tData.fiatCurrency
                            ),
                            feeNameId = tData.walletFeeId,
                            feeName = feeName,
                            feeType = issuanceConfigurationService.checkFixedOrPer(tData.feeValue),
                            feeAmount = issuanceConfigurationService.findValue(tData.feeValue, tData.fiatCurrency, tData.walletFeeId),
                            feeFrequency = tData.frequency,
                            feeMinimumAmount = issuanceConfigurationService.convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
                            feeMaximumAmount = issuanceConfigurationService.convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency),
                            feeDescription = issuanceConfigurationService.getFeeTransactionDescriptionByFrequency(
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant())),
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(datetime.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.DAYS))),
                                tData.frequency!!
                            )
                        )
                        feeDataResponse.add(feeConfigData)
                        calculateFeeForScheduler(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                    }
                }
                if (tData.frequency == IssuanceCommonController.Frequency.WEEKLY.toString()) {
                    var endDate = startDate.plus(7, ChronoUnit.DAYS)
                    for (i in 1..105) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(7, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val isFeeDeducted = issuanceConfigurationService.checkIsFeeAlreadyDeducted(
                        startDate,
                        endDate,
                        tData.id,
                        userAccount,
                        feeName,
                        tData.frequency
                    )
                    val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    if (isFeeDeducted != null && !isFeeDeducted && dateNowFormat == endDateFormatted) {
                        val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                            feeId = tData.id,
                            currencyType = issuanceConfigurationService.checkCurrencyUnit(
                                tData.fiatCurrency,
                                tData.walletFeeId,
                                tData.fiatCurrency
                            ),
                            feeNameId = tData.walletFeeId,
                            feeName = feeName,
                            feeType = issuanceConfigurationService.checkFixedOrPer(tData.feeValue),
                            feeAmount = issuanceConfigurationService.findValue(
                                tData.feeValue,
                                tData.fiatCurrency,
                                tData.walletFeeId
                            ),
                            feeFrequency = tData.frequency,
                            feeMinimumAmount = issuanceConfigurationService.convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
                            feeMaximumAmount = issuanceConfigurationService.convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency),
                            feeDescription = issuanceConfigurationService.getFeeTransactionDescriptionByFrequency(
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(startDate)),
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(endDate.minus(1, ChronoUnit.DAYS))),
                                tData.frequency!!
                            )
                        )
                        feeDataResponse.add(feeConfigData)
                        calculateFeeForScheduler(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                    }
                }
                if (tData.frequency == IssuanceCommonController.Frequency.MONTHLY.toString()) {
                    var endDate = startDate.plus(30, ChronoUnit.DAYS)
                    for (i in 1..24) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(30, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isFeeDeducted = issuanceConfigurationService.checkIsFeeAlreadyDeducted(
                        startDate,
                        endDate,
                        tData.id,
                        userAccount,
                        feeName,
                        tData.frequency
                    )
                    if (isFeeDeducted != null && !isFeeDeducted && dateNowFormat == endDateFormatted) {
                        val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                            feeId = tData.id,
                            currencyType = tData.fiatCurrency,
                            feeNameId = tData.walletFeeId,
                            feeName = feeName,
                            feeType = issuanceConfigurationService.checkFixedOrPer(tData.feeValue),
                            feeAmount = issuanceConfigurationService.findValue(
                                tData.feeValue,
                                tData.fiatCurrency,
                                tData.walletFeeId
                            ),
                            feeFrequency = tData.frequency,
                            feeMinimumAmount = tData.minValue,
                            feeMaximumAmount = tData.minValue,
                            feeDescription = issuanceConfigurationService.getFeeTransactionDescriptionByFrequency(
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(startDate)),
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(endDate.minus(1, ChronoUnit.DAYS))),
                                tData.frequency!!
                            )
                        )
                        feeDataResponse.add(feeConfigData)
                        calculateFeeForScheduler(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                    }
                }
                if (tData.frequency == IssuanceCommonController.Frequency.QUARTERLY.toString()) {
                    var endDate = startDate.plus(92, ChronoUnit.DAYS)
                    for (i in 1..8) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(92, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isFeeDeducted = issuanceConfigurationService.checkIsFeeAlreadyDeducted(
                        startDate,
                        endDate,
                        tData.id,
                        userAccount,
                        feeName,
                        tData.frequency
                    )
                    if (isFeeDeducted != null && !isFeeDeducted && dateNowFormat == endDateFormatted) {
                        val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                            feeId = tData.id,
                            currencyType = issuanceConfigurationService.checkCurrencyUnit(
                                tData.fiatCurrency,
                                tData.walletFeeId,
                                tData.fiatCurrency
                            ),
                            feeNameId = tData.walletFeeId,
                            feeName = feeName,
                            feeType = issuanceConfigurationService.checkFixedOrPer(tData.feeValue),
                            feeAmount = issuanceConfigurationService.findValue(
                                tData.feeValue,
                                tData.fiatCurrency,
                                tData.walletFeeId
                            ),
                            feeFrequency = tData.frequency,
                            feeMinimumAmount = issuanceConfigurationService.convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
                            feeMaximumAmount = issuanceConfigurationService.convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency),
                            feeDescription = issuanceConfigurationService.getFeeTransactionDescriptionByFrequency(
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(startDate)),
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(endDate.minus(1, ChronoUnit.DAYS))),
                                tData.frequency!!
                            )
                        )
                        feeDataResponse.add(feeConfigData)
                        calculateFeeForScheduler(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                    }
                }
                if (tData.frequency == IssuanceCommonController.Frequency.HALF_YEARLY.toString()) {
                    var endDate = startDate.plus(183, ChronoUnit.DAYS)
                    for (i in 1..4) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(183, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isFeeDeducted = issuanceConfigurationService.checkIsFeeAlreadyDeducted(
                        startDate,
                        endDate,
                        tData.id,
                        userAccount,
                        feeName,
                        tData.frequency
                    )
                    if (isFeeDeducted != null && !isFeeDeducted && dateNowFormat == endDateFormatted) {
                        val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                            feeId = tData.id,
                            currencyType = issuanceConfigurationService.checkCurrencyUnit(
                                tData.fiatCurrency,
                                tData.walletFeeId,
                                tData.fiatCurrency
                            ),
                            feeNameId = tData.walletFeeId,
                            feeName = feeName,
                            feeType = issuanceConfigurationService.checkFixedOrPer(tData.feeValue),
                            feeAmount = issuanceConfigurationService.findValue(
                                tData.feeValue,
                                tData.fiatCurrency,
                                tData.walletFeeId
                            ),
                            feeFrequency = tData.frequency,
                            feeMinimumAmount = issuanceConfigurationService.convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
                            feeMaximumAmount = issuanceConfigurationService.convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency),
                            feeDescription = issuanceConfigurationService.getFeeTransactionDescriptionByFrequency(
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(startDate)),
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(endDate.minus(1, ChronoUnit.DAYS))),
                                tData.frequency!!
                            )
                        )
                        feeDataResponse.add(feeConfigData)
                        calculateFeeForScheduler(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                    }
                }
                if (tData.frequency == IssuanceCommonController.Frequency.YEARLY.toString()) {
                    var endDate = startDate.plus(365, ChronoUnit.DAYS)
                    for (i in 1..2) {
                        if (endDate < Instant.now()) {
                            startDate = endDate
                            endDate = endDate.plus(365, ChronoUnit.DAYS)
                        } else {
                            break
                        }
                    }
                    val endDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDate.minus(1, ChronoUnit.DAYS)))
                    val isFeeDeducted = issuanceConfigurationService.checkIsFeeAlreadyDeducted(
                        startDate,
                        endDate,
                        tData.id,
                        userAccount,
                        feeName,
                        tData.frequency
                    )
                    if (isFeeDeducted != null && !isFeeDeducted && dateNowFormat == endDateFormatted) {
                        val feeConfigData = IssuanceWalletUserController.FeeConfigDetails(
                            feeId = tData.id,
                            currencyType = issuanceConfigurationService.checkCurrencyUnit(
                                tData.fiatCurrency,
                                tData.walletFeeId,
                                tData.fiatCurrency
                            ),
                            feeNameId = tData.walletFeeId,
                            feeName = feeName,
                            feeType = issuanceConfigurationService.checkFixedOrPer(tData.feeValue),
                            feeAmount = issuanceConfigurationService.findValue(
                                tData.feeValue,
                                tData.fiatCurrency,
                                tData.walletFeeId
                            ),
                            feeFrequency = tData.frequency,
                            feeMinimumAmount = issuanceConfigurationService.convertedAmount(tData.minValue, tData.walletFeeId, tData.fiatCurrency),
                            feeMaximumAmount = issuanceConfigurationService.convertedAmount(tData.maxValue, tData.walletFeeId, tData.fiatCurrency),
                            feeDescription = issuanceConfigurationService.getFeeTransactionDescriptionByFrequency(
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(startDate)),
                                SimpleDateFormat("dd MMMM yyyy").format(Date.from(endDate.minus(1, ChronoUnit.DAYS))),
                                tData.frequency!!
                            )
                        )
                        feeDataResponse.add(feeConfigData)
                        calculateFeeForScheduler(userAccount, feeDataResponse, issuanceBanksUserEntry.issuanceBanksId)
                    }
                }
            }
        }
        return true
    }

    fun calculateFeeForScheduler(
        userAccount: UserAccount,
        feeConfigData: MutableList<IssuanceWalletUserController.FeeConfigDetails>,
        issuanceBanksId: IssuanceBanks
    ): TransactionViewModel? {
        feeConfigData.forEach { fData ->
            val subAccount = userAccount.account.getSubAccountByAssetAlgoString(CurrencyUnit.SART.toString())
            val vubaSubaccount = subAccount?.let { ledgerService.getSubaccount(it.reference) }
            var balance = BigDecimal.ZERO
            if (vubaSubaccount != null) {
                if (vubaSubaccount.isPresent) {
                    balance = vubaSubaccount.get().balance
                }
            }
            // val balance = ledgerService.getSubaccount(userAccount.account.getSubaccountByAsset(CurrencyUnit.SART).reference).get().balance
            val feeAmountDeduction = issuanceGraphService.calculateFeeDeductionAmount(balance, fData)
            if (feeAmountDeduction > BigDecimal.ZERO && balance > feeAmountDeduction) {
                val feeConfigData = IssuanceCommonController.FeeConfigDataDetails(
                    feeId = fData.feeId,
                    enteredAmount = feeAmountDeduction,
                    feeAmount = fData.feeAmount,
                    feeCalculatedAmount = feeAmountDeduction,
                    feeName = fData.feeName,
                    feeType = fData.feeType.toString(),
                    currencyType = fData.currencyType,
                    description = fData.feeDescription
                )
                val feeData = mutableListOf<IssuanceCommonController.FeeConfigDataDetails>()
                feeData.add(feeConfigData)
                val serviceFeeRequest =
                    IssuanceCommonController.ServiceFeeRequest(
                        tokenAsset = CurrencyUnit.SART,
                        amount = feeAmountDeduction,
                        totalFeeApplied = feeAmountDeduction,
                        description = issuanceConfigurationService.getTransactionDescriptionByFrequency(fData.feeFrequency),
                        feeConfigData = feeData,
                        type = TransactionType.SERVICE_FEE
                    )
                return issuanceWalletService.serviceFeeDeduction(serviceFeeRequest, userAccount = userAccount, issuanceBanksId = issuanceBanksId)
            }
        }
        return null
    }

    fun runManuallyScheduledToAllUserAccount() {
        userAccountService.getUserAccountByKycVerified(VerificationStatus.APPROVED_VERIFIED)?.forEach { data ->
            runScheduler(data)
            issuanceConfigurationService.deductWalletFee(data)
        }
    }
    @Scheduled(cron = "0 0/30 * * * ?")
    @Transactional
    fun runServiceFeeScheduledToAllUserAccount() {
        userAccountService.getUserAccountByKycVerified(VerificationStatus.APPROVED_VERIFIED)?.forEach { data ->
            runScheduler(data)
        }
    }
    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional
    fun runWalletFeeScheduledToAllUserAccount() {
        userAccountService.getUserAccountByKycVerified(VerificationStatus.APPROVED_VERIFIED)?.forEach { data ->
            issuanceConfigurationService.deductWalletFee(data)
        }
    }

    fun saveTransactionLimitType(issuanceBank: IssuanceBanks, addTransactionLimitType: IssuanceCommonController.AddTransactionLimitType): IssuanceCommonController.TransactionTypeResponse? {
        val transactionType = issuanceTransactionTypeRepository.getByTransactionTypeId(addTransactionLimitType.transactionType.transactionId)
        var saveTransactionType = IssuanceTransactionType(
            transactionType = addTransactionLimitType.transactionName,
            transactionTypeId = addTransactionLimitType.transactionType.transactionId,
            isActive = true,
            issuanceBanksId = null
        )
        if (transactionType != null) {
            if (transactionType.transactionType != addTransactionLimitType.transactionName) {
                transactionType.transactionType = addTransactionLimitType.transactionName
                saveTransactionType = issuanceTransactionTypeRepository.save(transactionType)
            }
        } else {
            saveTransactionType = issuanceTransactionTypeRepository.save(saveTransactionType)
        }
        return IssuanceCommonController.TransactionTypeResponse(
            id = saveTransactionType.id,
            transactionTypeId = saveTransactionType.transactionTypeId,
            transactionType = saveTransactionType.transactionType
        )
    }

    fun saveWalletFeeType(issuanceBank: IssuanceBanks, addWalletFeeType: IssuanceCommonController.AddWalletFeeType): IssuanceCommonController.WalletFeeTypeResponse? {
        val walletFeeType = issuanceWalletFeeTypeRepository.getByWalletFeeId(addWalletFeeType.walletFeeType.walletFeeId)
        var saveWalletType = IssuanceWalletFeeType(
            feeType = addWalletFeeType.walletFeeName,
            walletFeeId = addWalletFeeType.walletFeeType.walletFeeId,
            isActive = true,
            issuanceBanksId = null
        )
        if (walletFeeType != null) {
            if (walletFeeType.feeType != addWalletFeeType.walletFeeName) {
                walletFeeType.feeType = addWalletFeeType.walletFeeName
                saveWalletType = issuanceWalletFeeTypeRepository.save(walletFeeType)
            }
        } else {
            saveWalletType = issuanceWalletFeeTypeRepository.save(saveWalletType)
        }
        return IssuanceCommonController.WalletFeeTypeResponse(
            id = saveWalletType.id,
            walletFeeId = saveWalletType.walletFeeId,
            feeType = saveWalletType.feeType
        )
    }

    fun validateCustomerOfflinePayment(
        type: IssuanceCommonController.TransactionLoadingType,
        transactionAmount: BigDecimal,
        userAccount: UserAccount,
        accountOwner: AccountOwner
    ): Boolean {
        val transactionLimit = checkTransactionLimitConfig(type, userAccount, accountOwner)
        if (transactionLimit != null) {
            if (transactionLimit.YEARLY != null &&
                (
                    (transactionLimit.YEARLY!!.availableCount != null && transactionLimit.YEARLY!!.availableCount!! <= 0) ||
                        (transactionLimit.YEARLY!!.remainingMaximumBalance != null && transactionLimit.YEARLY!!.remainingMaximumBalance!! < transactionAmount)
                    )
            ) {
                throw UnprocessableEntityException(ErrorCodes.YEARLY_LIMIT_EXHAUSTED)
            }
            if (transactionLimit.HALF_YEARLY != null &&
                (
                    (transactionLimit.HALF_YEARLY!!.availableCount != null && transactionLimit.HALF_YEARLY!!.availableCount!! <= 0) ||
                        (transactionLimit.HALF_YEARLY!!.remainingMaximumBalance != null && transactionLimit.HALF_YEARLY!!.remainingMaximumBalance!! < transactionAmount)
                    )
            ) {
                throw UnprocessableEntityException(ErrorCodes.HALF_YEARLY_LIMIT_EXHAUSTED)
            }
            if (transactionLimit.QUARTERLY != null &&
                (
                    (transactionLimit.QUARTERLY!!.availableCount != null && transactionLimit.QUARTERLY!!.availableCount!! <= 0) ||
                        (transactionLimit.QUARTERLY!!.remainingMaximumBalance != null && transactionLimit.QUARTERLY!!.remainingMaximumBalance!! < transactionAmount)
                    )
            ) {
                throw UnprocessableEntityException(ErrorCodes.QUARTERLY_LIMIT_EXHAUSTED)
            }
            if (transactionLimit.MONTHLY != null &&
                (
                    (transactionLimit.MONTHLY!!.availableCount != null && transactionLimit.MONTHLY!!.availableCount!! <= 0) ||
                        (transactionLimit.MONTHLY!!.remainingMaximumBalance != null && transactionLimit.MONTHLY!!.remainingMaximumBalance!! < transactionAmount)
                    )
            ) {
                throw UnprocessableEntityException(ErrorCodes.MONTHLY_LIMIT_EXHAUSTED)
            }
            if (transactionLimit.WEEKLY != null &&
                (
                    (transactionLimit.WEEKLY!!.availableCount != null && transactionLimit.WEEKLY!!.availableCount!! <= 0) ||
                        (transactionLimit.WEEKLY!!.remainingMaximumBalance != null && transactionLimit.WEEKLY!!.remainingMaximumBalance!! < transactionAmount)
                    )
            ) {
                throw UnprocessableEntityException(ErrorCodes.WEEKLY_LIMIT_EXHAUSTED)
            }
            if (transactionLimit.DAILY != null &&
                (
                    (transactionLimit.DAILY!!.availableCount != null && transactionLimit.DAILY!!.availableCount!! <= 0) ||
                        (transactionLimit.DAILY!!.remainingMaximumBalance != null && transactionLimit.DAILY!!.remainingMaximumBalance!! < transactionAmount)
                    )
            ) {
                throw UnprocessableEntityException(ErrorCodes.DAILY_LIMIT_EXHAUSTED)
            }
            if (transactionLimit.PER_TRANSACTION != null) {
                if (transactionLimit.PER_TRANSACTION!!.minimumBalance != null && transactionLimit.PER_TRANSACTION!!.maximumBalance != null) {
                    if (transactionLimit.PER_TRANSACTION!!.minimumBalance!! > transactionAmount || transactionLimit.PER_TRANSACTION!!.maximumBalance!! < transactionAmount) {
                        throw UnprocessableEntityException("Amount should be in between SAR* " + transactionLimit.PER_TRANSACTION!!.minimumBalance + " to SAR* " + transactionLimit.PER_TRANSACTION!!.maximumBalance + "")
                    }
                } else if (transactionLimit.PER_TRANSACTION!!.minimumBalance != null && transactionLimit.PER_TRANSACTION!!.minimumBalance!! > transactionAmount) {
                    throw UnprocessableEntityException("Amount should be greater then SAR* " + transactionLimit.PER_TRANSACTION!!.minimumBalance + " ")
                } else if (transactionLimit.PER_TRANSACTION!!.maximumBalance != null && transactionLimit.PER_TRANSACTION!!.maximumBalance!! < transactionAmount) {
                    throw UnprocessableEntityException("Amount should be less then SAR* " + transactionLimit.PER_TRANSACTION!!.maximumBalance + " ")
                } else {
                    return true
                }
            }
        }
        return true
    }

    fun updateConversionRatesValidTo(issuanceBank: IssuanceBanks): Boolean {
        val conversionRate = issuanceConversionRateRepository.getByIssuanceBanksId(issuanceId = issuanceBank)
        if (conversionRate != null) {
            val rateData = mutableListOf<IssuanceConversionRate>()
            conversionRate.forEach { data ->
                if (data.isActive) {
                    rateData.add(data)
                }
            }
            rateData.forEach { data ->
                val nextRate = findNextRate(rateData, data.validFrom)
                val nextEntry = nextRate.filter { e -> e.validFrom <= Instant.now() } as MutableList<IssuanceConversionRate>
                if (nextEntry.size > 0) {
                    val validTo = nextEntry[0].validFrom
                    if (validTo <= Instant.now()) {
                        data.isActive = false
                        data.validTo = validTo
                        issuanceConversionRateRepository.save(data)
                    }
                }
            }
            return true
        }
        return true
    }

    fun findNextRate(rateData: MutableList<IssuanceConversionRate>, validFrom: Instant): MutableList<IssuanceConversionRate> {
        return rateData.filter { e -> e.isActive && e.validFrom > validFrom } as MutableList<IssuanceConversionRate>
    }

    private fun updateConversionRatesAdjustmentValidTo(
        issuanceBank: IssuanceBanks,
        markType: IssuanceCommonController.MarkType?
    ): Boolean {
        val conversionRate = issuanceConversionRateAdjustmentRepository.getByIssuanceBanksId(issuanceBank)
        if (conversionRate != null) {
            val rateData = mutableListOf<IssuanceConversionRateAdjustment>()
            conversionRate.forEach { data ->
                if (data.isActive && data.type == markType) {
                    rateData.add(data)
                }
            }
            rateData.forEach { data ->
                val nextRate = findNextRateAdjustment(rateData, data.validFrom, markType)
                val nextEntry = nextRate.filter { e -> e.validFrom <= Instant.now() && e.type == markType } as MutableList<IssuanceConversionRateAdjustment>
                if (nextEntry.size > 0) {
                    val validTo = nextEntry[0].validFrom
                    if (validTo <= Instant.now()) {
                        data.isActive = false
                        data.validTo = validTo
                        issuanceConversionRateAdjustmentRepository.save(data)
                    }
                }
            }
            return true
        }
        return true
    }

    private fun findNextRateAdjustment(rateData: MutableList<IssuanceConversionRateAdjustment>, validFrom: Instant, markType: IssuanceCommonController.MarkType?): MutableList<IssuanceConversionRateAdjustment> {
        return rateData.filter { e -> e.isActive && e.validFrom > validFrom && e.type == markType } as MutableList<IssuanceConversionRateAdjustment>
    }

    fun updateIssuanceDetails(issuanceBanks: IssuanceBanks): IssuanceBanks {
        var isInstitutionIdUUID: Boolean = false
        if (issuanceBanks.institutionId != null) {
            isInstitutionIdUUID = try {
                UUID.fromString(issuanceBanks.institutionId)
                true
            } catch (exception: IllegalArgumentException) {
                false
            }
        }
        if (issuanceBanks.cognitoUsername != null && issuanceBanks.email != null && issuanceBanks.phoneNumber != null) {
            val isEmailsNotRegister = userAccountService.isEmailAvailable(issuanceBanks.email!!)
            if (isEmailsNotRegister) {
                userAccountService.createUserAccountIssuer(
                    issuanceBanks.cognitoUsername!!,
                    issuanceBanks.email!!,
                    issuanceBanks.phoneNumber!!,
                    issuanceBanks
                )
            }
        }
        if (!isInstitutionIdUUID) {
            issuanceBanks.institutionId = UUID.randomUUID().toString()
        }
        val updatedIssuanceBanks = issuanceBanksRepository.save(issuanceBanks)
        updatedIssuanceBanks.p2pTransfer = if (updatedIssuanceBanks.p2pTransfer != null) updatedIssuanceBanks.p2pTransfer else true
        return updatedIssuanceBanks
    }
}
