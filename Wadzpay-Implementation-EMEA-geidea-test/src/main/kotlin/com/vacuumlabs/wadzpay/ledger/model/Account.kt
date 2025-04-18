package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.vuba.ledger.api.convert.ReferenceAttributeConverter
import com.vacuumlabs.vuba.ledger.common.Reference
import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes.Companion.SUBACCOUNT_NOT_FOUND
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.FiatSubAccount
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

enum class AccountType {
    MAIN,
    RESERVATION
}

@Entity(name = "wadzpayAccount")
@Table(name = "wadzpay_account")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @JsonIgnore
    @ManyToOne
    @JoinColumn
    /*owner may be null for omnibus and fee collection accounts*/
    var owner: AccountOwner? = null,

    @Column(unique = true)
    @Convert(converter = ReferenceAttributeConverter::class)
    val reference: Reference,

    @Enumerated(EnumType.STRING)
    val type: AccountType = AccountType.MAIN
) {
    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = [CascadeType.REMOVE])
    val subaccounts = mutableListOf<Subaccount>()

    @JsonIgnore
    @OneToMany(mappedBy = "userAccount", cascade = [CascadeType.REMOVE])
    val fiatSubAccount = mutableListOf<FiatSubAccount>()

    fun getSubaccountByAsset(asset: CurrencyUnit): Subaccount {
        return subaccounts.find { it.asset == asset.toString() }
            ?: throw EntityNotFoundException(SUBACCOUNT_NOT_FOUND)
    }

    fun getSubaccountByAssetString(asset: String): Subaccount {
        return subaccounts.find { it.asset == asset }
            ?: throw EntityNotFoundException(SUBACCOUNT_NOT_FOUND)
    }
    fun getSubAccountByAssetAlgo(asset: CurrencyUnit): Subaccount? {
        return subaccounts.find { it.asset == asset.toString() }
    }

    fun getSubAccountByAssetAlgoString(asset: String): Subaccount? {
        return subaccounts.find { it.asset == asset }
    }

    fun getOwnerName(): String? {
        return when (owner) {
            is UserAccount -> if ((owner as UserAccount).customerId != null) (owner as UserAccount).customerId else (owner as UserAccount).email
            is Merchant -> (owner as Merchant).name
            else -> "External" // TODO: add more logic when we'll have off-ramp
        }
    }
    fun getOwnerFirstName(): String? {
        return when (owner) {
            is UserAccount -> if ((owner as UserAccount).firstName != null) (owner as UserAccount).firstName?.capitalize() else (owner as UserAccount).customerId
            is Merchant -> (owner as Merchant).name.capitalize()
            else -> null
        }
    }
    fun getOwnerLastName(): String? {
        return when (owner) {
            is UserAccount -> (owner as UserAccount).lastName?.capitalize()
            else -> " "
        }
    }
}

@Repository("wadzpayAccountRepository")
interface AccountRepository : CrudRepository<Account, Long> {
    fun getByReference(reference: Reference): Account
    fun getByOwnerAndType(owner: AccountOwner, type: AccountType): Account?
}
