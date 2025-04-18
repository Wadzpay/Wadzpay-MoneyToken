package com.vacuumlabs.vuba.ledger.model

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Account(
    @Id
    val reference: String,
) { // TODO: why are these not date class properties?
    @OneToMany(mappedBy = "account")
    val subaccounts = mutableSetOf<Subaccount>()

    @OneToMany(mappedBy = "account")
    val statuses = mutableSetOf<Status>()
}
