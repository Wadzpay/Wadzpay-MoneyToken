package com.vacuumlabs.wadzpay.ledger.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vacuumlabs.wadzpay.merchant.model.Merchant
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "merchant_config")
class MerchantConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = 0,

    @JsonIgnore
    @OneToOne
    var merchant: Merchant? = null,

    @Column
    var autoRefundApproveSeconds: Long = 0,

    @Column
    var resendExpiredWeblinkSeconds: Long = 0,

    @Column
    var resendExpiredWeblinkLimitCount: Int = 0,

    @Column
    var autoRefundApprovalRequired: Boolean = false,

    @Column
    var autoSendWeblinkRequired: Boolean = false,

    @Column
    var description: String? = null,

    @Column
    var createdAt: Instant? = Instant.now(),

    @Column
    var createdBy: String? = null,

    @Column
    var resendThresholdMaxSeconds: Long = 0,
)

@Repository
interface MerchantConfigRepository : CrudRepository<MerchantConfig, String> {
    fun findAllByAutoSendWeblinkRequired(flag: Boolean): MutableList<MerchantConfig>
    fun findAllByAutoRefundApprovalRequired(flag: Boolean): MutableList<MerchantConfig>
    fun findByMerchantId(merchantId: Long): MerchantConfig?
}
data class MerchantConfigRequest(
    val merchantId: Long,
    var autoRefundApproveSeconds: Long,
    var resendExpiredWeblinkSeconds: Long,
    var resendExpiredWeblinkLimitCount: Int,
    var autoRefundApprovalRequired: Boolean,
    var autoSendWeblinkRequired: Boolean,
    var description: String?,
    var createdBy: String? = null,
    var resendThresholdMaxSeconds: Long
)

data class MerchantConfigResponse(
    val id: Long,
    val merchantId: Long,
    var autoRefundApproveSeconds: Long,
    var resendExpiredWeblinkSeconds: Long,
    var resendExpiredWeblinkLimitCount: Int,
    var autoRefundApprovalRequired: Boolean,
    var autoSendWeblinkRequired: Boolean,
    var description: String?,
    var createdBy: String? = null,
    var resendThresholdMaxSeconds: Long
)
