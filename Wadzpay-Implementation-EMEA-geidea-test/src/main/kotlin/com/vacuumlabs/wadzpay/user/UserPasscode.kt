package com.vacuumlabs.wadzpay.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "user_passcode")
data class UserPasscode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    var userAccount: UserAccount,

    @Column(nullable = false, length = 255)
    val passcodeHash: String,

    @JsonIgnore
    val saltVersion: String? = null,

    @JsonIgnore
    var isActive: Boolean = true,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: UserAccount? = null,

    @JsonIgnore
    var createdDate: Instant? = null,

    @JsonIgnore
    var modifiedDate: Instant? = null,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    var modifiedBy: UserAccount? = null
)

@Repository
interface UserPasscodeRepository : CrudRepository<UserPasscode, Long> {
    fun getByUserAccount(userAccount: UserAccount): MutableList<UserPasscode>?
}
