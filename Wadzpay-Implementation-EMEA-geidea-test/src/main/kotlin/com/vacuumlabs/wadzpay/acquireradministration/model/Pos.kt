package com.vacuumlabs.wadzpay.acquireradministration.model
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.opencsv.bean.CsvBindByName
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.groups.Default

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
data class Pos(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
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
    var outletPreferenceId: String,
    @Column(nullable = true)
    @field:NotBlank(message = "Pos unique  Id  must not be empty")
    var posUniqueId: String,
    @Column(nullable = true)
    @field:NotBlank(message = "Pos  Id  must not be empty")
    var posId: String,
    @Column(nullable = true)
    var posModel: String,
    @Column(nullable = true)
    var posManufacturer: String,
    @Column(nullable = true)
    var posSerialNum: Long,
    @Column(nullable = true)
    var posMacAddress: String,
    @Column(nullable = true)
    var posIPAddress: String,
    @Column(nullable = true)
    var posFirmwareVersion: String,
    @Column(nullable = true)
    var status: String,
    @Column(nullable = false)
    var isParentDeActivated: Boolean = false,
    @Column(nullable = false)
    var isParentClosed: Boolean = false,
    @Column(nullable = false)
    var isParentBlocked: Boolean = false,
    @Column(nullable = true)
    var posKey: String?
) {

/*    @JsonIgnore
    @ManyToOne(cascade = [CascadeType.ALL], optional = false)
    @JoinColumn(name = "outlet_id")
    lateinit var outlet: Outlet*/
}

@Repository
interface PosRepository : CrudRepository<Pos, Long> {
    fun findPosByPosId(posId: String): Pos?
    fun findByPosId(posId: String): Pos?
    fun getPosById(id: Long): Pos
/*
    fun deleteByOutlet(outlet: Outlet)
    fun deleteAllByOutlet(outlet: Outlet)
    fun findByOutlet(outlet: Outlet): MutableList<Pos>
*/
    @Query("FROM Pos p WHERE p.posUniqueId=:posUniqueId and p.aggregatorPreferenceId=:aggregatorPreferenceId and p.insitutionPreferenceId=:insitutionPreferenceId and p.merchantGroupPreferenceId=:merchantGroupPreferenceId and p.merchantAcquirerPreferenceId=:merchantAcquirerId and p.subMerchantPreferenceId=:subMerchantId and p.outletPreferenceId=:outletPreferenceId order by p.posId")
    fun findUniquePosByQuery(posUniqueId: String, aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerId: String, subMerchantId: String, outletPreferenceId: String): Pos?

    @Query("FROM Pos p WHERE p.aggregatorPreferenceId=:aggregatorPreferenceId and p.insitutionPreferenceId=:insitutionPreferenceId and p.merchantGroupPreferenceId=:merchantGroupPreferenceId and p.merchantAcquirerPreferenceId=:merchantAcquirerId and p.subMerchantPreferenceId=:subMerchantId and p.outletPreferenceId=:outletPreferenceId order by p.posId")

    fun findPosRecordsByAllMatchedCriteria(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerId: String, subMerchantId: String, outletPreferenceId: String): MutableList<Pos >
    @Query(
        "SELECT NEW com.vacuumlabs.wadzpay.acquireradministration.model.PosListing(p,o,sma,ma,mg, i, a)  \n" +
            "FROM Pos p \n" +
            "LEFT JOIN FETCH Outlet o ON o.outletId = p.outletPreferenceId\n" +
            "LEFT JOIN FETCH SubMerchantAcquirer sma ON sma.subMerchantAcquirerId = p .subMerchantPreferenceId\n" +
            "LEFT JOIN FETCH MerchantAcquirer ma ON p.merchantAcquirerPreferenceId = ma .merchantAcquirerId\n" +
            "LEFT JOIN FETCH MerchantGroup mg ON p.merchantGroupPreferenceId = mg .merchantGroupPreferenceId\n" +
            "LEFT JOIN FETCH Institution i ON p.insitutionPreferenceId = i .institutionId\n" +
            "LEFT JOIN FETCH Aggregator a ON p.aggregatorPreferenceId = a .aggregatorPreferenceId   WHERE  p.aggregatorPreferenceId=:aggregatorPreferenceId and p.insitutionPreferenceId=:insitutionPreferenceId  and p.merchantGroupPreferenceId=:merchantGroupPreferenceId" + " and  p.merchantAcquirerPreferenceId=:merchantAcquirerPreferenceId" + " and p.subMerchantPreferenceId=:subMerchantPreferenceId" + " " + " and p.outletPreferenceId=:outletPreferenceId"
    )
    fun findRecordsWithParent(aggregatorPreferenceId: String, insitutionPreferenceId: String, merchantGroupPreferenceId: String, merchantAcquirerPreferenceId: String, subMerchantPreferenceId: String, outletPreferenceId: String): MutableList<PosListing >
    fun findPosByOutletPreferenceId(outletPreferenceId: String): List<Pos>
}
