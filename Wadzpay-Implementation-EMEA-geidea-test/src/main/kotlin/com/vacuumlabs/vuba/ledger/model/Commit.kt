package com.vacuumlabs.vuba.ledger.model

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Commit(
    @Id
    val reference: String
) {
    @OneToMany(mappedBy = "commit")
    val subaccountEntries = mutableSetOf<SubaccountEntry>()

    @OneToMany(mappedBy = "commit")
    val statusEntries = mutableSetOf<StatusEntry>()
}
