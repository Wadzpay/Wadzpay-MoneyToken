package com.vacuumlabs.wadzpay.acquireradministration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.opencsv.bean.CsvBindByName
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
import javax.persistence.Table
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.groups.Default

@Entity
@Table(name = "aggregator")
data class Aggregator(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    @CsvBindByName
    @Column
    @field:NotBlank(message = "Aggregator Name must not be empty", groups = [FullDataValidation::class, Default::class])
    var aggregatorName: String? = null
    @CsvBindByName
    @field:NotBlank(message = "Aggregator Id must not be empty", groups = [FullDataValidation::class, Default::class])
    @Column(unique = true)
    var aggregatorPreferenceId: String? = null
    @CsvBindByName
    @Column
    var clientAggregatorPreferenceId: String? = null

    @CsvBindByName
    @Column(nullable = true)
    var aggregatorStatus: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName
    var aggregatorLogo: String? = null

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var address: EntityAddress = EntityAddress()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var adminDetails: EntityAdminDetails = EntityAdminDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var bankDetails: EntityBankDetails = EntityBankDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var contactDetails: EntityContactDetails = EntityContactDetails()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var info: EntityInfo = EntityInfo()

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @CsvRecurse
    @field:Valid
    var others: EntityOthers = EntityOthers()
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "Errors")
    var error: String? = null
    @JsonInclude()
    @Transient
    @CsvBindByName(column = "Errors")
    var type: String? = null
}

@Repository
interface AggregatorRepository : CrudRepository<Aggregator, Long> {
    fun getAggregatorById(id: Long): Aggregator
    @Query("select a from Aggregator a where a.others.id=:id")
    fun getAggregatorByOthersId(id: Long): Aggregator?
    fun findByAggregatorName(aggregator: String): Aggregator
    fun getAggregatorByAggregatorPreferenceId(aggregatorId: String): Aggregator
    fun findByAggregatorPreferenceId(aggregatorId: String): Aggregator?

    fun deleteByAggregatorPreferenceId(aggregatorId: String): Aggregator
}
