package com.vacuumlabs.wadzpay.rolemanagement.service

import com.vacuumlabs.wadzpay.rolemanagement.RoleManagementService
import com.vacuumlabs.wadzpay.rolemanagement.model.LevelRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.Module
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleData
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.Role
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleListing
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleListingModule
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModule
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModuleRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModuleTransaction
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModuleTransactionRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleRepository
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleTransaction
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleTransactionRepository
import com.vacuumlabs.wadzpay.user.UserAccountRepository
import com.vacuumlabs.wadzpay.usermanagement.dataclass.StatusEnum
import com.vacuumlabs.wadzpay.usermanagement.model.Department
import com.vacuumlabs.wadzpay.usermanagement.model.DepartmentRepository
import com.vacuumlabs.wadzpay.usermanagement.model.LevelUserDetailsMapping
import com.vacuumlabs.wadzpay.usermanagement.model.LevelUserDetailsMappingRepository
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetails
import com.vacuumlabs.wadzpay.usermanagement.model.UserDetailsRepository
import com.vacuumlabs.wadzpay.usermanagement.service.UserDetailsService
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.lang.Math.ceil
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.NoSuchElementException
import javax.transaction.Transactional
import kotlin.streams.toList
@Service
class RoleService(
    val roleRepository: RoleRepository,
    val roleTransactionRepository: RoleTransactionRepository,
    val userAccountRepository: UserAccountRepository,
    val userDetailsRepository: UserDetailsRepository,
    val roleModuleService: RoleModuleService,
    val roleManagementService: RoleManagementService,
    @Lazy val userDetailsService: UserDetailsService,
    val levelRepository: LevelRepository,
    val moduleRepository: ModuleRepository,
    val roleModuleRepository: RoleModuleRepository,
    val departmentRepository: DepartmentRepository,
    val levelUserDetailsMappingRepository: LevelUserDetailsMappingRepository,
    val roleModuleTransactionRepository: RoleModuleTransactionRepository
) {
    @Transactional
    fun createRole(role: Role): Role {
        if (role.createdBy == "" || role.createdBy == null) {
            role.updatedBy = userDetailsRepository.findAll().toList().last().userPreferenceId
            role.createdBy = userDetailsRepository.findAll().toList().last().userPreferenceId
        }
        val currentTime = Instant.now()
        role.createdAt = currentTime
        role.updatedAt = currentTime
        val roleSaved = roleRepository.save(role)

        // roleTransaction.imageUrl = roleSaved.imageUrl
        createRoleTransaction(roleSaved, currentTime)

        return roleSaved
    }
    @Transactional
    fun initRoleAndUser() {
        val level = levelRepository.findByLevelNumber(0)
        val role = Role(
            0,
            roleName = "Admin",
            levelId = level!!,
            aggregatorId = "1"
        )
        role.createdAt = Instant.now()
        role.updatedBy = null
        role.createdBy = null
        role.updatedAt = Instant.now()
        role.status = true
        val savedRole = roleRepository.save(role)
        val modules: List<Module> = moduleRepository.findAll().toList()
        val modulesRefined = modules.map { it -> it.moduleId.toInt() }.toTypedArray()
        val roleModule = RoleModule(savedRole, modulesRefined)
        roleModule.createdBy = null
        roleModule.updatedBy = null
        val savedModule = roleModuleRepository.save(roleModule)
        val userAccount = userAccountRepository.findAll().last()
        val dept = Department(0, "Admin", userAccount.id, Instant.now(), userAccount.id, Instant.now(), true)
        departmentRepository.save(dept)
        val user = UserDetails(
            userId = 0,
            name = "john.doe",
            userPreferenceId = "super-admin",
            countryCode = "91",
            mobileNo = "9885524789",
            emailId = "admin@wadzpay.com",
            "admin",
            dept,
            savedRole,
            StatusEnum.ACTIVE,
            Instant.now(),
            null,
            0,
            null,
            Instant.now(),
            null,
            Instant.now(),
            null
        )
        val savedUser = userDetailsRepository.save(user)
        userDetailsService.createUserDetailsTransaction(savedUser)
        val levelUserMapping = LevelUserDetailsMapping(
            userId = savedUser,
            levelId = level,
            createdBy = savedUser.createdBy?.userId,
            createdAt = Instant.now()
        )
        levelUserDetailsMappingRepository.save(levelUserMapping)
        savedRole.createdBy = savedUser.userPreferenceId
        savedRole.updatedBy = savedUser.userPreferenceId
        savedModule.createdBy = savedUser.userPreferenceId
        savedModule.updatedBy = savedUser.userPreferenceId
        savedModule.createdAt = Instant.now()
        savedRole.createdAt = Instant.now()
        val roleModuleUpdated = roleModuleRepository.save(savedModule)
        savedRole.createdAt = roleModuleUpdated.createdAt
        val roleModuleTransaction = RoleModuleTransaction(roleModuleUpdated?.roleId?.roleId!!, roleModuleUpdated.moduleId)
        roleModuleTransaction.createdUpdatedAt = roleModuleUpdated.updatedAt
        roleModuleTransaction.createdUpdatedBy = roleModuleUpdated.updatedBy!!
        roleRepository.save(savedRole)
        roleModuleTransactionRepository.save(roleModuleTransaction)
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
        role.roleComments = request.role.roleComments
        return role
    }

    fun createRoleTransaction(roleSaved: Role, currentTime: Instant) {
        val roleTransaction = RoleTransaction(roleSaved.roleId, roleSaved.roleName, roleSaved.levelId.levelId, roleSaved.aggregatorId)
        roleTransaction.createdUpdatedBy = roleSaved.updatedBy!!
        roleTransaction.createdUpdatedAt = currentTime
        roleTransaction.roleComments = roleSaved.roleComments
        if (roleSaved.status != null) {
            roleTransaction.status = roleSaved.status!!
        }
        roleTransactionRepository.save(roleTransaction)
    }
    fun fetchRolesListing(level: Int, aggregatorId: String): MutableList<RoleListing> {
        val sort = Sort.by("roleId").ascending()
        return roleRepository.findByRecordsWithModuleRelation(level.toShort(), aggregatorId, sort) as MutableList<RoleListing>
    }
    fun fetchRoleTransaction(): MutableList<RoleTransaction> {
        return roleTransactionRepository.findAll() as MutableList<RoleTransaction>
    }
    /*fun updateRoleModel(roleModule: RoleDataService.RoleModulesRequest): Role {
        val roleSaved = updateRole(roleModule)
        createRoleTransaction(roleSaved, Instant.now())
        return roleSaved
    }*/
    @Transactional
    fun updateRole(request: RoleDataService.RoleModulesRequest): Role {
        val roleDb = roleRepository.findById(request.role.roleId).get()
        roleDb.roleName = request.role.roleName
        roleDb.roleComments = request.role.roleComments
        if (request.role.status != null) { roleDb.status = request.role.status!! }
        val currentTime = Instant.now()
        roleDb.updatedAt = currentTime
        // userAccountRepository.findById()
        if (request.role.updatedBy == "") { roleDb.updatedBy = userDetailsRepository.findAll().toList().last().userPreferenceId }
        try {
            val roleSaved = roleRepository.save(roleDb)
            val roleTransaction = RoleTransaction(roleSaved.roleId, roleSaved.roleName, roleSaved.levelId.levelId, roleSaved.aggregatorId)
            /* roleTransaction.createdUpdatedBy = roleSaved.updatedBy
             roleTransaction.createdUpdatedAt = currentTime
             if(roleSaved.status != null){
                 roleTransaction.status = roleSaved.status!!
             }
             roleTransaction.roleComments = roleSaved.roleComments*/
            createRoleTransaction(roleSaved, currentTime)
            return roleSaved
        } catch (ex: NoSuchElementException) {
            throw NoSuchElementException("No  Role found with Id ${request.role.roleId}")
        }
    }
    fun deleteRole(role: Role) {
        roleRepository.deleteById(role.roleId)
        val roleTransaction = RoleTransaction(role.roleId, role.roleName, role.levelId.levelId, role.aggregatorId)
        //  createRoleTransaction(roleTransaction)
    }
    fun fetchRolesListingFilters(request: RoleDataService.GetRoleListRequest): MutableList<RoleListing> {
        val sort = Sort.by("roleId").ascending()
        var roleListingFilter = roleRepository.findByRecordsWithModuleRelation(request.currentLevel?.toShort(), request.aggregatorId, sort)
        if (request.roleId != null) {
            return roleListingFilter.filter { it.role.roleId == request.roleId }.toMutableList()
        }

        if (request.roleName != null) {
            return roleListingFilter.filter { it.role.roleName == request.roleName }.toMutableList()
        }

        if (request.updatedBy != null) {
            return roleListingFilter.filter { it.role.updatedBy == request.updatedBy }.toMutableList()
        }

        if (request.updatedAt != null) {
            return roleListingFilter.filter { it.role.updatedAt == request.updatedAt }.toMutableList()
        }

        if (request.status != null) {
            return roleListingFilter.filter { it.role.status == request.status }.toMutableList()
        }
        return roleListingFilter
    }
    fun fetchRolesListingFiltersPagination(request: RoleDataService.GetRoleListRequest): RoleDataService.RoleDataResponseV2 {
        var sort: Sort? = null
        if (request.sortField?.isNotEmpty() == true) {
            if (request.sortOrder?.toLowerCase() == "asc") {
                sort = Sort.by(request.sortField).ascending()
            }
            if (request.sortOrder?.toLowerCase() == "desc") {
                sort = Sort.by(request.sortField).descending()
            }
        } else {
            sort = Sort.by("roleId").ascending()
        }

        var roleListingFilter: List<RoleListing>? = null
        roleListingFilter = roleRepository.findByRecordsWithModuleRelation(
            request?.currentLevel?.toShort(), request.aggregatorId,
            sort!!
        )
        println("roleListingFilter ==> " + roleListingFilter)
        if (request.roleId != null) {
            roleListingFilter = roleListingFilter.filter { e ->
                e.role.roleId.toString().contains(request.roleId.toString(), ignoreCase = true) ?: false
            }
        }

        if (request.roleName != null) {
            roleListingFilter = roleListingFilter.filter { e ->
                e.role.roleName.contains(request.roleName!!, ignoreCase = true) ?: false
            }
        }

        if (request.updatedBy != null) {
            roleListingFilter = roleListingFilter.filter { e ->
                e.role.updatedBy.toString().contains(request.updatedBy.toString(), ignoreCase = true) ?: false
            }
        }

        if (request.updatedAt != null) {
            roleListingFilter = roleListingFilter.filter { e ->
                e.role.updatedAt.toString().contains(request.updatedAt.toString(), ignoreCase = true) ?: false
            }
        }

        if (request.status != null) {
            roleListingFilter = roleListingFilter.filter { e ->
                e.role.status.toString().contains(request.status.toString(), ignoreCase = true) ?: false
            }
        }
        if (roleListingFilter.isNotEmpty()) {
            println("First if")
            roleListingFilter.forEach { data ->
                data.role.users = userDetailsService.getUserCountByRole(data.role).toLong()
            }
        }
        val pagination = RoleDataService.Pagination(
            current_page = request.page,
            total_records = roleListingFilter.size,
            total_pages = calculateTotalNoPages(
                roleListingFilter.size.toDouble(),
                if (request.limit > 0) request.limit.toDouble() else roleListingFilter.size.toDouble()
            )
        )
        if (request.page != null && request.page > 0) {
            val pageNo = request.page - 1
            roleListingFilter =
                roleListingFilter.map {
                    val users = userDetailsRepository.getByRoleIdAndStatus(it.role, StatusEnum.DEACTIVATED)
                    it.role.users = users?.size?.toLong()!!
                    it.role.updatedAt = it.role.updatedAt.truncatedTo(ChronoUnit.SECONDS)
                    it
                }.stream().skip(pageNo * request.limit)
                    .limit(request.limit).toList()
        }

        return RoleDataService.RoleDataResponseV2(
            fetchAllRolesCount(),
            roleListingFilter,
            pagination
        )
    }
    fun fetchRolesListingFiltersPaginationV2(request: RoleDataService.GetRoleListRequest): Any? {
        var sort: Sort? = null
        if (request.sortField?.isNotEmpty() == true) {
            if (request.sortOrder?.toLowerCase() == "asc") {
                sort = Sort.by(request.sortField).ascending()
            }
            if (request.sortOrder?.toLowerCase() == "desc") {
                sort = Sort.by(request.sortField).descending()
            }
        } else {
            sort = Sort.by("roleId").ascending()
        }
        sort = Sort.by("roleId").ascending()

        // var roles = roleRepository.findByRecordsWithModuleRelationV22(request.currentLevel.toShort(), request.aggregatorId,sort)

        var roles = roleRepository.findByRecordsWithModuleRelationV2(request.currentLevel.toShort(), request.aggregatorId, request)
        return roles
    }

    fun calculateTotalNoPages(size: Double, limit: Double): Double {
        val totalNoPages = ceil((size / limit).toDouble())
        return if (totalNoPages > 0) {
            totalNoPages
        } else {
            1.0
        }
    }

    fun fetchAllRolesCount(): Int? {
        val rolesList = roleRepository.findAll()
        return rolesList.count()
    }

    fun getByRoleIdAndLevelIdAndStatus(roleId: Int, levelId: Int, status: Boolean): MutableList<Role>? {
        val level = levelRepository.findById(levelId.toShort()).get()
        return roleRepository.getByRoleIdAndLevelIdAndStatus(roleId, level, status)
    }
    fun getRolesByRoleIdLevelId(roleId: Int?, currentLevel: Int): MutableList<RoleListingModule> {
        val level = levelRepository.findById(currentLevel.toShort()).get()
        val roles: MutableList<Role>? = if (roleId != null) {
            roleRepository.getByRoleIdAndLevelIdAndStatus(roleId, level, true)
        } else {
            val sort = Sort.by("roleId").ascending()
            roleRepository.getByLevelIdAndStatus(level, true, sort)
        }
        val roleData = mutableListOf<RoleListingModule>()
        if (!roles.isNullOrEmpty()) {
            roles.forEach { data ->
                val roleModule = roleModuleService.getRoleModule(data)
                val roleListingModule = RoleListingModule(
                    null,
                    null,
                    null, data.roleId,
                    data.roleName,
                    null
                )
                if (roleModule.moduleId.isNotEmpty()) {
                    val moduleDataList = mutableListOf<ModuleData>()
                    roleModule.moduleId.forEach { moduleId ->
                        val roleModuleData = roleManagementService.getModuleModuleId(moduleId.toShort())
                        if (roleModuleData != null) {
                            val moduleData = ModuleData(
                                roleModuleData.moduleName,
                                roleModuleData.moduleId
                            )
                            moduleDataList.add(moduleData)
                        }
                    }
                    roleListingModule.roleModuleList = moduleDataList
                }
                roleData.add(roleListingModule)
            }
        }
        return roleData
    }
}
