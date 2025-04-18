package com.vacuumlabs.wadzpay.notification

import com.vacuumlabs.wadzpay.api.Note
import com.vacuumlabs.wadzpay.common.ErrorCodes
import com.vacuumlabs.wadzpay.common.UnprocessableEntityException
import com.vacuumlabs.wadzpay.control.NotificationRequestPayment
import com.vacuumlabs.wadzpay.ledger.model.TransactionStatus
import com.vacuumlabs.wadzpay.ledger.service.NotificationDataService
import com.vacuumlabs.wadzpay.services.FirebaseMessagingService
import com.vacuumlabs.wadzpay.user.ContactService
import com.vacuumlabs.wadzpay.user.ContactViewModel
import com.vacuumlabs.wadzpay.user.CreateExpoTokenRequest
import com.vacuumlabs.wadzpay.user.DeleteExpoTokenRequest
import com.vacuumlabs.wadzpay.user.UserAccount
import com.vacuumlabs.wadzpay.viewmodels.TransactionViewModel
import com.vacuumlabs.wadzpay.viewmodels.toPushNotificationBody
import com.vacuumlabs.wadzpay.viewmodels.toPushNotificationTitle
import io.github.jav.exposerversdk.ExpoPushMessage
import io.github.jav.exposerversdk.PushClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NotificationService(
    val expoPushNotificationTokenRepository: ExpoPushNotificationTokenRepository,
    val contactService: ContactService,
    val notificationDataRepository: NotificationDataRepository,
    val firebaseService: FirebaseMessagingService
) {
    val logger = LoggerFactory.getLogger(javaClass)
    fun checkValidExpoPushToken(token: String?) {
        if (!PushClient.isExponentPushToken(token)) {
            throw UnprocessableEntityException(ErrorCodes.INVALID_EXPO_TOKEN)
        }
    }

    fun saveExpoPushNotificationToken(request: CreateExpoTokenRequest, userAccount: UserAccount): ExpoPushNotificationToken {
        val expoToken = ExpoPushNotificationToken(
            name = request.expoToken,
            userAccount = userAccount,
            fcmId = request.fcmId,
            device = request.deviceType
        )
        var expoTokenData = ExpoPushNotificationToken(
            name = null,
            userAccount = userAccount,
            fcmId = null,
            device = null
        )
        if (request.expoToken != null) {
            expoTokenData = expoPushNotificationTokenRepository.findByName(request.expoToken)!!
        }
        if (request.fcmId != null) {
            expoTokenData = expoPushNotificationTokenRepository.findByFcmId(request.fcmId)!!
        }
        if (expoTokenData.fcmId != null || expoTokenData.name != null) {
            expoTokenData.userAccount = userAccount
            expoPushNotificationTokenRepository.save(expoTokenData)
        } else {
            expoPushNotificationTokenRepository.save(expoToken)
        }
        return expoToken
    }

    fun getExpoPushNotificationTokenByNameAndOwner(request: DeleteExpoTokenRequest, userAccount: UserAccount): ExpoPushNotificationToken? {
        if (request.expoToken != null) {
            val expoToken = expoPushNotificationTokenRepository.findByName(request.expoToken)
                ?: throw UnprocessableEntityException(ErrorCodes.INVALID_EXPO_TOKEN)
            if (expoToken.userAccount != userAccount) {
                throw UnprocessableEntityException(ErrorCodes.INVALID_EXPO_TOKEN)
            }
            return expoToken
        }
        if (request.fcmId != null) {
            val expoToken = expoPushNotificationTokenRepository.findByFcmId(request.fcmId)
                ?: throw UnprocessableEntityException(ErrorCodes.INVALID_EXPO_TOKEN)
            if (expoToken.userAccount != userAccount) {
                throw UnprocessableEntityException(ErrorCodes.INVALID_EXPO_TOKEN)
            }
            return expoToken
        }
        return null
    }

    fun deleteExpoPushNotificationToken(expoToken: ExpoPushNotificationToken) {
        expoPushNotificationTokenRepository.delete(expoToken)
    }

    fun sendTransactionNotification(userAccount: UserAccount, transaction: TransactionViewModel) {
        println("transaction.toPushNotificationTitle() ==>" + transaction.toPushNotificationTitle())
        println("transaction.toPushNotificationBody() ==>" + transaction.toPushNotificationBody())
        sendPushNotifications(
            userAccount,
            transaction.toPushNotificationTitle()!!,
            transaction.toPushNotificationBody(),
            transaction.uuid
        )
    }

    fun sendPushNotifications(userAccount: UserAccount, title: String, body: String, uuid: UUID) {
        val pushClient = PushClient()
        // Check if tokens are valid
        var tokens = userAccount.expoTokens
        if (tokens != null) {
            var expoToken: String ? = null
            var fcmId: String ? = null
            userAccount.expoTokens.forEach {
                if (it.name != null) {
                    expoToken = it.name
                    checkValidExpoPushToken(it.name)
                }
                if (it.fcmId != null) {
                    fcmId = it.fcmId
                }
            }
            if (expoToken != null) {
                val expoPushMessage = ExpoPushMessage().apply {
                    to.addAll(userAccount.expoTokens.map { it.name })
                    this.data = mapOf(
                        Pair("uuid", uuid)
                    )
                    this.title = title
                    this.body = body
                }

                val expoPushMessages = listOf(expoPushMessage)
                val chunks = pushClient.chunkPushNotifications(expoPushMessages)

                chunks.forEach { pushClient.sendPushNotificationsAsync(it) }
            }
            if (fcmId != null) {
                var note = Note(
                    data = mapOf(
                        Pair("uuid", uuid.toString())
                    ),
                    subject = title,
                    content = body

                )
                firebaseService.sendNotification(note, fcmId)
            }
        }
    }
    fun sendPushNotifications(userAccount: UserAccount, title: String, body: String) {
        val pushClient = PushClient()

        // Check if tokens are valid
        userAccount.expoTokens.forEach { checkValidExpoPushToken(it.name) }

        val expoPushMessage = ExpoPushMessage().apply {
            to.addAll(userAccount.expoTokens.map { it.name })
            this.title = title
            this.body = body
        }

        val expoPushMessages = listOf(expoPushMessage)
        val chunks = pushClient.chunkPushNotifications(expoPushMessages)

        chunks.forEach { pushClient.sendPushNotificationsAsync(it) }
    }

    fun sendPushNotificationsPayment(
        requesterAccount: UserAccount,
        senderAccount: UserAccount,
        request: NotificationRequestPayment,
        notificationDataService: NotificationDataService
    ): NotificationData {
        val pushClient = PushClient()
// senderAccount -> send money receiverName
        // requesterAccount-> need money requesterName

        val contactSender = if (contactService.getContacts(requesterAccount, senderAccount.email).isNotEmpty()) {
            contactService.getContacts(requesterAccount, senderAccount.email)[0]
        } else {
            ContactViewModel(
                senderAccount.email!!,
                senderAccount.phoneNumber!!,
                senderAccount.email!!,
                senderAccount.cognitoUsername
            )
        }
        val contactRequester = if (contactService.getContacts(senderAccount, requesterAccount.email).isNotEmpty()) {
            contactService.getContacts(senderAccount, requesterAccount.email)[0]
        } else {
            ContactViewModel(
                requesterAccount.email!!,
                requesterAccount.phoneNumber!!,
                requesterAccount.email!!,
                requesterAccount.cognitoUsername
            )
        }

        //  = contactService.getContacts(requesterAccount, senderAccount.email)[0]
        //  val contactRequester = contactService.getContacts(senderAccount, requesterAccount.email)[0]

        senderAccount.expoTokens.forEach { checkValidExpoPushToken(it.name) }
        val notiData = notificationDataRepository.save(
            NotificationData(
                userAccount = requesterAccount,
                requesterName = contactRequester?.nickname ?: "UnKnown",
                requesterEmail = requesterAccount.email,
                requesterPhone = requesterAccount.phoneNumber,
                receiverName = contactSender?.nickname ?: "Unknown",
                receiverEmail = senderAccount.email,
                receiverPhone = senderAccount.phoneNumber,
                digitalCurrency = request.digitalCurrency,
                timeNotification = request.time,
                amount = request.amount,
                status = TransactionStatus.NEW,
                title = "Payment Request",
                body = "You Have A Payment Request From ${contactRequester?.nickname ?: "UnKnown"}"
            )
        )
        val expoPushMessage = ExpoPushMessage().apply {
            to.addAll(senderAccount.expoTokens.map { it.name })
            this.data = mapOf(
                Pair("id", notiData.id),
                Pair("requesterName", contactRequester?.nickname ?: "UnKnown"),
                Pair("requesterEmail", requesterAccount.email),
                Pair("requesterPhone", requesterAccount.phoneNumber),
                Pair("receiverName", contactSender?.nickname ?: "Unknown"),
                Pair("receiverEmail", senderAccount.email),
                Pair("receiverPhone", senderAccount.phoneNumber),
                Pair("digitalCurrency", request.digitalCurrency),
                Pair("amount", request.amount),
                // Pair("fee", request.fee),
                //   Pair("walletAddress", request.walletAddress),
                Pair("time", request.time),
                Pair("title", "Payment Request"),
                Pair("body", "You Have A Payment Request From ${contactRequester?.nickname ?: "UnKnown"}")
            )

            this.title = "Payment Request"
            this.body = "You Have A Payment Request From ${contactRequester?.nickname ?: "UnKnown"}"
        }

        val expoPushMessages = listOf(expoPushMessage)
        val chunks = pushClient.chunkPushNotifications(expoPushMessages)

        chunks.forEach { pushClient.sendPushNotificationsAsync(it) }

        return notiData
    }

    fun saveExternalNotificationRequest(
        requesterAccount: UserAccount,
        senderAccount: UserAccount,
        request: NotificationRequestPayment,
        notificationDataService: NotificationDataService
    ): NotificationData {
        senderAccount.expoTokens.forEach { checkValidExpoPushToken(it.name) }
        val notiData = notificationDataRepository.save(
            NotificationData(
                userAccount = requesterAccount,
                requesterEmail = requesterAccount.email,
                receiverName = "External Wallet",
                digitalCurrency = request.digitalCurrency,
                timeNotification = request.time,
                amount = request.amount,
                status = TransactionStatus.NEW,
                title = "Payment Request Raised",
                body = "You have raised a payment request to Ext Wallet"
            )
        )
        return notiData
    }

    /*   fun savePaymentInfo(
           userAccount: UserAccount,
           request: NotificationRequestPayment,
           notificationDataService: NotificationDataService
       ) {
           notificationDataService.savePaymentInfo(userAccount, request)
       }*/

/*    fun sendNotifyRequestStatus(
        userAccount: UserAccount,
        request: UpdatePaymentRequest,
        notifyService: RequestStatusService
    ) {
        val pushClient = PushClient()

        // Check if tokens are valid
        userAccount.expoTokens.forEach { checkValidExpoPushToken(it.name) }

        val expoPushMessage = ExpoPushMessage().apply {
            to.addAll(userAccount.expoTokens.map { it.name })
            this.data = mapOf(
                Pair("requesterName", request.requesterName),
                Pair("requesterEmail", request.requesterEmail),
                Pair("requesterPhone", request.requesterPhone),
                Pair("receiverName", request.receiverName),
                Pair("receiverEmail", request.receiverEmail),
                Pair("receiverPhone", request.receiverPhone),
                Pair("digitalCurrency", request.digitalCurrency),
                Pair("amount", request.amount),
                Pair("fee", request.fee),
                Pair("walletAddress", request.walletAddress),
                Pair("time", request.time),
                Pair("title", title),
                Pair("body", body),
                Pair("uuid", request.uuid),
                Pair("status", request.status),
                Pair("requestStatusId", request.requestStatusId)
            )

            this.title = "Request ${request.status}"
            this.body = "Your Request ${request.status} From ${request.requesterName}"
        }

        val expoPushMessages = listOf(expoPushMessage)
        val chunks = pushClient.chunkPushNotifications(expoPushMessages)

        chunks.forEach { pushClient.sendPushNotificationsAsync(it) }

        notifyService.saveRequestStatus(userAccount, request)
    }*/

    fun notifyRequester(updatePaymentStatus: NotificationData, requesterAccount: UserAccount) {
        val pushClient = PushClient()

        requesterAccount.expoTokens.forEach { checkValidExpoPushToken(it.name) }
        val expoPushMessage = ExpoPushMessage().apply {
            to.addAll(requesterAccount.expoTokens.map { it.name })
            this.data = mapOf(
                Pair("id", updatePaymentStatus.id),
                Pair("requesterName", updatePaymentStatus.requesterName),
                Pair("requesterEmail", updatePaymentStatus.requesterEmail),
                Pair("requesterPhone", updatePaymentStatus.requesterPhone),
                Pair("receiverName", updatePaymentStatus.receiverName),
                Pair("receiverEmail", updatePaymentStatus.receiverEmail),
                Pair("receiverPhone", updatePaymentStatus.receiverPhone),
                Pair("digitalCurrency", updatePaymentStatus.digitalCurrency),
                Pair("amount", updatePaymentStatus.amount),
                Pair("fee", updatePaymentStatus.fee),
                Pair("walletAddress", updatePaymentStatus.walletAddress),
                Pair("time", updatePaymentStatus.timeNotification),
                Pair("title", "Request ${updatePaymentStatus.status}"),
                Pair("body", "Your Request ${updatePaymentStatus.status} From ${updatePaymentStatus.receiverName}"),
                Pair("uuid", updatePaymentStatus.uuid),
                Pair("status", updatePaymentStatus.status),
                Pair("requestStatusId", updatePaymentStatus.uuid)
            )

            this.title = "Request ${updatePaymentStatus.status}"
            this.body = "Your Request ${updatePaymentStatus.status} From ${updatePaymentStatus.receiverName}"
        }

        val expoPushMessages = listOf(expoPushMessage)
        val chunks = pushClient.chunkPushNotifications(expoPushMessages)

        chunks.forEach { pushClient.sendPushNotificationsAsync(it) }
    }
}
