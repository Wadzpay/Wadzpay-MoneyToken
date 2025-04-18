package com.vacuumlabs.wadzpay.notification.notificationstatus

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table
data class NotificationStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    val userAccount: UserAccount,
    @Column(nullable = true, length = 160)
    val requesterEmail: String? = null,
    val notificationStatus: String? = null,
    val uuid: String = UUID.randomUUID().toString()

)

data class NotificationStatusParams(
    val requesterEmail: String,
    val notificationStatus: String,
    val uuid: String
)

@Repository
interface NotificationStatusRepository : CrudRepository<NotificationStatus, Long> {
    fun getAllByRequesterEmail(requesterEmail: String?): NotificationStatus
    fun findByRequesterEmail(requesterEmail: String?): NotificationStatus
}
