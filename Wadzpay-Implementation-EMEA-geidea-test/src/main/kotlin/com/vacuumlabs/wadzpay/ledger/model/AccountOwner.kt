package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.merchant.model.Order
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EntityNotFoundException
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.OneToMany

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
// Spring requires every field to be open
open class AccountOwner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    open val id: Long = 0
) {
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.REMOVE])
    open var accounts: MutableList<Account> = mutableListOf()

    @JsonIgnore
    @OneToMany(mappedBy = "target", cascade = [CascadeType.REMOVE])
    open val incomingOrders: List<Order> = mutableListOf()

    @JsonIgnore
    @OneToMany(mappedBy = "source", cascade = [CascadeType.REMOVE])
    open val outcomingOrders: List<Order> = mutableListOf()

    fun getAccount(type: AccountType): Account {
        return accounts.find { it.type == type }
            ?: throw EntityNotFoundException(ErrorCodes.ACCOUNT_NOT_FOUND)
    }

    @get:JsonIgnore
    val account: Account
        get() = getAccount(AccountType.MAIN)
}

@Repository
interface AccountOwnerRepository : CrudRepository<AccountOwner, Long>
