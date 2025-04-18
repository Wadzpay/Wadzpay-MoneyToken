package com.vacuumlabs.wadzpay.rolemanagement
import com.vacuumlabs.wadzpay.rolemanagement.model.Module
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleTransaction
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleTransactionRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementPermissions
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementPermissionsRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementRole
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementRoleRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementScreens
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementScreensRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementUser
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementUserRepository
import com.vacuumlabs.wadzpay.user.UserAccountRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RoleManagementService(
    val roleManagementPermissionsRepository: RoleManagementPermissionsRepository,
    val roleManagementRoleRepository: RoleManagementRoleRepository,
    val roleManagementUserRepository: RoleManagementUserRepository,
    val roleManagementScreensRepository: RoleManagementScreensRepository,
    val moduleRepository: ModuleRepository,
    val userAccountRepository: UserAccountRepository,
    val moduleTransactionRepository: ModuleTransactionRepository
) {
    fun createRoleManagementPermissions(roleManagementPermissions: RoleManagementPermissions) {
        roleManagementPermissionsRepository.save(roleManagementPermissions)
    }

    fun createRoleManagementRole(roleManagementRole: RoleManagementRole) {
        roleManagementRoleRepository.save(roleManagementRole)
    }

    fun createRoleManagementUser(roleManagementUser: RoleManagementUser) {
        roleManagementUserRepository.save(roleManagementUser)
    }

    fun createRoleManagementScreen(roleManagementScreens: RoleManagementScreens) {
        roleManagementScreensRepository.save(roleManagementScreens)
    }

    fun createModule(module: Module) {
        module.createdBy = userAccountRepository.findAll().toList().last().id
        module.updatedBy = userAccountRepository.findAll().toList().last().id
        val currentTime = Instant.now()
        module.createdAt = currentTime
        module.updatedAt = currentTime
        var moduleSaved = moduleRepository.save(module)
        val moduleTransaction = ModuleTransaction(moduleSaved.moduleId, moduleSaved.moduleName, moduleSaved.moduleUrl)
        moduleTransaction.createdUpdatedBy = moduleSaved.updatedBy
        moduleTransaction.createdUpdatedAt = currentTime
        if (moduleSaved.status != null) {
            moduleTransaction.status = moduleSaved.status!!
        }
        moduleTransaction.imageUrl = moduleSaved.imageUrl
        moduleTransaction.parentName = moduleSaved.parentName
        moduleTransaction.sorting = moduleTransaction.sorting
        moduleTransaction.parentId = moduleSaved.parent?.moduleId
        moduleTransaction.moduleType = moduleSaved.moduleType
        moduleTransactionOperation(moduleSaved)
    }
    fun listModule(): MutableList<Module> {
        var moduleList: MutableList<Module> =
            moduleRepository.findAll() as MutableList<Module>
        println(moduleList + "")
        println(moduleList.size.toString() + "")
        moduleList.sortWith(compareBy { it.moduleId })
        moduleList.map {
            it.updatedAt = it.updatedAt?.truncatedTo(ChronoUnit.MINUTES)
            it.createdAt = it.createdAt?.truncatedTo(ChronoUnit.MINUTES)
            it
        }
        return moduleList
    }

    fun updateModule(module: ModuleDataService.ModuleDataUpdate) {
        if (module.activate == null) {
            var updateModuleModule =
                moduleRepository.getByModuleId(module.moduleId)
            updateModuleModule.moduleName = module.moduleName
            updateModuleModule.parentName = module.parentName
            updateModuleModule.moduleType = module.moduleType
            updateModuleModule.moduleUrl = module.moduleUrl
            updateModuleModule.imageUrl = module.imageUrl
            // updateModuleModule.parent?.moduleId = module.parent!!
            updateModuleModule.sorting = module.sorting
//        updateModuleModule.createdAt = module.createdAt
//        updateModuleModule.createdBy = module.createdBy
            updateModuleModule.updatedAt = module.updatedAt
            updateModuleModule.updatedBy = module.updatedBy
            // val employeeToUpdate: Employee = entityManager.find(Employee::class.java, employeeId)
            if (module?.parent != null) {
                val parent: Module = moduleRepository.getByModuleId(module.parent!!)
                updateModuleModule.parent = parent
            }

            // Save the updated employee entity

            // Save the updated employee entity

            if (module.status == true) {
                updateModuleModule.status = module.status
            } else if (module.status == false) {
                updateModuleModule.status = module.status
            } else {
                println("status value is null")
            }
            var rmModuleSaved = moduleRepository.save(updateModuleModule)
            moduleTransactionOperation(rmModuleSaved)
        } else {
            val moduleDb = moduleRepository.getByModuleId(module.moduleId)
            moduleDb.status = module.status
            var rmModuleSaved = moduleRepository.save(moduleDb)
            moduleTransactionOperation(rmModuleSaved)
        }
    }

    fun deleteModule(module: Module) {
//        moduleRepository.deleteById(module.moduleId)
        var deleteRM = moduleRepository.getByModuleId(module.moduleId)
        moduleRepository.delete(deleteRM)
    }

    fun fetchModuleRecord(moduleId: Short): Module {
        return moduleRepository.getByModuleId(moduleId)
    }
    fun moduleTransactionOperation(
        module: Module,
    ) {
        val moduleTransaction = ModuleTransaction(
            moduleId = module.moduleId,
            moduleName = module.moduleName,
            moduleUrl = module.moduleUrl
        )
        moduleTransaction.moduleType = module.moduleType
        moduleTransaction.moduleUrl = module.moduleUrl
        moduleTransaction.imageUrl = module.imageUrl
        moduleTransaction.parentName = module.parentName
        moduleTransaction.parentId = module.parent?.moduleId
        moduleTransaction.sorting = module.sorting
        moduleTransaction.createdUpdatedAt = module.updatedAt
        moduleTransaction.createdUpdatedBy = module.updatedBy
        moduleTransaction.status = module.status

        createModuleTransaction(moduleTransaction)
    }

    fun createModuleTransaction(moduleTransaction: ModuleTransaction) {
        moduleTransactionRepository.save(moduleTransaction)
    }

    fun buildModuleTree(): List<Module> {
        val rootCategories = moduleRepository.findAllByParentModuleIdIsNull()

        val tree = rootCategories.map {
            it.key = it.moduleId
            it.name = it.moduleName
            buildModuleTree(it)
        }
        return tree
    }
    private fun buildModuleTree(module: Module): Module {
        module.key = module.moduleId
        module.name = module.moduleName
        val roleModules = moduleRepository.findAllByParentId(module.moduleId)
        roleModules.forEach {
            if (!module.children.contains(it)) {
                it.key = it.moduleId
                it.name = it.name
                module.children.add(it)
            }
        }
        // module.children.addAll(moduleRepository.findAllByParentId(module.moduleId))
        module.children.forEach {
            buildModuleTree(it)
        }
        return module
    }

    fun getModuleModuleId(moduleId: Short): Module? {
        val moduleData = moduleRepository.getByModuleIdAndStatus(moduleId, true)
        return if (!moduleData.isNullOrEmpty()) moduleData[0] else null
    }
}

data class ResponseModuleListByParent(
    val key: String,
    val name: String,
    val moduleList: MutableList<Module>?,
)
