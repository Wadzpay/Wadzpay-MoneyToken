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
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "entity_address")
data class EntityAddress(
    @JsonIgnore
    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
) {

    @CsvBindByName(column = "Address Line1")
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Address  Line1  must not be empty", groups = [FullDataValidation::class])
    var entityAddressAddressLine1: String? = null
    @CsvBindByName(column = "Address Line2")
    @Column(nullable = true, unique = true)
    var entityAddressAddressLine2: String? = null
    @CsvBindByName(column = "Address Line3")
    @Column(nullable = true, unique = true)
    var entityAddressAddressLine3: String? = null
    @CsvBindByName(column = "City",)
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "City should not be empty", groups = [FullDataValidation::class])
    var entityAddressCity: String? = null
    @CsvBindByName(column = "State")
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "State should not be empty", groups = [FullDataValidation::class])
    var entityAddressState: String? = null
    @CsvBindByName(column = "Country")
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Country should not be empty", groups = [FullDataValidation::class])
    var entityAddressCountry: String? = null
    @CsvBindByName(column = "Postal Code")
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = "Postal code should not be empty", groups = [FullDataValidation::class])

    var entityAddressPostalCode: String? = null
}
@Repository
interface EntityAddressRepository : CrudRepository<EntityAddress, Long>
