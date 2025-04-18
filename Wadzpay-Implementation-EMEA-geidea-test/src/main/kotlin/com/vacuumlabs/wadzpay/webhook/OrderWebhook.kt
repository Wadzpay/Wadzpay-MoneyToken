package com.vacuumlabs.wadzpay.webhook

import com.vacuumlabs.wadzpay.merchant.model.Merchant
import org.springframework.data.repository.CrudRepository
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
data class OrderWebhook(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @Column(unique = true)
    val uuid: UUID?,

    @OneToOne(optional = false)
    var webhookOwner: Merchant? = null,

    @Column(nullable = false)
    val targetUrl: String
)

interface OrderWebhookRepository : CrudRepository<OrderWebhook, Long> {
    fun findByWebhookOwner(webhookOwner: Merchant): OrderWebhook?
}
