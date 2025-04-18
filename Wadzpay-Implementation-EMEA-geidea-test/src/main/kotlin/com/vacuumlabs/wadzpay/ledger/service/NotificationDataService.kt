package com.vacuumlabs.wadzpay.ledger.service

import com.vacuumlabs.wadzpay.common.EntityNotFoundException
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.notification.NotificationData
import com.vacuumlabs.wadzpay.notification.NotificationDataRepository
import com.vacuumlabs.wadzpay.requeststatus.UpdatePaymentRequest
import com.vacuumlabs.wadzpay.user.UpdateNotification
import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class NotificationDataService(val notificationDataRepository: NotificationDataRepository) {

    fun saveExpoPushNotificationPayment(
        userAccount: UserAccount,
        notificationData: NotificationData
    ): NotificationData {
        return notificationDataRepository.save(notificationData)
    }

    /*   fun savePaymentInfo(
           userAccount: UserAccount,
           request: NotificationRequestPayment
       ): NotificationData {
           println("@44  savePaymentInfo")
           val notificationDataValue = NotificationData(
               id = request.requesterId,
               userAccount = userAccount,
               requesterName = request.requesterName,
               receiverEmail = request.receiverEmail,
               receiverPhone = request.receiverPhone,
               receiverName = request.receiverName,
               requesterEmail = request.requesterEmail,
               requesterPhone = request.requesterPhone,
               digitalCurrency = request.digitalCurrency,
               amount = request.amount,
               fee = request.fee,
               walletAddress = request.walletAddress,
               timeNotification = request.time,
               title = request.title,
               body = request.body,
               uuid = request.uuid,
               status = request.status,
               transactionId = request.transactionId
           )

           notificationDataRepository.save(notificationDataValue)
           return notificationDataValue
       }
   */
    fun getPushNotificationEmailData(email: String): List<NotificationData>? {
        var resultNotifyDataEmail2 = notificationDataRepository.findAllByRequesterEmailOrReceiverEmailOrderByTimeNotification(email, email)
        resultNotifyDataEmail2 = resultNotifyDataEmail2!!.reversed()
        return resultNotifyDataEmail2
    }

    fun updatePaymentStatus(request: UpdatePaymentRequest): NotificationData {

        val title = when (request.status) {
            TransactionStatus.SUCCESSFUL -> {
                "Payment Request Approved"
            }
            TransactionStatus.FAILED -> {
                "Payment Request Rejected"
            }
            else -> {
                "Payment Request " + request.status
            }
        }

        val notificationData = notificationDataRepository.findById(request.id).get()
        notificationData.status = request.status
        notificationData.title = title

        val body = when (request.status) {
            TransactionStatus.SUCCESSFUL -> {
                "Your payment request is Approved by " + notificationData.receiverName
            }
            TransactionStatus.FAILED -> {
                "Your payment request is Rejected by " + notificationData.receiverName
            }
            else -> {
                "Your payment request is " + request.status + " by " + notificationData.receiverName
            }
        }
        notificationData.body = body
        val notificationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        notificationData.timeNotification = notificationTime
        return notificationDataRepository.save(notificationData)
    }

    fun updateNotificationData(request: UpdateNotification): UpdateNotification {
        val notificationData = notificationDataRepository.findById(request.id).get()
        if (notificationData != null) {
            notificationData.isRead = request.isRead
            notificationDataRepository.save(notificationData)
            return request
        } else {
            throw EntityNotFoundException(ErrorCodes.NOTIFICATION_DATA_NOT_FOUND)
        }
    }
}
