package com.sap.codelab.utils.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sap.codelab.R
import com.sap.codelab.data.model.Memo
import com.sap.codelab.ui.detail.ViewMemoActivity
import com.sap.codelab.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * Creates and displays memo notifications triggered by location reminders.
 */
internal class NotificationUtil @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Shows a notification for the given memo.
     *
     * @return true if the notification can be posted, false otherwise.
     */
    @SuppressLint("MissingPermission")
    fun showMemoNotification(memo: Memo): Boolean {

        if (!canPostNotifications()) {
            return false
        }

        createNotificationChannel()

        val pendingIntent = createPendingIntent(memo.id)

        val description = memo.description.take(MAX_DESCRIPTION_LENGTH)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle(memo.title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(memo.id.hashCode(), notification)

        return true
    }

    /*
    * Creates a pending intent for the given memo.
     */
    private fun createPendingIntent(memoId: Long): PendingIntent {

        val intent = Intent(context, ViewMemoActivity::class.java).apply {
            putExtra(Constants.BUNDLE_MEMO_ID, memoId)
        }

        return PendingIntent.getActivity(
            context,
            memoId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Notification channels are required on Android 8.0 (API 26) and above.
     */
    private fun createNotificationChannel() {

        /**
         * The project currently targets higher Android versions,
         * but this guard is kept for backward compatibility and safer API usage.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Checks both runtime notification permission and
     * application-level notification settings.
     */
    private fun canPostNotifications(): Boolean {

        val notificationManager = NotificationManagerCompat.from(context)

        if (!notificationManager.areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {

        const val CHANNEL_ID = "location_reminder_channel"

        const val CHANNEL_NAME = "Location Reminders"

        const val MAX_DESCRIPTION_LENGTH = 140
    }
}