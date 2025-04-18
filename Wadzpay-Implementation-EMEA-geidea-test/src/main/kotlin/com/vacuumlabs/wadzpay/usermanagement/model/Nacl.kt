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
@Table(name = "nacl")
data class Nacl(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val naclId: Long = 0,
    @ManyToOne
    @JoinColumn(name = "user_id")
    val userId: UserDetails,
    val naclKey: String? = null,
    var createdBy: Long?,
    var createdAt: Instant = Instant.now(),
    var updatedBy: Long?,
    var updatedAt: Instant = Instant.now(),
    val status: Boolean = true,
)
@Repository
interface NaclRepository : CrudRepository<Nacl, Long> {
    fun getByUserId(userId: UserDetails): Nacl?
}
