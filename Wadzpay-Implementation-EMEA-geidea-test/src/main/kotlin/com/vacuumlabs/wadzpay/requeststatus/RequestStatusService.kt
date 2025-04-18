package com.vacuumlabs.wadzpay.requeststatus

import com.vacuumlabs.wadzpay.user.UserAccount
import org.springframework.stereotype.Service

@Service
class RequestStatusService(val requestStatusRepository: RequestStatusRepository) {

  /*  fun saveRequestStatus(
        userAccount: UserAccount,
        request: UpdatePaymentRequest
    ): RequestStatus {
        println("@16  saveRequestStatus")
        val requestStatusVal = RequestStatus(
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
            requestStatusId = request.requestStatusId
        )

        requestStatusRepository.save(requestStatusVal)
        return requestStatusVal
    }*/

    fun getRequestStatus(emailParam: String, userAccount: UserAccount): List<RequestStatus>? {
        val resultNotifyDataEmail2 = requestStatusRepository.findByRequesterEmail(emailParam)
        println("@47 $resultNotifyDataEmail2")
        return resultNotifyDataEmail2
    }
}
