package com.vacuumlabs.wadzpay.language.model

import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "language_master")
data class LanguageMaster(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var languageUuid: UUID,

    var languageName: String,

    var languageDisplayName: String? = null,

    var languageIconUrl: String ? = null,
    @ManyToOne
    @JoinColumn(name = "country", nullable = false)
    var country: Country,

    var resourceFileUrl: String? = null,
    var isActive: Boolean = true,

    val createdDate: Instant = Instant.now(),
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: IssuanceBanks ? = null,

    var modifiedDate: Instant? = null,
    @ManyToOne
    @JoinColumn(name = "modified_by")
    var modifiedBy: IssuanceBanks? = null
)
fun LanguageMaster.toViewModel(): LanguageMasterViewModel {
    return LanguageMasterViewModel(
        id,
        languageName,
        languageDisplayName,
        country.id,
        country.countryName,
        country.countryImageUrl,
        resourceFileUrl,
        isActive,
        null
    )
}
data class LanguageMasterViewModel(
    val id: Long,
    val languageName: String,
    val languageDisplayName: String?,
    val countryId: Long?,
    val countryName: String?,
    val countryImageUrl: String?,
    val resourceFileUrl: String?,
    val isActive: Boolean,
    var mappingData: MappingData ? = null,
    var institutionCount: Int = 0
)
data class MappingData(
    val mappingId: Long,
    val isDefault: Boolean,
)
@Repository
interface LanguageMasterRepository : CrudRepository<LanguageMaster, Long> {
    fun getByIdAndIsActive(id: Long, isActive: Boolean): LanguageMaster?

    fun getByLanguageNameAndCountryAndIsActive(languageName: String, country: Country, isActive: Boolean): List<LanguageMaster>?
    fun getById(id: Long): LanguageMaster?

    fun getByIsActive(isActive: Boolean): List<LanguageMaster>?
}
