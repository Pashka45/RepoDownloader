package com.udacity.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.R

private const val NOTIFICATION_ID = 0

const val FIELD_NOTIFICATION_ID = "notification_id"
const val FIELD_FILE_NAME = "file_name"
const val FIELD_STATUS = "status"

fun NotificationManager.sendNotification(
    messageBody: String,
    applicationContext: Context,
    fileName: String,
    status: Byte
) {

    val detailIntent = Intent(applicationContext, DetailActivity::class.java).apply {
        putExtra(FIELD_NOTIFICATION_ID, NOTIFICATION_ID)
        putExtra(FIELD_FILE_NAME, fileName)
        putExtra(FIELD_STATUS, status)
    }

    val detailPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        detailIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.file_downloaded_notification_channel_id)
    )
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(
            R.drawable.ic_launcher_background,
            applicationContext.getString(R.string.show_details),
            detailPendingIntent
        )

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}
