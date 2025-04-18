package com.vacuumlabs.wadzpay.rolemanagement

import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.rolemanagement.model.Module
import com.vacuumlabs.wadzpay.rolemanagement.model.ModuleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementPermissions
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementRole
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementScreens
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleManagementUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
@Tag(name = "Role Management Methods")
@Validated
class RoleManagementController(
    val roleManagementService: RoleManagementService
) {
    @PostMapping(
        value = [
            "/merchant/user/createRoleManagementPermissions"
        ]
    )
    @Operation(summary = "Create Role Management Permissions")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createRoleManagementPermissions(@RequestBody roleManagementPermissions: RoleManagementPermissions) {
        roleManagementService.createRoleManagementPermissions(roleManagementPermissions)
    }

    @PostMapping(
        value = [
            "/merchant/user/createRoleManagementRole"
        ]
    )
    @Operation(summary = "Create Role Management Role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createRoleManagementRole(@RequestBody roleManagementRole: RoleManagementRole) {
        roleManagementService.createRoleManagementRole(roleManagementRole)
    }

    @PostMapping(
        value = [
            "/merchant/user/createRoleManagementUser"
        ]
    )
    @Operation(summary = "Create Role Management Role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createRoleManagementUser(@RequestBody roleManagementUser: RoleManagementUser) {
        roleManagementService.createRoleManagementUser(roleManagementUser)
    }

    @PostMapping(
        value = [
            "/merchant/user/createRoleManagementScreen"
        ]
    )
    @Operation(summary = "Create Role Management Role")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createRoleManagementScreen(@RequestBody roleManagementScreens: RoleManagementScreens) {
        roleManagementService.createRoleManagementScreen(roleManagementScreens)
    }

    // module
    @PostMapping(
        value = [
            "/merchant/role/createModule"
        ]
    )
    @Operation(summary = "Create Role Management Module")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun createRoleManagementModule(@RequestBody module: Module) {
        roleManagementService.createModule(module)
    }

    @PostMapping(
        value = [
            "/merchant/role/moduleList"
        ]
    )
    @Operation(summary = "Fetch Role Management Module")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun listRoleManagementModule(): MutableList<Module> {
        return roleManagementService.listModule()
    }

    @PostMapping(
        value = [
            "/merchant/role/updateModule"
        ]
    )
    @Operation(summary = "Update Role Management Module")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun updateRoleManagementModule(
        @RequestBody module: ModuleDataService.ModuleDataUpdate
    ) {
        roleManagementService.updateModule(module)
    }

    @PostMapping(
        value = [
            "/merchant/role/deleteModule"
        ]
    )
    @Operation(summary = "Delete Role Management Module")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun deleteRoleManagementModule(
        @RequestBody module: Module
    ) {
        roleManagementService.deleteModule(module)
    }

    @PostMapping(
        value = [
            "/merchant/user/fetchModuleRecord"
        ]
    )
    @Operation(summary = "Fetch Role Management Module")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun fetchRoleManagementModuleRecord(
        @RequestParam moduleId: Short
    ): Module {
        return roleManagementService.fetchModuleRecord(moduleId)
    }
    @PostMapping(
        value = [
            "/merchant/role/moduleListCategories"
        ]
    )
    @Operation(summary = "Fetch Role Management Module")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful request"),
        ApiResponse(
            responseCode = "400",
            description = ErrorCodes.INVALID_PAGINATION,
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "${ErrorCodes.UNAUTHORIZED} - login to get list of transactions",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "${ErrorCodes.USER_NOT_FOUND},${ErrorCodes.MERCHANT_NOT_FOUND} - current user doesn't have the account",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    fun listRoleManagementModuleCategories(): List<Module > {
        return roleManagementService.buildModuleTree()
    }
}
