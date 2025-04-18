package com.vacuumlabs.wadzpay.rolemanagement.model

import java.time.Instant
import javax.validation.constraints.PositiveOrZero

class RoleDataService {

    data class GetRoleListRequest(
        val roleId: Int? = null,
        val currentLevel: Int,
        var roleName: String? = null,
        val aggregatorId: String,
        var updatedBy: String? = "",
        val updatedAt: Instant? = null,
        var status: Boolean? = null,
        val limit: Long = 10,
        val duration: Long = 0,
        val startDate: String,
        val endDate: String,
        @PositiveOrZero
        val page: Long? = null,
        val sortField: String? = null,
        val sortOrder: String? = null
    )
    data class RoleDTO(

        val roleId: Int = 0,
        var roleName: String,
        var levelId: Short,
        var aggregatorId: String
    ) {
        var users: Long = 0
        var roleComments: String = ""
        var createdBy: String = ""
        var createdAt: Instant = Instant.now()
        var updatedBy: String = ""
        var updatedAt: Instant = Instant.now()
        var status: Boolean? = true
    }
    data class RoleModulesRequest(
        val role: RoleDTO,
        val module: Array<Int>

    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RoleModulesRequest

            if (role != other.role) return false
            if (!module.contentEquals(other.module)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = role.hashCode()
            result = 31 * result + module.contentHashCode()
            return result
        }
    }

    data class RoleDataResponse(
        val role: Role,
        val roleModule: RoleModule
    )
    data class RoleDataResponseV2(
        val totalCount: Int? = 0,
        val roleList: List<RoleListing>? = null,
        val pagination: Pagination? = null,

    )
    data class RoleDataResponseV3(
        val totalCount: Int? = 0,
        val roleList: Any? = null,
        val pagination: Pagination? = null

    )

    data class RoleListingDTO(
        val roleId: Int,
        val roleName: String,
        val roleCreatedAt: Instant,
        val roleUpdatedAt: Instant,
        val aggregatorId: String,
        val roleStatus: Boolean?,
        val levelId: Short,
        val levelName: String,
        val levelNumber: Short,
        val levelCreatedAt: Instant,
        val levelUpdatedAt: Instant,
        val moduleIdArray: Array<Int>,
        val roleModuleId: Int,
        val roleModuleCreatedAt: Instant,
        val roleModuleUpdatedAt: Instant,
        val userDetailsCount: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RoleListingDTO

            if (roleId != other.roleId) return false
            if (roleName != other.roleName) return false
            if (roleCreatedAt != other.roleCreatedAt) return false
            if (roleUpdatedAt != other.roleUpdatedAt) return false
            if (aggregatorId != other.aggregatorId) return false
            if (roleStatus != other.roleStatus) return false
            if (levelId != other.levelId) return false
            if (levelName != other.levelName) return false
            if (levelNumber != other.levelNumber) return false
            if (levelCreatedAt != other.levelCreatedAt) return false
            if (levelUpdatedAt != other.levelUpdatedAt) return false
            if (!moduleIdArray.contentEquals(other.moduleIdArray)) return false
            if (roleModuleId != other.roleModuleId) return false
            if (roleModuleCreatedAt != other.roleModuleCreatedAt) return false
            if (roleModuleUpdatedAt != other.roleModuleUpdatedAt) return false
            if (userDetailsCount != other.userDetailsCount) return false

            return true
        }

        override fun hashCode(): Int {
            var result = roleId
            result = 31 * result + roleName.hashCode()
            result = 31 * result + roleCreatedAt.hashCode()
            result = 31 * result + roleUpdatedAt.hashCode()
            result = 31 * result + aggregatorId.hashCode()
            result = 31 * result + (roleStatus?.hashCode() ?: 0)
            result = 31 * result + levelId
            result = 31 * result + levelName.hashCode()
            result = 31 * result + levelNumber
            result = 31 * result + levelCreatedAt.hashCode()
            result = 31 * result + levelUpdatedAt.hashCode()
            result = 31 * result + moduleIdArray.contentHashCode()
            result = 31 * result + roleModuleId
            result = 31 * result + roleModuleCreatedAt.hashCode()
            result = 31 * result + roleModuleUpdatedAt.hashCode()
            result = 31 * result + userDetailsCount.hashCode()
            return result
        }
    }

    data class Pagination(
        val current_page: Long? = 0,
        val total_records: Int = 0,
        val total_pages: Double = 0.0
    )

    data class RoleListRequest(
        val currentLevel: Int
    )
}
