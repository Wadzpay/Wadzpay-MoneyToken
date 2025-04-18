package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.vuba.ledger.api.convert.DualReferenceAttributeConverter
import com.vacuumlabs.vuba.ledger.common.DualReference
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity(name = "wadzpaySubaccount")
@Table(name = "wadzpay_subaccount")
data class Subaccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @JsonIgnore
    @ManyToOne
    val account: Account,

    @Column(unique = true)
    @Convert(converter = DualReferenceAttributeConverter::class)
    val reference: DualReference,

    val asset: String,

    @OneToOne(optional = true)
    var address: CryptoAddress? = null,

    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = true)
    var userAccountId: UserAccount? = null

) {
    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = [CascadeType.REMOVE])
    val incomingTransactions = mutableListOf<Transaction>()

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = [CascadeType.REMOVE])
    val outgoingTransactions = mutableListOf<Transaction>()
}

@Repository("wadzpaySubaccountRepository")
interface SubaccountRepository : CrudRepository<Subaccount, Long> {
    // used only in tests, there is also account.getSubaccountByAsset
    fun findByAccountAndAsset(
        account: Account,
        asset: CurrencyUnit
    ): Subaccount?

    fun getByAddress(address: CryptoAddress?): Subaccount?
}
