package com.example.androidplayground.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androidplayground.MainActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val uncreatedChannels = mutableListOf<NotificationChannel>()

            if (notificationManager.getNotificationChannel(CHANNEL_URGENT) == null) {
                uncreatedChannels.add(
                    NotificationChannel(
                        CHANNEL_URGENT,
                        "Urgent Notifications",
                        NotificationManager.IMPORTANCE_MAX
                    ).apply { description = "Highest priority notifications" }
                )
            }

            if (notificationManager.getNotificationChannel(CHANNEL_HIGH) == null) {
                uncreatedChannels.add(
                    NotificationChannel(
                        CHANNEL_HIGH,
                        "High Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply { description = "High priority notifications" }
                )
            }

            if (notificationManager.getNotificationChannel(CHANNEL_DEFAULT) == null) {
                uncreatedChannels.add(
                    NotificationChannel(
                        CHANNEL_DEFAULT,
                        "Medium Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Default priority notifications" }
                )
            }

            if (notificationManager.getNotificationChannel(CHANNEL_LOW) == null) {
                uncreatedChannels.add(
                    NotificationChannel(
                        CHANNEL_LOW,
                        "Low Notifications",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply { description = "Low priority notifications" }
                )
            }

            if (uncreatedChannels.isNotEmpty()) {
                val sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                sysManager.createNotificationChannels(uncreatedChannels)
            }
        }
    }

    fun sendNotification(
        notificationId: Int,
        channelId: String,
        title: String,
        text: String,
        appName: String?,
        visibility: Int,
        timestamp: Long = System.currentTimeMillis(),
        actionLabel: String? = null,
        actionTarget: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure we're using a valid resource for the small icon. 
        // Android requires a transparent/white monochrome icon ideally.
        // We'll use android.R.drawable.ic_dialog_info as a generic stand-in.
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(channelIdToPriority(channelId))
            .setVisibility(visibility)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setShowWhen(true)

        if (!appName.isNullOrBlank()) {
            builder.setSubText(appName)
        }

        if (visibility == NotificationCompat.VISIBILITY_PRIVATE || visibility == NotificationCompat.VISIBILITY_SECRET) {
            val publicVersion = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle("New notification")
                .setContentText("Unlock device to view content")
                .build()
            builder.setPublicVersion(publicVersion)
        }

        if (actionLabel != null && actionTarget != null) {
            val actionIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("route", actionTarget)
            }
            val actionPendingIntent = PendingIntent.getActivity(
                context, 1, actionIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val action = NotificationCompat.Action.Builder(
                0, actionLabel, actionPendingIntent
            ).build()
            builder.addAction(action)
        }

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission on Android 13+
            e.printStackTrace()
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun channelIdToPriority(channelId: String): Int {
        return when (channelId) {
            CHANNEL_URGENT -> NotificationCompat.PRIORITY_MAX
            CHANNEL_HIGH -> NotificationCompat.PRIORITY_HIGH
            CHANNEL_DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            CHANNEL_LOW -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    companion object {
        const val CHANNEL_URGENT = "channel_urgent"
        const val CHANNEL_HIGH = "channel_high"
        const val CHANNEL_DEFAULT = "channel_default"
        const val CHANNEL_LOW = "channel_low"
    }
}
