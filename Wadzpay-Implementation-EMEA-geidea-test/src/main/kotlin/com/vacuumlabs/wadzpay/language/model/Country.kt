package com.vacuumlabs.wadzpay.language.model

import com.vacuumlabs.wadzpay.user.UserAccount
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
@Table(name = "country")
data class Country(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val countryCode: String,

    val countryName: String,

    val countryImageUrl: String,

    var isActive: Boolean? = true,
    val createdDate: Instant = Instant.now(),
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: UserAccount? = null,

    val modifiedDate: Instant? = null,
    @ManyToOne
    @JoinColumn(name = "modified_by")
    var modifiedBy: UserAccount? = null
)
fun Country.toViewModel(): CountryViewModel {
    return CountryViewModel(
        id,
        countryCode,
        countryName,
        countryImageUrl
    )
}
data class CountryViewModel(
    val countryId: Long,
    val countryCode: String,
    val countryName: String,
    val countryImageUrl: String?
)
@Repository
interface CountryRepository : CrudRepository<Country, Long> {
    fun getAllByIsActive(isActive: Boolean): List<Country>?

    fun getById(id: Long): Country?
}
