package com.vacuumlabs.wadzpay.issuance.models

import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "issuance_banks")
data class IssuanceBanks(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var bankName: String ? = null,

    var countryCode: String? = null,

    var timeZone: String ? = null,

    var defaultCurrency: String ? = null,

    @Column(unique = true)
    var phoneNumber: String? = null,

    @Column(unique = true)
    var email: String? = null,

    @Column(unique = true)
    var cognitoUsername: String? = null,

    var updatedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val isActive: Boolean? = true,
    var bankLogo: String? = null,
    var institutionId: String? = null,
    var institutionAbbreviation: String? = null,
    var institutionDescription: String? = null,
    var institutionRegion: String? = null,
    @Enumerated(EnumType.STRING)
    var destinationFiatCurrency: FiatCurrencyUnit? = null,
    var companyType: String? = null,
    var industryType: String? = null,
    var activationDate: Instant? = null,
    var addressLine1: String? = null,
    var addressLine2: String? = null,
    var addressLine3: String? = null,
    var city: String? = null,
    var provice: String? = null,
    var country: String? = null,
    var postalCode: String? = null,
    var primaryContactFirstName: String? = null,
    var primaryContactMiddleName: String? = null,
    var primaryContactLastName: String? = null,
    var primaryContactEmailId: String? = null,
    var primaryContactPhoneNumber: String? = null,
    var primaryContactDesignation: String? = null,
    var primaryContactDepartment: String? = null,
    var customerOfflineTransaction: Boolean? = null,
    var merchantOfflineTransaction: Boolean? = null,
    var institutionStatus: Boolean? = null,
    var approvalWorkFlow: Boolean? = null,
    var p2pTransfer: Boolean? = null,
    @Enumerated(EnumType.STRING)
    var fiatCurrency: FiatCurrencyUnit? = null,

)
@Repository
interface IssuanceBanksRepository : CrudRepository<IssuanceBanks, Long> {

    fun getAllBy(): List<IssuanceBanks> ?
    fun getByEmail(email: String): IssuanceBanks?

    fun getById(issuanceId: Long): IssuanceBanks?

    fun getByInstitutionId(institutionId: String?): IssuanceBanks?
}
