package com.vacuumlabs.wadzpay.ledger.model
import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "transaction_wallet_fee_details")
data class TransactionWalletFeeDetails(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0,

    @JsonIgnore
    var transactionUuid: String,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    var userAccount: UserAccount,

    @JsonIgnore
    var walletFeeId: Long,

    var baseAmount: BigDecimal? = BigDecimal.ZERO,

    var feeAmount: BigDecimal? = BigDecimal.ZERO,

    var feeName: String ? = null,

    var feeType: String ? = null,

    var feeFrequency: String ? = null,

    var feeValue: BigDecimal? = BigDecimal.ZERO,
    @JsonIgnore
    var createdAt: Instant? = null,
    @JsonIgnore
    var lastUpdatedOn: Instant? = null,

    var feeDescription: String ? = null,

    var feeAsset: String ? = null
)
@Repository
interface TransactionWalletFeeDetailsRepository : CrudRepository<TransactionWalletFeeDetails, Long> {
    fun getByTransactionUuid(uuid: String): MutableList<TransactionWalletFeeDetails>?

    fun getByWalletFeeIdAndUserAccount(walletFeeId: Long, userAccount: UserAccount): MutableList<TransactionWalletFeeDetails>?

    fun getByFeeNameAndUserAccount(feeName: String?, userAccount: UserAccount): MutableList<TransactionWalletFeeDetails>?

    fun getByFeeFrequencyAndUserAccount(feeFrequency: String, userAccount: UserAccount): MutableList<TransactionWalletFeeDetails>?

    fun getByFeeNameAndFeeFrequencyAndUserAccount(feeName: String?, feeFrequency: String?, userAccount: UserAccount): MutableList<TransactionWalletFeeDetails>?
}
