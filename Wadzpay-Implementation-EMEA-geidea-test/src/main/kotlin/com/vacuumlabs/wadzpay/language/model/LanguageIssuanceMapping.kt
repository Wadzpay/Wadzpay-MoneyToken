package com.vacuumlabs.wadzpay.language.model

import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "language_issuance_mapping")
data class LanguageIssuanceMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "language_id", nullable = false)
    var languageId: LanguageMaster,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    val createdDate: Instant = Instant.now(),
    var modifiedDate: Instant? = null,
    var isActive: Boolean? = true,
    var isDefault: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks,

    @ManyToOne
    @JoinColumn(name = "modified_by")
    var modifiedBy: IssuanceBanks? = null
)
fun LanguageIssuanceMapping.toViewModel(): LanguageIssuanceMappingViewModel {
    return LanguageIssuanceMappingViewModel(
        id,
        languageId.languageName,
        languageId.languageDisplayName,
        languageId.country.countryName,
        languageId.country.countryImageUrl,
        languageId.resourceFileUrl,
        isDefault
    )
}
data class LanguageIssuanceMappingViewModel(
    val mappedId: Long,
    val languageName: String,
    val languageDisplayName: String?,
    val countryName: String,
    val countryImageUrl: String?,
    val resourceFileURL: String?,
    val isDefault: Boolean
)
@Repository
interface LanguageIssuanceMappingRepository : CrudRepository<LanguageIssuanceMapping, Long> {
    fun getByLanguageId(languageId: LanguageMaster): List<LanguageIssuanceMapping>?

    fun getByIssuanceBanksIdAndIsActive(issuanceBanksId: IssuanceBanks, isActive: Boolean?): List<LanguageIssuanceMapping>?

    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): List<LanguageIssuanceMapping>?

    fun getByLanguageIdAndIssuanceBanksIdAndIsActive(languageId: LanguageMaster, issuanceBanksId: IssuanceBanks, isActive: Boolean?): List<LanguageIssuanceMapping>?

    fun getByLanguageIdAndIssuanceBanksId(languageId: LanguageMaster, issuanceBanksId: IssuanceBanks): List<LanguageIssuanceMapping>?
}
