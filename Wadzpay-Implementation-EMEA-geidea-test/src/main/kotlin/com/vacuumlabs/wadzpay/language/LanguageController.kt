package com.vacuumlabs.wadzpay.language

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanksUserEntryRepository
import com.vacuumlabs.wadzpay.language.model.CountryViewModel
import com.vacuumlabs.wadzpay.language.model.LanguageMasterViewModel
import com.vacuumlabs.wadzpay.user.UserAccountService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/language")
@Tag(name = "Language Controller")
@Validated
class LanguageController(
    val userAccountService: UserAccountService,
    val languageService: LanguageService,
    val issuanceBanksUserEntryRepository: IssuanceBanksUserEntryRepository
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    @GetMapping("/getCountyList")
    @Operation(summary = "Get All Country List")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getCountyList(principal: Authentication): List<CountryViewModel> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        if (userAccount.issuanceBanks != null) {
            return languageService.getCountyList(userAccount.issuanceBanks!!)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }
    @PostMapping("/addLanguage")
    @Operation(summary = "Add Language ")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Language added successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "422",
            description = ErrorCodes.LANGUAGE_MAPPED_WITH_INSTITUTIONS,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun addLanguage(@RequestBody requestData: AddLanguageRequest, principal: Authentication): Any? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            if (requestData.languageName.isNullOrEmpty()) {
                throw EntityNotFoundException(ErrorCodes.NO_DATA_SENT)
            }
            return languageService.addLanguage(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/getAllLanguages")
    @Operation(summary = "Get Languages type on issuance portal.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get conversion rate"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getAllLanguages(principal: Principal):
        List<LanguageMasterViewModel> {
            val userAccount = userAccountService.getUserAccountByEmail(principal.name)
            if (userAccount.issuanceBanks != null) {
                return languageService.getAllLanguages(userAccount.issuanceBanks!!)
            } else {
                throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
            }
        }

    @PostMapping("/mappedLanguageInstitution")
    @Operation(summary = "mapped Language with institution")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Language added successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun mappedLanguageInstitution(@RequestBody requestData: List<MappedLanguageRequest>, principal: Authentication): MappedLanguageResponse? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            return languageService.mappedLanguageInstitution(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    @GetMapping("/getMappedLanguages")
    @Operation(summary = "Get Mapped Languages with issuer institution.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Get Mapped Languages"),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND} , ${ErrorCodes.ISSUANCE_BANK_NOT_FOUND}",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun getMappedLanguages(principal: Authentication): List<Any> {
        val userAccount = userAccountService.getUserAccountByEmail(principal.name)
        val issuanceBanks: IssuanceBanks = if (userAccount.issuanceBanks != null) {
            userAccount.issuanceBanks!!
        } else {
            val issuanceBanksUserEntry = issuanceBanksUserEntryRepository.getByUserAccountId(userAccount)
            issuanceBanksUserEntry?.issuanceBanksId ?: throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
        return languageService.getMappedLanguages(issuanceBanks)
    }

    @PostMapping("/makeLanguageDefault")
    @Operation(summary = "Make Language Default")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Language updated successfully."),
        ApiResponse(responseCode = "403", description = ErrorCodes.UNAUTHORIZED),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.USER_NOT_FOUND,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun makeLanguageDefault(@RequestBody requestData: MakeLanguageDefault, principal: Authentication): MappedLanguageResponse? {
        val issuerInstitution = userAccountService.getUserAccountByEmail(principal.name)
        if (issuerInstitution.issuanceBanks != null) {
            return languageService.makeLanguageDefault(issuerInstitution, requestData)
        } else {
            throw EntityNotFoundException(ErrorCodes.ISSUANCE_BANK_NOT_FOUND)
        }
    }

    data class AddLanguageRequest(
        val id: Long? = null,
        val isActive: Boolean? = null,
        /* language Name unique */
        val languageName: String?,
        /* language Display Name*/
        var languageDisplayName: String?,
        /* Country Name*/
        val countryId: Long? = null,
        /* s3 bucket resource File Url*/
        val resourceFileUrl: String?
    )

    data class MappedLanguageRequest(
        val id: Long? = null,
        val isActive: Boolean? = null,
        /* language Name unique */
        val languageId: Long?
    )

    data class MappedLanguageResponse(
        val status: String?,
    )

    data class MakeLanguageDefault(
        val isDefault: Boolean = false,
        /* language Name unique */
        val languageId: Long?
    )

    data class LanguageDisableErrorMessage(
        val errorMessage: String,
        val institutions: List<String>
    )
}
