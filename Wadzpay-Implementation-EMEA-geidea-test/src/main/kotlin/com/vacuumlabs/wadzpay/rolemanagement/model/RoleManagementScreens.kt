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
data class RoleManagementScreens(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val roleScreenId: Long = 0,
    @Column(nullable = true, unique = true)
    var roleScreenType: String?,
    @Column(nullable = true, unique = true)
    var roleScreenName: String?,
    @Column(nullable = true, unique = true)
    var roleScreenDashboard: String?,
    @Column(nullable = true, unique = true)
    var roleScreenTransactions: String?,
    @Column(nullable = true, unique = true)
    var roleScreenRefund: String?,
    @Column(nullable = true, unique = true)
    var roleScreenSettings: String?,
    @Column(nullable = true, unique = true)
    var roleScreenAdmin: String?,
    @Column(nullable = true, unique = true)
    var roleScreenAggregator: String?,
    @Column(nullable = true, unique = true)
    var roleScreenInstitution: String?,
    @Column(nullable = true, unique = true)
    var roleScreenPermissionId: Long?,
    @Column(nullable = true, unique = true)
    var roleScreenRoleId: Long?,
    @Column(name = "option_1", nullable = true, unique = true)
    var option1: String?,
    @Column(name = "option_2", nullable = true, unique = true)
    var option2: String?,
    @Column(nullable = true, unique = true)
    val lastUpdatedOn: Instant = Instant.now()
)

@Repository
interface RoleManagementScreensRepository : CrudRepository<RoleManagementScreens, Long> {
    fun getByRoleScreenId(roleScreenRoleId: Long): RoleManagementScreens
}
