package com.vacuumlabs.wadzpay.issuance.models

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "issuance_banks_audit_logs")
data class IssuanceBanksAuditLogs(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    var columnName: String ? = null,

    var oldValue: String? = null,

    var newValue: String ? = null,

    var modifiedDate: Instant? = null,
    var isActive: Boolean = true,
    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: IssuanceBanks? = null
)
@Repository
interface IssuanceBanksAuditLogsRepository : CrudRepository<IssuanceBanksAuditLogs, Long>
