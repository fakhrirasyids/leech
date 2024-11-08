package com.fakhrirasyids.leech.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.fakhrirasyids.leech.utils.Constants
import com.fakhrirasyids.leech.utils.NotificationUtils.removeNotification

/**
 * LeechNotificationManager handles creating and updating notifications for file download tasks.
 * It manages notification channels, foreground notifications for active downloads, and status broadcasts.
 *
 * @param context The application context.
 * @param notificationIcon The icon resource for the notification.
 * @param notificationImportance The importance level of the notification channel.
 * @param leechFileName The name of the file being downloaded.
 * @param leechId The unique ID for the download task.
 */
internal class LeechNotificationManager(
    private val context: Context,
    private val notificationIcon: Int,
    private val notificationImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    private val leechFileName: String,
    private val leechId: Int
) {
    private lateinit var foregroundInfo: ForegroundInfo

    private val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
        setSmallIcon(notificationIcon)
        setOnlyAlertOnce(true)
        setOngoing(true)
        setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    // Initialize the notification channel on Android O and above.
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()
    }

    /**
     * Posts or updates the progress notification for the download.
     *
     * @param progress The current progress of the download, as a percentage (0-100).
     * @param update Indicates if this is an update to an existing notification.
     * @return A ForegroundInfo object to set this notification as a foreground service.
     */
    fun postUpdateNotification(
        progress: Int = 0,
        update: Boolean = false
    ): ForegroundInfo {
        if (update) {
            notificationBuilder
                .setProgress(DOWNLOAD_MAX_VALUE_PROGRESS, progress, false)
                .setSubText("$progress%")
        } else {
            removeNotification(context, leechId)
            removeNotification(context, leechId + 1)

            val intentOpen = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(Constants.EXTRA_FILE_REQUEST_ID, leechId)
                }

            val pendingIntentOpen = PendingIntent.getActivity(
                context, leechId, intentOpen, PendingIntent.FLAG_IMMUTABLE
            )

            val intentCancel = Intent(context, LeechNotificationReceiver::class.java).apply {
                action = Constants.NOTIFICATION_CANCEL
                putExtra(Constants.EXTRA_NOTIFICATION_ID, leechId)
                putExtra(Constants.EXTRA_FILE_REQUEST_ID, leechId)
            }
            val pendingIntentCancel = PendingIntent.getBroadcast(
                context.applicationContext,
                leechId,
                intentCancel,
                PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder.setContentTitle("Downloading $leechFileName")
                .setContentIntent(pendingIntentOpen)
                .setProgress(DOWNLOAD_MAX_VALUE_PROGRESS, progress, false)
                .addAction(-1, CANCEL_BUTTON_TEXT, pendingIntentCancel)
        }

        foregroundInfo = ForegroundInfo(
            leechId,
            notificationBuilder.build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        )
        return foregroundInfo
    }

    /**
     * Posts a notification indicating a successful download completion and sends a broadcast.
     *
     * @param totalLength The total size of the downloaded file, used in the notification content.
     */
    fun postSuccessfulDownloadNotification(totalLength: Long) {
        sendDownloadStatusBroadcast(
            Constants.FileDownloadStatus.DOWNLOAD_COMPLETE.name,
            totalLength
        )
    }

    /**
     * Posts a notification indicating a failed download and sends a broadcast.
     */
    fun postFailedDownloadNotification() {
        sendDownloadStatusBroadcast(Constants.FileDownloadStatus.DOWNLOAD_FAILED.name)
    }

    /**
     * Sends a broadcast with the current download status to notify other components (e.g., BroadcastReceiver).
     *
     * @param action The action indicating the current download status (e.g., complete, failed).
     * @param totalLength The total size of the downloaded file, if applicable.
     */
    @JvmSynthetic
    private fun sendDownloadStatusBroadcast(
        action: String,
        totalLength: Long = 0
    ) {
        Intent(context, LeechNotificationReceiver::class.java).apply {
            // Notification Resources
            putExtra(Constants.EXTRA_CHANNEL_NAME, CHANNEL_NAME)
            putExtra(Constants.EXTRA_NOTIFICATION_IMPORTANCE, notificationImportance)
            putExtra(Constants.EXTRA_NOTIFICATION_ICON, notificationIcon)
            putExtra(Constants.EXTRA_NOTIFICATION_ID, leechId + 1)

            // Download File Resources
            putExtra(Constants.EXTRA_FILE_NAME, leechFileName)
            putExtra(Constants.EXTRA_FILE_SIZE, totalLength)
            putExtra(Constants.EXTRA_FILE_REQUEST_ID, leechId)

            this.action = action
            context.sendBroadcast(this)
        }
    }

    /**
     * Creates a notification channel for Android O and above to categorize notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, notificationImportance).apply {
            description = "Notifications for Leech file downloads"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "leech_notification_channel"
        const val CHANNEL_NAME = "Leech File Download"

        private const val DOWNLOAD_MAX_VALUE_PROGRESS = 100

        private const val CANCEL_BUTTON_TEXT = "Cancel"
    }
}
