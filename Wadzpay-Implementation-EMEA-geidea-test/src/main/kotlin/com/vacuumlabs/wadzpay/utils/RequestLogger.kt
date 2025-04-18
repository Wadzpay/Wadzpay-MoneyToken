package com.vacuumlabs.wadzpay.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "request_log")
data class RequestLogger(
    val hostNameServer: String?,
    val canonicalHostNameServer: String?,
    val hostAddressServer: String?,
    val requestStatus: String?,
    val requestLocalAddr: String?,
    val requestLocalName: String?,
    val requestLocale: String?,
    val requestLocalPort: String?,
    val requestMethod: String?,
    val requestAuth: String?,
    val requestUri: String?,
    val userRemoteAddr: String?,
    val requestParameterMap: String?,
    val responseSent: String?,
    val createdAt: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0
}

@Repository
interface RequestLoggerRepository : CrudRepository<RequestLogger, Long> {
    fun getById(id: Long): RequestLogger?
}
