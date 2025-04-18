package com.vacuumlabs.wadzpay.ledger.model

import com.vacuumlabs.wadzpay.common.BigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "bdo_withdrawal")
class BDOWithdrawal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val cardNumber: String,

    @Column
    var walletAddress: String,

    @Convert(converter = BigDecimalAttributeConverter::class)
    var totalAmount: BigDecimal,

    @Convert(converter = BigDecimalAttributeConverter::class)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    val asset: CurrencyUnit,

    @Enumerated(EnumType.STRING)
    var status: TransactionStatus,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()

)

@Repository
interface BDOWithdrawalRepository : PagingAndSortingRepository<BDOWithdrawal, Long>, JpaSpecificationExecutor<Transaction> {
    fun findByCardNumber(cardNumber: String): BDOWithdrawal?
    fun findByWalletAddress(walletAddress: String?): BDOWithdrawal?
}
