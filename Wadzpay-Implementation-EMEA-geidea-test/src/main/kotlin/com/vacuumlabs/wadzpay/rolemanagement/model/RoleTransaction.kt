package com.vacuumlabs.wadzpay.rolemanagement.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "role_transaction")
data class RoleTransaction(
    val roleId: Int = 0,
    var roleName: String,
    var levelId: Short = 0,
    var aggregatorId: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val roleTransactionId: Long = 0
    var roleComments: String = ""
    var createdUpdatedBy: String = ""
    var createdUpdatedAt: Instant = Instant.now()
    var status: Boolean? = null
}
@Repository
interface RoleTransactionRepository : CrudRepository<RoleTransaction, Long>
