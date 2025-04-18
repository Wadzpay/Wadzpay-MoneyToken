package com.vacuumlabs.wadzpay.rolemanagement.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class RoleManagementPermissions(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val permissionId: Long = 0,
    @Column(nullable = true, unique = true)
    var permissionName: String?,
    @Column(nullable = true, unique = true)
    var permissionType: String?,
    @Column(name = "option_1", nullable = true, unique = true)
    var option1: String?,
    @Column(name = "option_2", nullable = true, unique = true)
    var option2: String?,
    @Column(nullable = true, unique = true)
    val lastUpdatedOn: Instant = Instant.now()
)

@Repository
interface RoleManagementPermissionsRepository : CrudRepository<RoleManagementPermissions, Long> {
    fun getByPermissionId(permissionId: Long): RoleManagementPermissions
}
