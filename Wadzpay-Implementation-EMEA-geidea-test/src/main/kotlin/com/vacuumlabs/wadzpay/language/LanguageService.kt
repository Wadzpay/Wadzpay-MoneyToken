package com.vacuumlabs.wadzpay.language

import com.vacuumlabs.DEFAULT_LANGUAGES
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponseWithListData
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksRepository
import com.vacuumlabs.wadzpay.language.model.CountryRepository
import com.vacuumlabs.wadzpay.language.model.CountryViewModel
import com.vacuumlabs.wadzpay.language.model.LanguageIssuanceMapping
import com.vacuumlabs.wadzpay.language.model.LanguageIssuanceMappingRepository
import com.vacuumlabs.wadzpay.language.model.LanguageIssuanceMappingViewModel
import com.vacuumlabs.wadzpay.language.model.LanguageMaster
import com.vacuumlabs.wadzpay.language.model.LanguageMasterRepository
import com.vacuumlabs.wadzpay.language.model.LanguageMasterViewModel
import com.vacuumlabs.wadzpay.language.model.MappingData
import com.vacuumlabs.wadzpay.language.model.toViewModel
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.user.UserAccount
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * This service provides methods for add/fetch Language and also mapped with the issuer institution .
 */
@Service
class LanguageService(
    val languageMasterRepository: LanguageMasterRepository,
    val countryRepository: CountryRepository,
    val languageIssuanceMappingRepository: LanguageIssuanceMappingRepository,
    val issuanceBanksRepository: IssuanceBanksRepository
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun addLanguage(
        issuerInstitution: UserAccount,
        requestData: LanguageController.AddLanguageRequest
    ): Any? {
        if (requestData.id != null) {
            var languageMaster = languageMasterRepository.getById(requestData.id)
            if (languageMaster != null) {
                if (requestData.countryId != null) {
                    val country = countryRepository.getById(requestData.countryId)
                    if (languageMaster.country != country && country != null && requestData.languageName != null) {
                        val languageSaved = languageMasterRepository.getByLanguageNameAndCountryAndIsActive(
                            requestData.languageName, country, true
                        )
                        if (!languageSaved.isNullOrEmpty()) {
                            throw EntityNotFoundException(ErrorCodes.LANGUAGE_ALREADY_ADDED)
                        }
                    } else if (languageMaster.country == country && requestData.languageName != null && languageMaster.languageName != requestData.languageName) {
                        val languageSaved = languageMasterRepository.getByLanguageNameAndCountryAndIsActive(
                            requestData.languageName, country, true
                        )
                        if (!languageSaved.isNullOrEmpty()) {
                            throw EntityNotFoundException(ErrorCodes.LANGUAGE_ALREADY_ADDED)
                        }
                    }
                    languageMaster.country = country ?: languageMaster.country
                }
                if (requestData.isActive == false) {
                    val mappingData = languageIssuanceMappingRepository.getByLanguageId(languageMaster)
                    if (!mappingData.isNullOrEmpty()) {
                        val institutionList = mutableListOf<String>()
                        mappingData.forEach { data ->
                            if (data.isActive == true) {
                                institutionList.add(data.issuanceBanksId.email.toString())
                            }
                        }
                        if (institutionList.isNotEmpty()) {
                            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(ErrorResponseWithListData(HttpStatus.UNPROCESSABLE_ENTITY.value(), ErrorCodes.LANGUAGE_MAPPED_WITH_INSTITUTIONS, institutionList))
                        }
                    }
                }
                languageMaster.isActive = requestData.isActive ?: languageMaster.isActive
                languageMaster.languageName = requestData.languageName ?: languageMaster.languageName
                languageMaster.languageDisplayName =
                    requestData.languageDisplayName ?: languageMaster.languageDisplayName
                languageMaster.resourceFileUrl = requestData.resourceFileUrl ?: languageMaster.resourceFileUrl
                languageMaster.modifiedBy = issuerInstitution.issuanceBanks
                languageMaster.modifiedDate = Instant.now()
                println(languageMaster)
                languageMaster = languageMasterRepository.save(languageMaster)
            }
            return languageMaster?.toViewModel()
        } else {
            if (requestData.countryId != null) {
                val country = countryRepository.getById(requestData.countryId)
                if (country != null) {
                    val languageMaster = requestData.languageName?.let {
                        languageMasterRepository.getByLanguageNameAndCountryAndIsActive(
                            it, country, true
                        )
                    }
                    if (!languageMaster.isNullOrEmpty()) {
                        throw EntityNotFoundException(ErrorCodes.LANGUAGE_ALREADY_ADDED)
                    } else {
                        val addReq = requestData.languageName?.let {
                            LanguageMaster(
                                languageUuid = UUID.randomUUID(),
                                languageName = it,
                                languageDisplayName = requestData.languageDisplayName,
                                country = country,
                                resourceFileUrl = requestData.resourceFileUrl,
                                createdBy = issuerInstitution.issuanceBanks
                            )
                        }
                        return languageMasterRepository.save(addReq!!).toViewModel()
                    }
                }
            }
            throw EntityNotFoundException(ErrorCodes.NO_DATA_SENT)
        }
    }

    fun getAllLanguages(issuanceBanks: IssuanceBanks): List<LanguageMasterViewModel> {
        val languageData = languageMasterRepository.findAll()
        val resData = languageData.map {
            it.toViewModel()
        }
        resData.forEach { data ->
            val language = languageMasterRepository.getById(data.id)
            val languageMappedData =
                language?.let { languageIssuanceMappingRepository.getByLanguageIdAndIssuanceBanksIdAndIsActive(it, issuanceBanks, true) }
            if (!languageMappedData.isNullOrEmpty()) {
                val mappingData = MappingData(
                    mappingId = languageMappedData[0].id,
                    isDefault = languageMappedData[0].isDefault,
                )
                data.mappingData = mappingData
            }
            val mappingList = language?.let { languageIssuanceMappingRepository.getByLanguageId(it) }
            val institutionList = mutableListOf<String>()
            if (!mappingList.isNullOrEmpty()) {
                mappingList.forEach { tData ->
                    if (tData.isActive == true) {
                        institutionList.add(tData.issuanceBanksId.email.toString())
                    }
                }
            }
            data.institutionCount = institutionList.size
        }
        return resData.sortedWith(compareByDescending<LanguageMasterViewModel> { it.mappingData?.mappingId }.thenBy { it.languageName })
    }

    fun getCountyList(issuanceBanks: IssuanceBanks): List<CountryViewModel> {
        val countryList = countryRepository.getAllByIsActive(true)
        val countryData = mutableListOf<CountryViewModel>()
        if (countryList != null) {
            return countryList.map {
                it.toViewModel()
            }.sortedBy { it.countryId }
        }
        return countryData
    }

    fun mappedLanguageInstitution(
        issuerInstitution: UserAccount,
        requestDataList: List<LanguageController.MappedLanguageRequest>
    ): LanguageController.MappedLanguageResponse? {
        requestDataList.forEach { requestData ->
            val languageMaster = requestData.languageId?.let { languageMasterRepository.getByIdAndIsActive(it, true) }
            if (languageMaster != null) {
                val languageMappedData = languageIssuanceMappingRepository.getByLanguageIdAndIssuanceBanksId(
                    languageId = languageMaster,
                    issuerInstitution.issuanceBanks!!
                )
                if (!languageMappedData.isNullOrEmpty()) {
                    languageMappedData.forEach { data ->
                        if (requestData.isActive == true && data.isActive == true) {
                            // throw EntityNotFoundException(ErrorCodes.LANGUAGE_ALREADY_ADDED)
                        } else {
                            data.isActive = requestData.isActive
                            data.modifiedBy = issuerInstitution.issuanceBanks
                            data.modifiedDate = Instant.now()
                            languageIssuanceMappingRepository.save(data)
                        }
                    }
                } else {
                    val addReq = LanguageIssuanceMapping(
                        languageId = languageMaster,
                        issuanceBanksId = issuerInstitution.issuanceBanks!!,
                        createdBy = issuerInstitution.issuanceBanks!!
                    )
                    languageIssuanceMappingRepository.save(addReq)
                }
            }
        }
        return LanguageController.MappedLanguageResponse(
            status = TransactionStatus.SUCCESSFUL.toString()
        )
    }

    fun getMappedLanguages(
        issuanceBanks: IssuanceBanks
    ): List<Any> {
        val languageMappedData = languageIssuanceMappingRepository.getByIssuanceBanksIdAndIsActive(issuanceBanks, true)
        val resData = mutableListOf<LanguageIssuanceMappingViewModel>()
        println("languageMappedData ==> $languageMappedData")
        if (!languageMappedData.isNullOrEmpty()) {
            println("First if")
            languageMappedData.forEach { data ->
                resData.add(data.toViewModel())
            }
            return resData.sortedByDescending { list -> list.mappedId }
        } else {
            println("second if")
            val rateData = mutableListOf<LanguageMasterViewModel>()
            val languageMaster = languageMasterRepository.getByIsActive(true)
            println("languageMaster ==> $languageMaster")
            if (!languageMaster.isNullOrEmpty()) {
                languageMaster.forEach { data ->
                    if (data.languageName.equals(DEFAULT_LANGUAGES, true)) {
                        rateData.add(data.toViewModel())
                    }
                }
                return rateData
            }
        }
        return resData
    }

    fun makeLanguageDefault(
        issuerInstitution: UserAccount,
        requestData: LanguageController.MakeLanguageDefault
    ): LanguageController.MappedLanguageResponse? {
        val languageMaster = requestData.languageId?.let { languageMasterRepository.getById(it) }
        if (languageMaster != null) {
            val languageMapping = languageIssuanceMappingRepository.getByIssuanceBanksId(
                issuerInstitution.issuanceBanks!!
            )
            if (!languageMapping.isNullOrEmpty()) {
                languageMapping.forEach { data ->
                    data.isDefault = data.languageId == languageMaster
                    languageIssuanceMappingRepository.save(data)
                }
            }
            return LanguageController.MappedLanguageResponse(status = TransactionStatus.SUCCESSFUL.toString())
        } else {
            throw EntityNotFoundException(ErrorCodes.LANGUAGE_NOT_FOUND)
        }
    }

    fun getLanguages(institutionId: String): List<Any> {
        val issuanceBank = issuanceBanksRepository.getByInstitutionId(institutionId)
        if (issuanceBank != null) {
            return getMappedLanguages(issuanceBank)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
}
