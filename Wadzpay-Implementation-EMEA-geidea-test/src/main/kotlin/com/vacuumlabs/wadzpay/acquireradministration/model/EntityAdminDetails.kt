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
@Table(name = "entity_admin_details")
class EntityAdminDetails(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    var id: Long = 0
) {

    @CsvBindByName(column = "Admin  FirstName")
    @Column(nullable = true, unique = true)
    @field:NotBlank(message = " Admin First Name  should not be empty", groups = [FullDataValidation::class])
    var entityAdminDetailsFirstName: String? = null
    @CsvBindByName(column = "Admin  MiddleName")
    @Column(nullable = true, unique = true)
    var entityAdminDetailsMiddleName: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Admin  LastName")
    @field:NotBlank(message = "Last Name  should not be empty", groups = [FullDataValidation::class])
    var entityAdminDetailsLastName: String? = null
    @CsvBindByName(column = "Admin  EmailId")
    @Column(nullable = true, unique = true)
    @field :NotBlank(message = "Email cannot be empty", groups = [FullDataValidation::class])
    @field:Email(message = "Email  is not valid", groups = [FullDataValidation::class, Default::class])
    var entityAdminDetailsEmailId: String? = null
    @CsvBindByName(column = "Admin  MobileNumber")
    @Column(nullable = true, unique = true)
    @field :NotBlank(message = "Mobile number cannot be empty", groups = [FullDataValidation::class])
    @field:Pattern(regexp = "^(\\d{10}|\\d{12}|\\d{11})\$", message = "Mobile number is  not valid", groups = [FullDataValidation::class, Default::class])
    var entityAdminDetailsMobileNumber: String? = null
    @CsvBindByName(column = "Admin  Department")
    @Column(nullable = true, unique = true)
    var entityAdminDetailsDepartment: String? = null
}

@Repository
interface EntityAdminDetailsRepository : CrudRepository<EntityAdminDetails, Long>
