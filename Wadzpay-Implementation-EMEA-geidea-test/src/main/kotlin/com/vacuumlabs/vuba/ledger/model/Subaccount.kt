package com.vacuumlabs.vuba.ledger.model

import com.vacuumlabs.vuba.ledger.api.convert.BigDecimalAttributeConverter
import java.math.BigDecimal
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.validation.constraints.NotBlank

@Entity
data class Subaccount(
    @Id
    val reference: String,
    @ManyToOne
    val account: Account,
    @OneToOne // TODO: ManyToOne?
    @NotBlank
    val asset: Asset,
    @Convert(converter = BigDecimalAttributeConverter::class)
    var balance: BigDecimal
) {
    @OneToMany(mappedBy = "subaccount")
    val entries = mutableListOf<SubaccountEntry>()
}
