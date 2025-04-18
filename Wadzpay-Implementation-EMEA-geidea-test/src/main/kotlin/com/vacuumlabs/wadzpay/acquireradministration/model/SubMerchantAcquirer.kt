package com.vacuumlabs.wadzpay.acquireradministration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.opencsv.bean.CsvBindByName
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.groups.Default

@Entity
data class SubMerchantAcquirer(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Aggregator  Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Aggregator Id")
    var aggregatorPreferenceId: String? = null,
    @Column
    var clientSubMerchantAcquirerId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Institution  Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Institution Id")
    var insitutionPreferenceId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Merchant Group Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Merchant Group Id")
    var merchantGroupPreferenceId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Merchant  Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Merchant Id")
    var merchantAcquirerPreferenceId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Submerchant  Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Submerchant Id")
    var subMerchantAcquirerId: String? = null,
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Submerchant Name")
    @field:NotBlank(message = "Submerchant   Name must not be empty", groups = [FullDataValidation::class, Default::class])
    var subMerchantAcquirerName: String? = null,
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Status")
    var subMerchantAcquirerStatus: String? = null,
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Submerchant Logo")
    var subMerchantAcquirerLogo: String? = null,
    @Column(nullable = false)
    var isParentBlocked: Boolean = false,
    @Column(nullable = false)
    var isParentDeActivated: Boolean = false,
    @Column(nullable = false)
    var isParentClosed: Boolean = false,
    @Column(nullable = false)
    var systemGenerated: Boolean = false,
    @Column(nullable = true)
    var generatedFrom: String? = null,
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var address: EntityAddress = EntityAddress(),

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var adminDetails: EntityAdminDetails = EntityAdminDetails(),

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var bankDetails: EntityBankDetails = EntityBankDetails(),

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var contactDetails: EntityContactDetails = EntityContactDetails(),
    @field:Valid
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    var info: EntityInfo = EntityInfo(),

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @field:Valid
    var others: EntityOthers = EntityOthers(),
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "Errors")
    var error: String? = null,
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "type")
    var type: String? = null

)

@Repository
interface SubMerchantRepository : CrudRepository<SubMerchantAcquirer, Long> {
    fun getSubMerchantAcquirerBySubMerchantAcquirerId(subMerchantAcquirerId: Long): SubMerchantAcquirer

    fun findBySubMerchantAcquirerId(subMerchantAcquirerId: String): SubMerchantAcquirer?
    @Query("FROM SubMerchantAcquirer WHERE aggregatorPreferenceId=:aggregatorPreferenceId and insitutionPreferenceId=:insitutionPreferenceId and merchantGroupPreferenceId=:merchantGroupPreferenceId and merchantAcquirerPreferenceId=:merchantAcquirerId order by subMerchantAcquirerId")
    fun findByUniqueIdOrderOrderBySubMerchantAcquirerId(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerId: String): MutableList<SubMerchantAcquirer>
    @Query("FROM SubMerchantAcquirer WHERE aggregatorPreferenceId=:aggregatorPreferenceId and insitutionPreferenceId=:insitutionPreferenceId and merchantGroupPreferenceId=:merchantGroupPreferenceId and merchantAcquirerPreferenceId=:merchantAcquirerId")
    fun findByUniqueId(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerId: String): MutableList<SubMerchantAcquirer>
    fun findByMerchantAcquirerPreferenceId(merchantAcquirerPreferenceId: String): MutableList<SubMerchantAcquirer>?
    @Query(
        "SELECT NEW com.vacuumlabs.wadzpay.acquireradministration.model.SubMerchantAcquirerListing(sma,ma,mg, i, a)  \n" +
            "FROM SubMerchantAcquirer sma \n" +
            "LEFT JOIN FETCH MerchantAcquirer ma ON sma.merchantAcquirerPreferenceId = ma .merchantAcquirerId\n" +
            "LEFT JOIN FETCH MerchantGroup mg ON sma.merchantGroupPreferenceId = mg .merchantGroupPreferenceId\n" +
            "LEFT JOIN FETCH Institution i ON sma.insitutionPreferenceId = i .institutionId\n" +
            "LEFT JOIN FETCH Aggregator a ON sma.aggregatorPreferenceId = a .aggregatorPreferenceId   WHERE  sma.aggregatorPreferenceId=:aggregatorPreferenceId and sma.insitutionPreferenceId=:insitutionPreferenceId  and sma.merchantGroupPreferenceId=:merchantGroupPreferenceId" + " and  sma.merchantAcquirerPreferenceId=:merchantAcquirerPreferenceId"
    )

    fun findByRecordsWithParent(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerPreferenceId: String): MutableList<SubMerchantAcquirerListing>
    @Query("select sm from SubMerchantAcquirer sm where sm.others.id=:id")
    fun getSubMerchantAcquirerByOthersId(id: Long): SubMerchantAcquirer?
}
