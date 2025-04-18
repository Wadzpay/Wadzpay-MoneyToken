package com.vacuumlabs.wadzpay.merchant.model

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
data class CustomerOfflineWrongPasswordEntry(
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = true, unique = true)
    var senderId: String?,
    @Column(nullable = true, unique = true)
    var senderEmail: String?,
    @Column(nullable = true, unique = true)
    var senderName: String?,
    @Column(nullable = true, unique = true)
    var receiverId: String?,
    @Column(nullable = true, unique = true)
    var receiverEmail: String?,
    @Column(nullable = true, unique = true)
    var receiverName: String?,
    @JsonIgnore
    @Column(nullable = true, unique = true)
    val createdAt: Instant = Instant.now()
)

@Repository
interface CustomerOfflineWrongPasswordEntryRepository : CrudRepository<CustomerOfflineWrongPasswordEntry, Long>
