package com.vacuumlabs.wadzpay.pos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.common.OptionalBigDecimalAttributeConverter
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.model.TransactionType
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import com.vacuumlabs.wadzpay.merchant.model.Order
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
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
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "transaction_pos")
data class PosTransaction(

    @JsonIgnore
    @ManyToOne
    val merchant: Merchant,

    @JsonIgnore
    @ManyToOne
    val merchantPos: MerchantPos?,

    @Enumerated(EnumType.STRING)
    var assetcrypto: CurrencyUnit,

    @Enumerated(EnumType.STRING)
    val assetfiat: FiatCurrencyUnit,

    val address: String,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    val amountfiat: BigDecimal? = null,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var amountcrypto: BigDecimal? = null,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var feewadzpay: BigDecimal? = null,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    @JsonIgnore
    val feeexternal: BigDecimal? = null,

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    val type: TransactionType,
    @JsonIgnore
    val senderid: String,
    @JsonIgnore
    var blockchainid: String? = null,

    val description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: TransactionStatus,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var digitalCurrencyReceived: BigDecimal = BigDecimal.ZERO,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var conversion_rate: BigDecimal,

    @Convert(converter = OptionalBigDecimalAttributeConverter::class)
    var totalFiatReceived: BigDecimal

) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(unique = true)
    val uuid: String = UUID.randomUUID().toString()

    val createdAt: Instant = Instant.now()

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "saltkey")
    @JsonIgnore
    var saltkey: PosSalt? = null

    @JsonIgnore
    @ManyToOne
    var transaction: Transaction? = null

    @JsonIgnore
    @ManyToOne
    var order: Order? = null

    var comments: String? = null

    @Column(name = "order_id", insertable = false, updatable = false)
    var orderId: Long? = null
}

@Repository
interface PosTransactionRepository : CrudRepository<PosTransaction, Long> {
    fun getByAddressAndType(address: String, type: TransactionType): PosTransaction?
    fun getByAddressAndTypeAndAssetcrypto(address: String, type: TransactionType, assetcrypto: CurrencyUnit): PosTransaction?
    fun getByTransaction(transaction: Transaction): MutableList<PosTransaction>?

    fun getByUuid(uuid: String): PosTransaction?
}
