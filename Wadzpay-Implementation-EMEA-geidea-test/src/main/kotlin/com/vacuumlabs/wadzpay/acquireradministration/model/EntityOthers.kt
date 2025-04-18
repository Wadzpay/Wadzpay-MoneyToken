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

@Entity
@Table(name = "entity_others")
class EntityOthers(
    @JsonIgnore
    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
) {
    //
    @CsvBindByName(column = "Customer Offline Txn")
    @Column(nullable = true, unique = true)
    var entityOthersCustomerOfflineTxn: String? = null
    @CsvBindByName(column = "Merchant Offline Txn")
    @Column(nullable = true, unique = true)
    var entityOthersMerchantOfflineTxn: String? = null
    @CsvBindByName(column = "Approval WorkFlow")
    @Column(nullable = true, unique = true)
    var entityOthersApprovalWorkFlow: String? = null
    @CsvBindByName(column = "Activation Date")
    @Column(nullable = true)
    var entityOthersActivationDate: String? = null
    @Column(nullable = true)
    var entityOthersExpiryDate: String? = null
}

@Repository
interface EntityOthersRepository : CrudRepository<EntityOthers, Long> {
    fun getEntityOthersById(id: Long): EntityOthers
}
