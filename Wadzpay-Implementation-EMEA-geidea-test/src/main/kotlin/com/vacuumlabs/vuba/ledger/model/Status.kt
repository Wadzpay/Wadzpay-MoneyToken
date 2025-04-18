package com.vacuumlabs.vuba.ledger.model

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
data class Status(
    @Id
    val reference: String,
    @ManyToOne // TODO: really?
    val account: Account,
    @OneToOne
    val type: StatusType,
    var value: String
) {
    @OneToMany(mappedBy = "status")
    val entries = mutableListOf<StatusEntry>()
}
