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
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.groups.Default

@Entity
@Table(name = "entity_contact_details")
class EntityContactDetails(
    @JsonIgnore
    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
) {
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails FirstName")
    @field:NotBlank(message = "First Name  should not be empty", groups = [FullDataValidation::class])
    var entityContactDetailsFirstName: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails MiddleName")
    var entityContactDetailsMiddleName: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails LastName")
    @field:NotBlank(message = "Last Name  should not be empty", groups = [FullDataValidation::class])
    var entityContactDetailsLastName: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails EmailId")
    @field :NotBlank(message = "Email cannot be empty", groups = [FullDataValidation::class])
    @field:Email(message = "Email  is not valid", groups = [FullDataValidation::class, Default::class])
    var entityContactDetailsEmailId: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails MobileNumber")
    @field :NotBlank(message = "Mobile number cannot be empty", groups = [FullDataValidation::class])
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid", groups = [FullDataValidation::class, Default::class])
    var entityContactDetailsMobileNumber: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails Designation")
    var entityContactDetailsDesignation: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "ContactDetails Department")
    var entityContactDetailsDepartment: String? = null
}
@Repository
interface EntityContactDetailsRepository : CrudRepository<EntityContactDetails, Long>
