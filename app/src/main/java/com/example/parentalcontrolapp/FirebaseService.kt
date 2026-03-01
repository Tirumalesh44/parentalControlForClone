package com.example.parentalcontrolapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        println("MESSAGE RECEIVED FROM FCM")
        println("MESSAGE RECEIVED FROM FCM")
        println("DATA: ${remoteMessage.data}")
        println("NOTIFICATION: ${remoteMessage.notification}")

        val title = remoteMessage.data["title"] ?: "Alert"
        val body = remoteMessage.data["body"] ?: "Incident detected"

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {

        val channelId = "incident_alerts"

        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Incident Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1, notification)
    }
}