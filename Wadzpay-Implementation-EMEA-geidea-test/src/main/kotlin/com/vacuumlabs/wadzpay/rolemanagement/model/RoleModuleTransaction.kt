package com.vacuumlabs.wadzpay.rolemanagement.model

import org.hibernate.annotations.Type
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
@Entity
@Table(name = "role_modules_transaction")
data class RoleModuleTransaction(
    val roleId: Int = 0,
    @Type(type = "com.vacuumlabs.wadzpay.utils.PostgreSQLIntegerArrayType")
    @Column(name = "moduleId", columnDefinition = "integer[]")
    var moduleId: Array<Int>
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var roleModuleTransactionId: Long = 0
    var createdUpdatedBy: String = ""
    var createdUpdatedAt: Instant = Instant.now()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoleModuleTransaction

        if (roleId != other.roleId) return false
        if (!moduleId.contentEquals((other.moduleId))) return false
        if (roleModuleTransactionId != other.roleModuleTransactionId) return false
        if (createdUpdatedBy != other.createdUpdatedBy) return false
        if (createdUpdatedAt != other.createdUpdatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roleId
        result = 31 * result + moduleId.hashCode()
        result = 31 * result + roleModuleTransactionId.hashCode()
        result = 31 * result + createdUpdatedBy.hashCode()
        result = 31 * result + createdUpdatedAt.hashCode()

        return result
    }
}
@Repository
interface RoleModuleTransactionRepository : CrudRepository<RoleModuleTransaction, Int>
