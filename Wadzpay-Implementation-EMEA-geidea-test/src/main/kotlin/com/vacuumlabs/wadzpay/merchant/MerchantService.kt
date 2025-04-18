package com.vacuumlabs.wadzpay.merchant
import com.vacuumlabs.wadzpay.auth.encoders
import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnauthorizedException
import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.ledger.LedgerService
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.MerchantApiKey
import com.vacuumlabs.wadzpay.merchant.model.MerchantApiKeyRepository
import com.vacuumlabs.wadzpay.merchant.model.MerchantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Base64
import java.util.Properties
import java.util.UUID

@Service
class MerchantService(
    val merchantRepository: MerchantRepository,
    val merchantApiKeyRepository: MerchantApiKeyRepository,
    val ledgerService: LedgerService,
    val appConfig: AppConfig
) {

    fun issueCredentials(merchant: Merchant): CreateMerchantResponse {
        val merchantCredentials = issueNewApiKey(merchant)
        return CreateMerchantResponse.fromCredentials(merchantCredentials)
    }

    @Transactional
    fun createMerchant(request: CreateMerchantRequest): Merchant {
        if (merchantRepository.getByName(request.name) != null) {
            throw DuplicateEntityException(ErrorCodes.MERCHANT_ALREADY_EXISTS)
        }
        val merchant = with(request) {
            merchantRepository.save(Merchant(name, countryOfRegistration, registrationCode, primaryContactFullName, primaryContactEmail, primaryContactPhoneNumber, companyType, industryType, merchantId))
        }
        ledgerService.createAccounts(merchant)

        return merchant
    }

    @Transactional
    fun createMerchantTwo(request: CreateMerchantRequest): Merchant {
        if (merchantRepository.getByName(request.name) != null) {
            throw DuplicateEntityException(ErrorCodes.MERCHANT_ALREADY_EXISTS)
        }
        val merchant = with(request) {
            merchantRepository.save(Merchant(name, countryOfRegistration, registrationCode, primaryContactFullName, primaryContactEmail, primaryContactPhoneNumber, companyType, industryType, merchantId))
        }
        ledgerService.createAccountsTwo(merchant)

        return merchant
    }

    @Transactional
    fun issueNewApiKey(merchant: Merchant): MerchantCredentials {
        val password = UUID.randomUUID().toString()
        val merchantApiKey = MerchantApiKey(
            merchant = merchant,
            apiKeySecretHash = encoders.bcrypt.encode(password)
        )

        return MerchantCredentials(merchantApiKeyRepository.save(merchantApiKey).apiKeyId(), password)
    }

    fun invalidateApiKey(apiKeyId: String, owner: Merchant) {
        val apiKey = merchantApiKeyRepository.findAll().find { apiKeyId == it.apiKeyId() }
            ?: throw EntityNotFoundException(ErrorCodes.API_KEY_DOES_NOT_EXIST)

        if (apiKey.merchant != owner || !apiKey.valid) {
            throw UnauthorizedException(ErrorCodes.API_KEY_ALREADY_INVALID)
        }

        apiKey.valid = false
        merchantApiKeyRepository.save(apiKey)
    }

    fun getMerchantByName(name: String): Merchant {
        return merchantRepository.getByName(name) ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
    }

    fun findById(id: Long): Merchant {
        return merchantRepository.findById(id).orElseThrow { EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND) }
    }

    fun findByNameAndPrimaryContactEmail(userName: String, email: String): Merchant {
        return merchantRepository.findByNameAndPrimaryContactEmail(userName, email) ?: throw EntityNotFoundException(ErrorCodes.MERCHANT_NOT_FOUND)
    }

    fun setMerchantTimeZone(request: MerchantTimeZoneRequest, merchant: Merchant): Merchant {
        merchant.countryOfRegistration = request.countryId
        merchant.defaultTimeZone = request.timeZone
        return merchantRepository.save(merchant)
    }

    // Function to load merchant properties with language support
    fun getMerchantProperties(merchantName: String, lang: String?): Map<String, String> {
        val effectiveLang = lang ?: "English"
        val propertiesFileName = "${merchantName}_$effectiveLang.properties"
        val properties = loadProperties(propertiesFileName)

        return mapOf(
            "merchantName" to properties.getProperty("merchant.name"),
            "merchantLogo" to properties.getProperty("merchant.logo"),
            "merchantCurrency" to properties.getProperty("merchant.currency")
        )
    }

    // Generic function to load properties from a file
    private fun loadProperties(fileName: String): Properties {
        val properties = Properties()
        try {
            val inputStream = this.javaClass.classLoader.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("Property file '$fileName' not found in classpath")

            inputStream.use {
                properties.load(it)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle the exception if the file doesn't exist or can't be loaded
        }
        return properties
    }
}

data class MerchantCredentials(
    val apiKeyId: String,
    val apiKeySecret: String
) {
    companion object {
        fun fromToken(token: String): MerchantCredentials {
            val actualToken = token.substringAfter(' ')
            val decodedToken = String(Base64.getDecoder().decode(actualToken))
            return MerchantCredentials(
                decodedToken.substringBeforeLast(':'),
                decodedToken.substringAfterLast(':')
            )
        }
    }

    fun encode(): String {
        val credentials = Base64.getEncoder().encodeToString("$apiKeyId:$apiKeySecret".toByteArray())
        return "Basic $credentials"
    }

    fun getMerchantName(): String = apiKeyId.substringBeforeLast('_')
}
