package com.vacuumlabs.wadzpay.acquireradministration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.opencsv.bean.CsvBindByName
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Pattern
import javax.validation.groups.Default

@Entity
@Table(name = "entity_info")
class EntityInfo(
    @JsonIgnore
    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
) {

    @CsvBindByName(column = "Abbrevation")
    @Column(nullable = true, unique = true)
    var entityInfoAbbrevation: String? = null
    @CsvBindByName(column = "Description")
    @Column(nullable = true, unique = true)
    var entityInfoDescription: String? = null
    @CsvBindByName(column = "Logo")
    @Column(nullable = true, unique = true)
    var entityInfoLogo: String? = null
    @CsvBindByName(column = "Region")
    @Column(nullable = true, unique = true)
    var entityInfoRegion: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Timezone")
    @field:Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+\$", groups = [FullDataValidation::class, Default::class])
    var entityInfoTimezone: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Type")
    var entityInfoType: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Default Digital Currency")
    var entityInfoDefaultDigitalCurrency: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Base Fiat Currency")
    var entityInfoBaseFiatCurrency: String? = null
}

@Repository
interface EntityInfoRepository : CrudRepository<EntityInfo, Long>
