package com.vacuumlabs.wadzpay.merchant.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.vuba.ledger.api.convert.ReferenceAttributeConverter
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.wadzpay.ledger.model.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "fiat_sub_account")
data class FiatSubAccount(
    @JsonIgnore
    @Convert(converter = ReferenceAttributeConverter::class)
    val userReference: Reference?,
    @JsonIgnore
    @ManyToOne
    val userAccount: Account,

    @Enumerated(EnumType.STRING)
    val fiatasset: FiatCurrencyUnit,

    var balance: BigDecimal = BigDecimal.ZERO,

    val updatedAt: Instant = Instant.now()

) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}

@Repository
interface FiatSubAccountRepository : CrudRepository<FiatSubAccount, Long> {
    fun findByUserAccount(account: Account): ArrayList<FiatSubAccount>?
}
