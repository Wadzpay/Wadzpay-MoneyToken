package com.vacuumlabs.wadzpay.rolemanagement.controller
import com.vacuumlabs.wadzpay.common.BadRequestException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.ErrorResponse
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleDataService
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleListing
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleListingModule
import com.vacuumlabs.wadzpay.rolemanagement.model.RoleModule
import com.vacuumlabs.wadzpay.rolemanagement.service.RoleModuleService
import com.vacuumlabs.wadzpay.rolemanagement.service.RoleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
@RequestMapping
class RolesController(
    val roleService: RoleService,
    val roleModuleService: RoleModuleService
) {

    @PostMapping(
        value = [
            "/merchant/role/roles"
        ]
    )
    @Operation(summary = "Get list of Roles")
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
    fun fetchRoles(
        principal: Authentication,
        @RequestBody request: RoleDataService.GetRoleListRequest

    ): MutableList<RoleListing> {
        return roleService.fetchRolesListing(request.currentLevel, request.aggregatorId)
    }

    @PostMapping(
        value = [
            "/merchant/role/createRole"
        ]
    )
    @Operation(summary = "create Role")
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
    fun createRole(@Valid@RequestBody roleAndModule: RoleDataService.RoleModulesRequest): Any {
        if (roleAndModule.role.roleName.isEmpty() || roleAndModule.module.isEmpty()) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        }
        val role = roleService.buildRole(roleAndModule)
        val roleDb = roleService.createRole(role)
        val roleModule = RoleModule(role, roleAndModule.module)
        val roleModuleDb = roleModuleService.createRoleModule(roleModule)
        return RoleDataService.RoleDataResponse(roleDb, roleModuleDb)
    }
    @PostMapping(
        value = [
            "/merchant/role/updateRole"
        ]
    )
    @Operation(summary = "update Role")
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
    fun updateRole(@RequestBody roleAndModule: RoleDataService.RoleModulesRequest): RoleDataService.RoleDataResponse {
        if (roleAndModule.role.roleId <= 0 || roleAndModule.module.isEmpty()) {
            throw BadRequestException(ErrorCodes.BAD_REQUEST)
        }
        //  val role=roleService.buildRole(roleAndModule)
        val roleDb = roleService.updateRole(roleAndModule)

        val roleModuleDb = roleModuleService.updateRoleModule(roleAndModule, roleDb)
        return RoleDataService.RoleDataResponse(roleDb, roleModuleDb)
    }
    @GetMapping(
        value = [
            "/merchant/role/init"
        ]
    )
    @Operation(summary = "initialize")
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
    fun init(
        principal: Authentication,
    ) {
        return roleService.initRoleAndUser()
    }
    //
    @PostMapping(
        value = [
            "/merchant/role/rolesFilters"
        ]
    )
    @Operation(summary = "Get list of Roles")
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
    fun fetchRolesFilters(
        principal: Authentication,
        @RequestBody request: RoleDataService.GetRoleListRequest
    ): MutableList<RoleListing> {
        println("request  $request")
        return roleService.fetchRolesListingFilters(request)
    }
    @PostMapping(
        value = [
            "/merchant/role/rolesFiltersPagination"
        ]
    )
    @Operation(summary = "Get list of Roles")
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
    fun fetchRolesFiltersPagination(
        principal: Authentication,
        @RequestBody request: RoleDataService.GetRoleListRequest
    ): RoleDataService.RoleDataResponseV2 {
        println("request  $request")
        return roleService.fetchRolesListingFiltersPagination(request)
    }
    @PostMapping(
        value = [
            "/merchant/role/rolesFiltersPaginationv2"
        ]
    )
    @Operation(summary = "Get list of Roles")
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
    fun fetchRolesByCriterion(
        principal: Authentication,
        @RequestBody request: RoleDataService.GetRoleListRequest
    ): Any? {
        return roleService.fetchRolesListingFiltersPaginationV2(request)
    }
    @GetMapping("/role/getRoles")
    @Operation(summary = "Get list of Roles")
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
    fun getRoles(
        principal: Authentication,
        @RequestParam(value = "currentLevel", required = true) currentLevel: Int,
    ): MutableList<RoleListingModule> {
        return roleService.getRolesByRoleIdLevelId(null, currentLevel)
    }
}
