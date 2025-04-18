package com.vacuumlabs.wadzpay.usermanagement.model

import com.vacuumlabs.wadzpay.rolemanagement.model.Level
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "level_user_details_mapping")
data class LevelUserDetailsMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val levelUserDetailsMappingId: Long = 0,
    @ManyToOne
    @JoinColumn(name = "level_id")
    var levelId: Level,
    @ManyToOne
    @JoinColumn(name = "user_id")
    var userId: UserDetails,
    var createdBy: Long?,
    var createdAt: Instant = Instant.now()
)
fun LevelUserDetailsMapping.toViewModelLevelUsers(): UserDetailsViewModel {
    return UserDetailsViewModel(
        userId.userId,
        userId.name.trim(),
        userId.userPreferenceId.trim(),
        userId.emailId.trim(),
        userId.countryCode,
        userId.mobileNo,
        userId.designation,
        userId.departmentId?.departmentId,
        userId.departmentId?.departmentName,
        userId.roleId?.roleId,
        userId.roleId?.roleName,
        userId.createdBy?.userId,
        userId.createdBy?.name?.trim(),
        null,
        userId.createdAt,
        userId.updatedAt,
        userId.lastSuccessLogin,
        userId.status.statusType,
        null,
        userId.roleFromUserId
    )
}
data class UserDetailsViewModel(
    val userId: Long,
    val userName: String,
    val userPreferenceId: String?,
    val userEmail: String?,
    val countryCode: String?,
    val userMobile: String?,
    val designation: String?,
    val departmentId: Int?,
    val department: String?,
    val assignedRoleId: Int?,
    val assignedRole: String?,
    val requestedById: Long?,
    val requestedBy: String?,
    val actionBy: String?,
    var createdAt: Instant?,
    var updatedAt: Instant?,
    val lastActiveAt: Instant?,
    val status: String?,
    var comment: List<comments>?,
    val roleFromUserId: Long?
)

data class comments(
    val userCommentId: Long,
    val comment: String?,
    val commentDate: Instant? = null,
    val createdBy: String? = null
)
@Repository
interface LevelUserDetailsMappingRepository : CrudRepository<LevelUserDetailsMapping, Long> {
    fun findByLevelId(levelId: Level): MutableList<LevelUserDetailsMapping>?

    fun findByUserId(userId: UserDetails): MutableList<LevelUserDetailsMapping>?
}
