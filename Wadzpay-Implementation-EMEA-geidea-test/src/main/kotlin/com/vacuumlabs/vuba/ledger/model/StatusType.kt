package com.vacuumlabs.vuba.ledger.model

import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class StatusType(
    @Id
    val reference: String,
) {
    @ElementCollection
    @Column(name = "vals")
    val values = mutableSetOf<String>()
}
