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
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
@Entity
@Table(name = "role_modules")
data class RoleModule(
    @OneToOne
    @JoinColumn(name = "role_id", unique = true, nullable = false)
    var roleId: Role,
    @Type(type = "com.vacuumlabs.wadzpay.utils.PostgreSQLIntegerArrayType")
    @Column(name = "moduleId", columnDefinition = "integer[]")

    var moduleId: Array<Int>

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var roleModuleId: Int = 0
    var createdBy: String? = null
    var createdAt: Instant = Instant.now()
    var updatedBy: String ? = null
    var updatedAt: Instant = Instant.now()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoleModule

        if (roleId != other.roleId) return false
        if (!moduleId.contentEquals(other.moduleId)) return false
        if (roleModuleId != other.roleModuleId) return false
        if (createdBy != other.createdBy) return false
        if (createdAt != other.createdAt) return false
        if (updatedBy != other.updatedBy) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roleId.hashCode()
        result = 31 * result + moduleId.contentHashCode()
        result = 31 * result + roleModuleId
        result = 31 * result + createdBy.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedBy.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
@Repository
interface RoleModuleRepository : CrudRepository<RoleModule, Int> {
    fun findByRoleId(roleId: Role): RoleModule
}
