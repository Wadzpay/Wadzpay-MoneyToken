package com.vacuumlabs.wadzpay.notification.notificationstatus

import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.stereotype.Service

@Service
class NotificationStatusService(val notificationStatusRepository: NotificationStatusRepository) {

    fun saveRequestStatus(
        userAccount: UserAccount,
        request: NotificationStatusParams
    ) {
        var notificationStatus: NotificationStatus
        var notificationStatusVal: NotificationStatus
        try {
            notificationStatus =
                notificationStatusRepository.findByRequesterEmail(request.requesterEmail)
            if (notificationStatus == null) {
                notificationStatusVal = NotificationStatus(
//                    id = notificationStatus.id,
                    userAccount = userAccount,
                    requesterEmail = request.requesterEmail,
                    notificationStatus = request.notificationStatus,
                    uuid = request.uuid
                )
            } else {
                notificationStatusVal = NotificationStatus(
                    id = notificationStatus.id,
                    userAccount = userAccount,
                    requesterEmail = request.requesterEmail,
                    notificationStatus = request.notificationStatus,
                    uuid = request.uuid
                )
            }
        } catch (e: Exception) {
            notificationStatusVal = NotificationStatus(
                userAccount = userAccount,
                requesterEmail = request.requesterEmail,
                notificationStatus = request.notificationStatus,
                uuid = request.uuid
            )
        }

        notificationStatusRepository.save(notificationStatusVal)
    }

    fun getNotificationStatus(emailParam: String, userAccount: UserAccount): NotificationStatus {
        return notificationStatusRepository.findByRequesterEmail(emailParam)
    }
}
