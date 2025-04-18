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
@Table(name = "department_transaction")
data class DepartmentTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val departmentTransactionId: Long = 0,
    var departmentId: Int,
    var departmentName: String,
    var createdUpdatedBy: Long?,
    var createdUpdatedAt: Instant = Instant.now(),
    var status: Boolean = true
)

@Repository
interface DepartmentTransactionRepository : CrudRepository<DepartmentTransaction, Long>
