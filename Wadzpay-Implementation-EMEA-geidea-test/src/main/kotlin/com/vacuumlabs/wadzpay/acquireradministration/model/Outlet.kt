package com.vacuumlabs.wadzpay.acquireradministration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvIgnore
import com.opencsv.bean.CsvRecurse
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
data class Outlet(
    @JsonIgnore
    @Id
    @CsvIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Aggregator  Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Aggregator Id")
    var aggregatorPreferenceId: String? = null,
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
    @CsvBindByName(column = "Sub Merchant Id")
    var subMerchantPreferenceId: String? = null,
    @Column(unique = true)
    @field:NotBlank(message = "Outlet  Id  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Outlet Id")
    var outletId: String? = null,
    @Column
    var clientOutletId: String? = null,
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Outlet  Name  must not be empty", groups = [FullDataValidation::class, Default::class])
    @CsvBindByName(column = "Outlet  Name")
    var outletName: String? = null,
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Status")
    var outletStatus: String? = null,
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Outlet Logo")
    var outletLogo: String? = null,
    @Column(nullable = false)
    var isParentBlocked: Boolean = false,
    @Column(nullable = false)
    var isParentDeActivated: Boolean = false,
    @Column(nullable = false)
    var isParentClosed: Boolean = false,
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var address: EntityAddress = EntityAddress(),
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var adminDetails: EntityAdminDetails = EntityAdminDetails(),
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var bankDetails: EntityBankDetails = EntityBankDetails(),
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var contactDetails: EntityContactDetails = EntityContactDetails(),
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var info: EntityInfo = EntityInfo(),
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var others: EntityOthers = EntityOthers()
) {
/*    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "outlet_id")
    @CsvIgnore
    var posList: MutableList<Pos> = mutableListOf()*/
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "Errors")
    var error: String? = null
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "type")
    var type: String? = null
}
@Repository
interface OutletRepository : CrudRepository<Outlet, Long> {
    fun getOutletById(id: Long): Outlet
    fun findByOutletId(outletId: String): Outlet?

    @Query("FROM Outlet o WHERE o.aggregatorPreferenceId=:aggregatorPreferenceId and o.insitutionPreferenceId=:insitutionPreferenceId and o.merchantGroupPreferenceId=:merchantGroupPreferenceId and o.merchantAcquirerPreferenceId=:merchantAcquirerId and o.subMerchantPreferenceId=:subMerchantId")
    fun findByUniqueId(
        aggregatorPreferenceId: String,
        insitutionPreferenceId: String,
        merchantGroupPreferenceId: String,
        merchantAcquirerId: String,
        subMerchantId: String
    ): MutableList<Outlet>
    @Query("FROM Outlet o WHERE o.aggregatorPreferenceId=:aggregatorPreferenceId and o.insitutionPreferenceId=:insitutionPreferenceId and o.merchantGroupPreferenceId=:merchantGroupPreferenceId and o.merchantAcquirerPreferenceId=:merchantAcquirerId and o.subMerchantPreferenceId=:subMerchantId order by o.outletId")
    fun findByUniqueIdOrdeOrderByOutletId(
        aggregatorPreferenceId: String,
        insitutionPreferenceId: String,
        merchantGroupPreferenceId: String,
        merchantAcquirerId: String,
        subMerchantId: String
    ): MutableList<Outlet>
    fun findBySubMerchantPreferenceId(subMerchantPreferenceId: String): MutableList<Outlet>?
    @Query(
        "SELECT NEW com.vacuumlabs.wadzpay.acquireradministration.model.OutletListing(o,sma,ma,mg, i, a)  \n" +
            "FROM Outlet o \n" +
            "LEFT JOIN FETCH SubMerchantAcquirer sma ON sma.subMerchantAcquirerId = o .subMerchantPreferenceId\n" +
            "LEFT JOIN FETCH MerchantAcquirer ma ON o.merchantAcquirerPreferenceId = ma .merchantAcquirerId\n" +
            "LEFT JOIN FETCH MerchantGroup mg ON o.merchantGroupPreferenceId = mg .merchantGroupPreferenceId\n" +
            "LEFT JOIN FETCH Institution i ON o.insitutionPreferenceId = i .institutionId\n" +
            "LEFT JOIN FETCH Aggregator a ON o.aggregatorPreferenceId = a .aggregatorPreferenceId   WHERE  o.aggregatorPreferenceId=:aggregatorPreferenceId and o.insitutionPreferenceId=:insitutionPreferenceId  and o.merchantGroupPreferenceId=:merchantGroupPreferenceId" + " and  o.merchantAcquirerPreferenceId=:merchantAcquirerPreferenceId" + " and o.subMerchantPreferenceId=:subMerchantPreferenceId"
    )
    fun findByRecordsWithParent(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerPreferenceId: String, subMerchantPreferenceId: String): MutableList<OutletListing >

    fun findByMerchantAcquirerPreferenceId(merchantPreferenceId: String): MutableList<Outlet>
    fun findByMerchantGroupPreferenceId(merchantGroupPreferenceId: String): MutableList<Outlet>
    fun findByInsitutionPreferenceId(insitutionPreferenceId: String): MutableList<Outlet>
    fun findByAggregatorPreferenceId(aggregatorPreferenceId: String): MutableList<Outlet>
    @Query("select o from Outlet o where o.others.id=:id")
    fun getOutletByOthersId(id: Long): Outlet?
}
