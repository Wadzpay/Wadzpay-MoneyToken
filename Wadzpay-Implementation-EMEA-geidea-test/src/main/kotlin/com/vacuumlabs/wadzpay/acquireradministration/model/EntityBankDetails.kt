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
import javax.validation.constraints.Pattern
import javax.validation.groups.Default

@Entity
data class EntityBankDetails(
    @JsonIgnore
    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
) {

    @CsvBindByName(column = "Bank Name")
    @Column(nullable = true, unique = true)
    var entityBankDetailsBankName: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Bank Account Number")
    @field:Pattern(regexp = "^[A-Za-z0-9]*$", message = "Account number must  be aplhanumeric only", groups = [FullDataValidation::class, Default::class])
    var entityBankDetailsBankAccountNumber: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Bank Holder Name")
    var entityBankDetailsBankHolderName: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Bank Branch Code")
    var entityBankDetailsBranchCode: String? = null
    @Column(nullable = true, unique = true)
    @CsvBindByName(column = "Bank Branch Location")
    var entityBankDetailsBranchLocation: String? = null
}

@Repository
interface EntityBankDetailsRepository : CrudRepository<EntityBankDetails, Long>
