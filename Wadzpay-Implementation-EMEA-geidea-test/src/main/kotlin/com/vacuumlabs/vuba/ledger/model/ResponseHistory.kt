package com.vacuumlabs.vuba.ledger.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["idempotencyNamespace", "idempotencyKey"])]
)
data class ResponseHistory(
    @Column
    val idempotencyNamespace: String,
    @Column
    val idempotencyKey: String,
    @Column
    @Lob
    val requestBody: String,
    @Column
    var responseBody: String?,
    @Column
    var responseCode: Int?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0L
}
