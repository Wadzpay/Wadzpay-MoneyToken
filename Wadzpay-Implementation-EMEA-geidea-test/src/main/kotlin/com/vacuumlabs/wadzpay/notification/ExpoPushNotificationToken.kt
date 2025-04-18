package com.vacuumlabs.wadzpay.notification

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.user.DeviceType
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class ExpoPushNotificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val id: Long = 0,

    @Column(unique = true)
    val name: String ? = null,

    val fcmId: String ? = null,

    @Enumerated(EnumType.STRING)
    val device: DeviceType? = null,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    var userAccount: UserAccount
)

@Repository
interface ExpoPushNotificationTokenRepository : CrudRepository<ExpoPushNotificationToken, Long> {
    fun findByName(name: String): ExpoPushNotificationToken?

    fun findByFcmId(name: String): ExpoPushNotificationToken?
}
