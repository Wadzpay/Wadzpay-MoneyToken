package com.vacuumlabs.wadzpay.usermanagement.model

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "department")
data class Department(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val departmentId: Int = 0,

    var departmentName: String,

    var createdBy: Long?,

    var createdAt: Instant = Instant.now(),

    var updatedBy: Long?,

    var updatedAt: Instant = Instant.now(),

    var status: Boolean = true
)

@Repository
interface DepartmentRepository : CrudRepository<Department, Long> {
    fun getByStatus(status: Boolean): List<Department>?

    fun getByDepartmentIdAndStatus(departmentId: Int, status: Boolean): List<Department>
    fun getByDepartmentNameIgnoreCase(departmentName: String): Department?

    fun getByDepartmentId(departmentId: Int): Department?
}
