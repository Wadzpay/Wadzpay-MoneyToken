package com.vacuumlabs.wadzpay.rolemanagement.service

import com.vacuumlabs.wadzpay.rolemanagement.model.LevelRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.Role
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModule
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModuleRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModuleTransaction
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModuleTransactionRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleTransaction
import com.vacuumlabs.wadzpay.user.UserAccountRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.NoSuchElementException
import javax.transaction.Transactional

@Service
class RoleModuleService(
    val roleModuleRepository: RoleModuleRepository,
    val roleModuleTransactionRepository: RoleModuleTransactionRepository,
    val userDetailsRepository: UserDetailsRepository,
    val userAccountRepository: UserAccountRepository,
    val levelRepository: LevelRepository
) {
    fun createRoleModule(roleModule: RoleModule): RoleModule {
        if (roleModule.createdBy == "" || roleModule.createdBy == null) {
            roleModule.updatedBy = userDetailsRepository.findAll().toList().last().userPreferenceId
            roleModule.createdBy = userDetailsRepository.findAll().toList().last().userPreferenceId
        }
        val currentTime = Instant.now()
        roleModule.createdAt = currentTime
        roleModule.updatedAt = currentTime
        val roleModuleSaved = roleModuleRepository.save(roleModule)
        val roleModuleTransaction = RoleModuleTransaction(roleModuleSaved?.roleId?.roleId!!, roleModuleSaved.moduleId)
        roleModuleTransaction.createdUpdatedBy = roleModuleSaved?.updatedBy!!

        roleModuleTransaction.createdUpdatedBy = roleModuleSaved.updatedBy!!
        roleModuleTransaction.createdUpdatedAt = currentTime
        // roleTransaction.imageUrl = roleSaved.imageUrl
        createRoleModuleTransaction(roleModuleTransaction)

        return roleModuleSaved
    }

    fun createRoleModuleTransaction(roleModuleTransaction: RoleModuleTransaction) {
        roleModuleTransactionRepository.save(roleModuleTransaction)
    }
    fun fetchRoles(): MutableList<Role> {
        return roleModuleRepository.findAll() as MutableList<Role>
    }
    fun fetchRoleTransaction(): MutableList<RoleTransaction> {
        return roleModuleTransactionRepository.findAll() as MutableList<RoleTransaction>
    }

    @Transactional
    fun updateRoleModule(roleModule: RoleDataService.RoleModulesRequest, role: Role): RoleModule {
        val roleModuleDb = roleModuleRepository.findByRoleId(role)
        try {
            roleModuleDb.moduleId = roleModule.module
/*
            roleModuleDb.roleId = roleModule.role.roleId
*/
            val currentTime = Instant.now()
            roleModuleDb.updatedAt = currentTime
            // userAccountRepository.findById()
            if (roleModule.role.updatedBy == "") { roleModuleDb.updatedBy = userDetailsRepository.findAll().toList().last().userPreferenceId }
            val roleModuleSaved = roleModuleRepository.save(roleModuleDb)
            val roleModuleTransaction = RoleModuleTransaction(roleModuleSaved.roleId?.roleId!!, roleModule.module)
            roleModuleTransaction.createdUpdatedBy = roleModuleSaved.updatedBy!!
            roleModuleTransaction.createdUpdatedAt = currentTime
            createRoleModuleTransaction(roleModuleTransaction)
            return roleModuleSaved
        } catch (ex: NoSuchElementException) {
            throw NoSuchElementException("No  Role  mapping found with Id   ${roleModule.role?.roleId}")
        }
    }
    fun deleteRole(roleModule: RoleModule) {
        roleModuleRepository.deleteById(roleModule.roleModuleId)
        /*val roleModuleTransaction = RoleModuleTransaction(roleModule.roleId, roleModule.moduleId,)
        createRoleModuleTransaction(roleModuleTransaction)
    */
    }
    fun getRoleModule(roleId: Role): RoleModule {
        return roleModuleRepository.findByRoleId(roleId)
    }
    fun buildRole(request: RoleDataService.RoleModulesRequest): Role {
        val level = levelRepository.findById(request.role.levelId)
            .orElseThrow { IllegalArgumentException("Level with id ${request.role.levelId} not found") }
        val role = Role(
            roleId = 0,
            roleName = request.role.roleName,
            levelId = level,
            aggregatorId = request.role.aggregatorId
        )
        return role
    }
}
