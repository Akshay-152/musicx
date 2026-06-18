package com.example.musicx2.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.musicx2.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "sync_channel"
    private val DOWNLOAD_CHANNEL_ID = "download_channel"

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val syncChannel = NotificationChannel(
                CHANNEL_ID,
                "Sync Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for sync room events"
            }

            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Download Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for track downloads"
            }

            notificationManager.createNotificationChannel(syncChannel)
            notificationManager.createNotificationChannel(downloadChannel)
        }
    }

    fun showDownloadNotification(trackTitle: String, progress: Int, isFailed: Boolean = false) {
        val iconRes = when {
            isFailed -> android.R.drawable.stat_notify_error
            progress < 100 -> android.R.drawable.stat_sys_download
            else -> android.R.drawable.stat_sys_download_done
        }
        val title = when {
            isFailed -> "Download Failed"
            progress < 100 -> "Downloading..."
            else -> "Download Complete"
        }

        val builder = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(trackTitle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(progress < 100 && !isFailed)
            .setAutoCancel(true)

        if (!isFailed && progress in 0..100) {
            builder.setProgress(100, progress, progress == 0)
        }

        notificationManager.notify(trackTitle.hashCode(), builder.build())
    }

    fun showRoomTerminatedNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_disabled)
            .setContentTitle("Sync Session Terminated")
            .setContentText("The sync room has been closed.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }
}
