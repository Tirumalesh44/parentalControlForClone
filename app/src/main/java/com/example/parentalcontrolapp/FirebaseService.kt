package com.example.parentalcontrolapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("=== FCM MESSAGE RECEIVED ===")
        println("From: ${remoteMessage.from}")
        println("Data: ${remoteMessage.data}")
        println("Notification: ${remoteMessage.notification}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Parental Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Inappropriate content detected!"

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("NEW FCM TOKEN: $token")
        // Optional: re-register to backend here if you want
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
            .setAutoCancel(true)
            .build()

        manager.notify(1, notification)
    }
}