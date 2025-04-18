package com.vacuumlabs.wadzpay.fledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "user_bank_account")
data class UserBankAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    var userAccount: UserAccount,

    @Column(nullable = true, length = 25)
    val bankAccountNumber: String? = null,

    val accountHolderName: String? = null,
    val ifscCode: String? = null,
    val branchName: String? = null,

    val updatedAt: Instant = Instant.now(),
    val createdAt: Instant = Instant.now(),
    val countryCode: String? = null,
    val fiatCurrency: String? = null
)

@Repository
interface UserBankAccountRepository : CrudRepository<UserBankAccount, Long> {
    fun getByBankAccountNumber(bankAccountNumber: String): ArrayList<UserBankAccount>?
}
