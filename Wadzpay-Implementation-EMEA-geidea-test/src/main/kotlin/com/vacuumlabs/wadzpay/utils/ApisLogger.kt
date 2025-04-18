package com.vacuumlabs.wadzpay.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "apis_log")
data class ApisLog(
    @Column(unique = true)
    val end_point: String,
    @Column(unique = true)
    val mesg: String,
    @Column(unique = true)
    val user_email: String,
    @Column(unique = true)
    val other: String,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0
}
@Repository
interface ApisLoggerRepository : CrudRepository<ApisLog, Long> {
    fun getApisLoggerById(id: Long): ApisLog
}
