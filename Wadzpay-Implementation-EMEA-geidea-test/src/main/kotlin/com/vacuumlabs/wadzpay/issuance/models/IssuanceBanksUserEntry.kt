package com.vacuumlabs.wadzpay.issuance.models

import au.com.console.jpaspecificationdsl.`in`
import au.com.console.jpaspecificationdsl.equal
import au.com.console.jpaspecificationdsl.greaterThanOrEqualTo
import au.com.console.jpaspecificationdsl.lessThanOrEqualTo
import com.opencsv.bean.CsvDate
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "issuance_banks_user_entry")
data class IssuanceBanksUserEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "issuance_banks_id", nullable = false)
    var issuanceBanksId: IssuanceBanks,

    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    var userAccountId: UserAccount,

    val validFromDate: Instant = Instant.now(),
    val validToDate: Instant = Instant.now(Clock.offset(Clock.systemUTC(), Duration.ofDays(30))),

    @Enumerated(EnumType.STRING)
    var status: Status,
    var isActive: Boolean? = true,
    @Column(nullable = true, unique = true)
    val walletId: String,
    var updatedAt: Instant? = Instant.now(),
    val createdAt: Instant = Instant.now(),
    var partnerInstitutionName: String ? = null
)
data class UserAccountViewModel(
    val status: Status,
    val firstName: String ?,
    val lastName: String ?,
    val walletId: String,
    val phoneNumber: String?,
    val email: String?,
    val custonerId: String?,
    val cognitoUsername: String,
    @CsvDate(value = "yyyy-MM-dd")
    val validFromDate: Instant,
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    val validToDate: Instant,
    val isActive: Boolean? = true,
    var tokenBalance: Any?,
    var tokenAsset: CurrencyUnit,
    @CsvDate(value = "yyyy-MM-dd-HH:mm:ss")
    val createdAt: Instant,
    val updatedAt: Instant?,
)
enum class Status {
    INVITED,
    VERIFIED,
    ENABLE,
    DISABLED
}
enum class SortableFields(val value: String) {
    STATUS("status"),
    CRETAEDAT("createdAt"),
    UPDATEDAT("updatedAt")
}
fun belongsToAccount(userAccount: UserAccount): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::userAccountId.equal(userAccount)

fun hasOwner(owner: IssuanceBanks): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::issuanceBanksId.equal(owner)
fun hasDateGreaterOrEqualTo(date: Instant): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::validFromDate.greaterThanOrEqualTo(date)

fun hasDateLessOrEqualTo(date: Instant): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::validToDate.lessThanOrEqualTo(date)

fun hasStatus(statuses: Collection<Status>): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::status.`in`(statuses)
fun hasWalletIdEqualTo(walletId: String): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::walletId.equal(walletId)
fun hasCreatedDateGreaterOrEqualTo(date: Instant): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::createdAt.greaterThanOrEqualTo(date)

fun hasCreatedDateLessOrEqualTo(date: Instant): Specification<IssuanceBanksUserEntry> =
    IssuanceBanksUserEntry::createdAt.lessThanOrEqualTo(date)

fun IssuanceBanksUserEntry.toViewModel(): UserAccountViewModel {
    return UserAccountViewModel(
        status,
        userAccountId.firstName,
        userAccountId.lastName,
        walletId,
        userAccountId.phoneNumber,
        if (userAccountId.customerId != null) if (checkEmail(userAccountId.customerId!!)) userAccountId.email else userAccountId.customerId else userAccountId.email,
        userAccountId.customerId,
        userAccountId.cognitoUsername,
        validFromDate,
        validToDate,
        isActive,
        BigDecimal.ZERO.toString(),
        CurrencyUnit.SART,
        createdAt,
        updatedAt
    )
}

fun checkEmail(username: String): Boolean {
    println("checkEmail ==> $username")
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return username.matches(emailRegex.toRegex())
}

@Repository

interface IssuanceBanksUserEntryRepository : PagingAndSortingRepository<IssuanceBanksUserEntry, Long>, JpaSpecificationExecutor<IssuanceBanksUserEntry> {
    fun getByUserAccountId(userAccountId: UserAccount): IssuanceBanksUserEntry?

    fun getByIssuanceBanksId(issuanceBanksId: IssuanceBanks): List<IssuanceBanksUserEntry>?
}
