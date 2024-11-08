package com.fakhrirasyids.leech.utils

import android.content.Context
import androidx.core.app.NotificationManagerCompat

internal object NotificationUtils {

    @JvmSynthetic
    fun removeNotification(context: Context, notificationId: Int) =
        NotificationManagerCompat.from(context).cancel(notificationId)
}