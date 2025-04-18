package com.vacuumlabs.wadzpay.rolemanagement.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "modules_transaction")
data class ModuleTransaction(
    @Column(nullable = true, unique = true)
    val moduleId: Short = 0,
    @Column(nullable = true, unique = true)
    var moduleName: String?,
    @Column(nullable = true, unique = true)
    var moduleUrl: String?,

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val moduleTransactionId: Long = 0
    @Column(nullable = true, unique = true)
    var moduleType: String? = null
    @Column(nullable = true, unique = true)
    var imageUrl: String? = null
    @Column(nullable = true, unique = true)
    var parentId: Short? = null
    @Column(nullable = true)
    var parentName: String? = null
    @JsonIgnore
    var sorting: BigDecimal = BigDecimal("4")
    @Column(nullable = true, unique = true)
    var createdUpdatedAt: Instant? = Instant.now()
    @Column(nullable = true, unique = true)
    var createdUpdatedBy: Long? = 4
    @Column(nullable = true, unique = true)
    var status: Boolean? = null
}
@Repository
interface ModuleTransactionRepository : CrudRepository<ModuleTransaction, Long>
