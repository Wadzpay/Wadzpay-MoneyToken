package com.vacuumlabs.wadzpay.usermanagement.model

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
@Table(name = "user_comments")
data class UserComments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userCommentId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id")
    var userId: UserDetails,

    var comment: String?,

    @ManyToOne
    @JoinColumn(name = "created_by")
    var createdBy: UserDetails?,

    var createdAt: Instant? = null
)
@Repository
interface UserCommentsRepository : CrudRepository<UserComments, Long> {
    fun getByUserId(userId: UserDetails): List<UserComments>?
}
