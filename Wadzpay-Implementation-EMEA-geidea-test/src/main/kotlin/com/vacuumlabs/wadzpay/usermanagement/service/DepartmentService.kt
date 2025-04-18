package com.vacuumlabs.wadzpay.usermanagement.service

import com.vacuumlabs.wadzpay.common.DuplicateEntityException
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.usermanagement.dataclass.DepartmentData
import com.vacuumlabs.wadzpay.usermanagement.dataclass.DepartmentRequest
import com.vacuumlabs.wadzpay.usermanagement.model.Department
import com.vacuumlabs.wadzpay.usermanagement.model.DepartmentRepository
import com.vacuumlabs.wadzpay.usermanagement.model.DepartmentTransaction
import com.vacuumlabs.wadzpay.usermanagement.model.DepartmentTransactionRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.NoSuchElementException

@Service
class DepartmentService(
    val departmentRepository: DepartmentRepository,
    val departmentTransactionRepository: DepartmentTransactionRepository
) {
    /** Create a new department in the database, or throw a [DuplicateEntityException] if it already exists.
     *  Check for existence before update the department, if department id not exist then send error code [NoSuchElementException]
     *  Fetch all department with active status and send back as mutable list.
     */

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun fetchDepartment(): MutableIterable<DepartmentData> {
        val departmentList = mutableListOf<DepartmentData>()
        departmentRepository.getByStatus(true)?.forEach { data ->
            departmentList.add(
                DepartmentData(
                    departmentId = data.departmentId,
                    departmentName = data.departmentName,
                    status = data.status
                )
            )
        }
        return departmentList
    }

    fun createDepartment(userAccount: UserAccount, departmentRequest: DepartmentRequest): DepartmentData {
        val departmentAlreadyExist = departmentRepository.getByDepartmentNameIgnoreCase(departmentRequest.departmentName)
        if (departmentAlreadyExist != null && departmentAlreadyExist.status) {
            throw DuplicateEntityException("Department already exist with name ${departmentRequest.departmentName}")
        }
        println("currentTime ==> ${Instant.now()}")

        val department = Department(
            departmentName = departmentRequest.departmentName,
            status = true,
            createdBy = userAccount.id,
            updatedBy = userAccount.id,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        val departmentSaved = departmentRepository.save(department)
        val departmentTransaction = DepartmentTransaction(
            departmentId = departmentSaved.departmentId,
            departmentName = departmentSaved.departmentName,
            createdUpdatedAt = departmentSaved.updatedAt,
            createdUpdatedBy = departmentSaved.updatedBy,
            status = departmentSaved.status
        )
        createDepartmentTransaction(departmentTransaction)
        return DepartmentData(
            departmentId = departmentSaved.departmentId,
            departmentName = departmentSaved.departmentName,
            status = departmentSaved.status
        )
    }

    private fun createDepartmentTransaction(departmentTransaction: DepartmentTransaction) {
        departmentTransactionRepository.save(departmentTransaction)
    }

    fun updateDepartment(userAccount: UserAccount, departmentUpdateRequest: DepartmentData): DepartmentData {
        val departmentDb = departmentRepository.getByDepartmentId(departmentUpdateRequest.departmentId)
        try {
            val departmentAlreadyExist = departmentRepository.getByDepartmentNameIgnoreCase(departmentUpdateRequest.departmentName)
            if (departmentAlreadyExist != null && departmentAlreadyExist.departmentId != departmentUpdateRequest.departmentId) {
                throw DuplicateEntityException("Department already exist with name ${departmentUpdateRequest.departmentName}")
            }
            if (departmentDb != null) {
                departmentDb.departmentName = departmentUpdateRequest.departmentName
                if (departmentUpdateRequest.status != null) {
                    departmentDb.status = departmentUpdateRequest.status
                }
                departmentDb.updatedAt = Instant.now()
                departmentDb.updatedBy = userAccount.id
                val departmentSaved = departmentRepository.save(departmentDb)
                val departmentTransaction = DepartmentTransaction(
                    departmentId = departmentSaved.departmentId,
                    departmentName = departmentSaved.departmentName,
                    createdUpdatedAt = departmentSaved.updatedAt,
                    createdUpdatedBy = departmentSaved.updatedBy,
                    status = departmentSaved.status
                )
                createDepartmentTransaction(departmentTransaction)
                return DepartmentData(
                    departmentId = departmentSaved.departmentId,
                    departmentName = departmentSaved.departmentName,
                    status = departmentSaved.status
                )
            }
            throw NoSuchElementException("No  Department found with Id ${departmentUpdateRequest.departmentId}")
        } catch (ex: NoSuchElementException) {
            throw NoSuchElementException("No  Department found with Id ${departmentUpdateRequest.departmentId}")
        }
    }

    fun getByDepartmentIdAndStatus(departmentId: Int, status: Boolean): List<Department> {
        return departmentRepository.getByDepartmentIdAndStatus(departmentId, status)
    }
}
