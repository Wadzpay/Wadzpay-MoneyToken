package com.vacuumlabs.vuba.ledger.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class StatusEntry(
    @ManyToOne
    val commit: Commit,
    @ManyToOne
    val status: Status,
    var value: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0L
}
