package com.vacuumlabs.wadzpay.user

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.fledger.model.UserBankAccount
import com.vacuumlabs.wadzpay.issuance.models.IssuanceBanks
import com.vacuumlabs.wadzpay.kyc.models.KycLogs
import com.vacuumlabs.wadzpay.kyc.models.VerificationStatus
import com.vacuumlabs.wadzpay.ledger.model.AccountOwner
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.notification.ExpoPushNotificationToken
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class UserAccount(
    @Column(unique = true)
    val cognitoUsername: String,

    @Column(unique = true)
    var email: String?,

    @Column(unique = true)
    val phoneNumber: String?,

    @Column
    var notificationStatus: Boolean? = true,

    @ManyToOne
    var merchant: Merchant? = null,

    var firstName: String? = null,
    var lastName: String? = null,
    @ManyToOne
    var issuanceBanks: IssuanceBanks? = null,

    var customerType: String ? = null,

    var createdDate: Instant? = null,

    @Column(unique = true)
    val customerId: String? = null,
    @Column(unique = true)
    var bearerToken: String? = null,

) : AccountOwner() {

    @JsonIgnore
    @OneToMany(mappedBy = "userAccount", cascade = [CascadeType.REMOVE])
    val contactEntries: List<Contact> = emptyList()

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = [CascadeType.REMOVE])
    val ownedContacts: List<Contact> = emptyList()

    @JsonIgnore
    @OneToMany(mappedBy = "userAccount")
    val expoTokens: MutableList<ExpoPushNotificationToken> = mutableListOf()

    @JsonIgnore
    @OneToMany(mappedBy = "userAccount")
    val kycLogs: MutableList<KycLogs> = mutableListOf()

    @OneToMany(mappedBy = "userAccount")
    val userBankAccount: MutableList<UserBankAccount> = mutableListOf()

    @Enumerated(EnumType.STRING)
    var kycVerified: VerificationStatus = VerificationStatus.UNKNOWN
}

@Repository
interface UserAccountRepository : CrudRepository<UserAccount, Long> {
    fun getByEmail(email: String): UserAccount?
    fun getByPhoneNumber(phoneNumber: String): UserAccount?
    fun getByCognitoUsername(cognitoUsername: String): UserAccount?
    fun getByMerchantId(merchantId: Long): ArrayList<UserAccount>?

    fun getByFirstName(firstName: String?): UserAccount?

    fun getByLastName(lastName: String?): UserAccount?

    @Query("From UserAccount  where email like %:email%")
    fun findByEmailLike(email: String): ArrayList<UserAccount>?

    @Query("From UserAccount  where phoneNumber like %:phoneNumber%")
    fun findByPhoneLike(phoneNumber: String): ArrayList<UserAccount>?

    @Query("From UserAccount  where firstName like %:firstName%")
    fun findByFirstNameLike(firstName: String): ArrayList<UserAccount>?
    fun findByEmail(firstName: String): UserAccount?
    fun findByBearerToken(bearerToken: String): UserAccount?

    @Query("From UserAccount  where lastName like %:lastName%")
    fun findByLastNameLike(lastName: String): ArrayList<UserAccount>?

    fun getByCustomerIdIgnoreCase(customerId: String?): ArrayList<UserAccount>?

    fun getByKycVerified(kycLogs: VerificationStatus): MutableIterable<UserAccount>?

    fun getByEmailIgnoreCase(email: String): UserAccount?
}
