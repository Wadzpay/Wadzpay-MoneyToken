package com.vacuumlabs.wadzpay.webhook

import com.vacuumlabs.wadzpay.merchant.model.Order
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
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

@Entity
class OrderWebhookLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @JoinColumn(nullable = false)
    @ManyToOne
    val webhook: OrderWebhook,

    @JoinColumn(nullable = false)
    @ManyToOne
    val order: Order,

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    val responseStatus: HttpStatus? = null,

    @Column(nullable = true)
    val responseBody: String? = null,

    @Column(nullable = false)
    val retryCount: Int = 0,

    @Column(nullable = false)
    val timestamp: Instant = Instant.now()
)

interface OrderWebhookLogRepository : CrudRepository<OrderWebhookLog, Long> {
    fun countByOrder(order: Order): Int
}
