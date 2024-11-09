package com.fakhrirasyids.leech.services.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fakhrirasyids.leech.Leech
import com.fakhrirasyids.leech.utils.Constants
import com.fakhrirasyids.leech.utils.ConverterUtil
import com.fakhrirasyids.leech.utils.FileDownloadStatus

/**
 * LeechNotificationReceiver is a BroadcastReceiver that handles notifications related to
 * download actions, such as canceling or displaying the status of a download.
 */
internal class LeechNotificationReceiver : BroadcastReceiver() {

    /**
     * Called when the receiver receives a broadcast. Handles download notifications and actions.
     * Cancels the notification if required, updates the notification with download status, or
     * opens the application when a notification is clicked.
     *
     * @param context The application context.
     * @param intent The received Intent containing action and extra data.
     */
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action

        val notificationChannelName = intent.getStringExtra(Constants.EXTRA_CHANNEL_NAME)
            ?: LeechNotificationManager.CHANNEL_NAME
        val notificationImportance = intent.getIntExtra(
            Constants.EXTRA_NOTIFICATION_IMPORTANCE,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationSmallIcon = intent.getIntExtra(
            Constants.EXTRA_NOTIFICATION_ICON,
            android.R.drawable.stat_sys_download
        )
        val fileName = intent.getStringExtra(Constants.EXTRA_FILE_NAME) ?: ""
        val totalLength = intent.getLongExtra(Constants.EXTRA_FILE_SIZE, -1)
        val leechId = intent.getIntExtra(Constants.EXTRA_FILE_REQUEST_ID, -1)
        val notificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1)

        val leechApplication =
            Leech.getInstance(context, notificationSmallIcon, notificationImportance)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context, notificationChannelName, notificationImportance)
        }

        if (intent.action == Constants.NOTIFICATION_CANCEL) {
            NotificationManagerCompat.from(context).cancel(notificationId)
            leechApplication.cancelDownloadItem(leechId)
            return
        }

        val intentOpen = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(Constants.EXTRA_FILE_REQUEST_ID, leechId)
            }

        val pendingIntentOpen = PendingIntent.getActivity(
            context, notificationId, intentOpen, PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = when (action) {
            FileDownloadStatus.DOWNLOAD_COMPLETE.name -> StringBuilder(
                "Download successful (${
                    ConverterUtil.getFormattedFileSize(
                        totalLength
                    )
                })."
            )

            FileDownloadStatus.DOWNLOAD_FAILED.name -> "Download failed."
            else -> "Download status unknown."
        }

        val notification = NotificationCompat.Builder(context, LeechNotificationManager.CHANNEL_ID)
            .setSmallIcon(notificationSmallIcon)
            .setContentTitle(fileName)
            .setContentText(contentText)
            .setContentIntent(pendingIntentOpen)
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Creates a notification channel for Android O and above.
     * This is required to display notifications with specific properties (like importance).
     *
     * @param context The application context.
     * @param channelName The name of the notification channel.
     * @param importance The importance level for the channel.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        context: Context,
        channelName: String,
        importance: Int
    ) {
        val channel = NotificationChannel(
            LeechNotificationManager.CHANNEL_ID,
            channelName,
            importance
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

