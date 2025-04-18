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
data class RoleManagementUser(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val roleUserId: Long = 0,
    @Column(nullable = true, unique = true)
    var roleUserType: String?,
    @Column(nullable = true, unique = true)
    var roleUserName: String?,
    @Column(nullable = true, unique = true)
    var roleUserEmail: String?,
    @Column(nullable = true, unique = true)
    var roleUserMobileNumber: String?,
    @Column(nullable = true, unique = true)
    var roleUserDesignation: String?,
    @Column(nullable = true, unique = true)
    var roleUserDepartment: String?,
    @Column(nullable = true, unique = true)
    var roleUserStatus: String?,
    @Column(nullable = true, unique = true)
    var roleUserAssignedRoleFrom: String?,
    @Column(nullable = true, unique = true)
    var roleUserSelectRoleFrom: String?,
    @Column(nullable = true, unique = true)
    var role_user_comment: String?,
    @Column(nullable = true, unique = true)
    var roleUserPermissionId: Long?,
    @Column(nullable = true, unique = true)
    var roleUserRoleId: Long?,
    @Column(name = "option_1", nullable = true, unique = true)
    var option1: String?,
    @Column(name = "option_2", nullable = true, unique = true)
    var option2: String?,
    @Column(nullable = true, unique = true)
    val lastUpdatedOn: Instant = Instant.now()
)

@Repository
interface RoleManagementUserRepository : CrudRepository<RoleManagementUser, Long>
