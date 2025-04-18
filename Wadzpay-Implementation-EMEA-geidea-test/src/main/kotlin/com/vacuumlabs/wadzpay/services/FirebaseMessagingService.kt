package com.vacuumlabs.wadzpay.services

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.vacuumlabs.wadzpay.api.Note
import org.springframework.stereotype.Service

@Service
class FirebaseMessagingService() {
    fun sendNotification(note: Note, token: String?): String? {
        val notification: Notification = Notification
            .builder()
            .setTitle(note.subject)
            .setBody(note.content)
            .build()
        val message: Message = Message
            .builder()
            .setToken(token)
            .setNotification(notification)
            .putAllData(note.data)
            .build()
        return try {
            FirebaseMessaging.getInstance().send(message)
        } catch (e: FirebaseMessagingException) {
            e.printStackTrace()
            "Error"
        }
    }
}
