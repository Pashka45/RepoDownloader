package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.utils.cancelNotifications
import com.udacity.utils.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


const val NOTIFICATION_SUCCESS: Byte = 1
const val NOTIFICATION_FAILED: Byte = 0

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var repo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {

            val urlToDownload: String
            when (reposRadioGroup.checkedRadioButtonId) {
                R.id.glideRadioButton -> {
                    urlToDownload = URL_GLIDE
                    repo = getString(R.string.glide)
                }
                R.id.currRepoRadioButton -> {
                    urlToDownload = URL_CURR_REPO
                    repo = getString(R.string.load_app)
                }
                R.id.retrofitRadioButton -> {
                    urlToDownload = URL_RETROFIT
                    repo = getString(R.string.retrofit)
                }
                else -> {
                    urlToDownload = ""
                    repo = ""
                }
            }

            if (urlToDownload != "") {
                download(urlToDownload)
                custom_button.start()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please select option to download",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        createChannel(
            getString(R.string.file_downloaded_notification_channel_id),
            getString(R.string.download_notification_channel_name)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val query = DownloadManager.Query()
                .setFilterById(downloadID)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {

                val status = cursor.getInt(
                    cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                )

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        sendNotification(NOTIFICATION_SUCCESS)
                    }

                    DownloadManager.STATUS_FAILED -> {
                        sendNotification(NOTIFICATION_FAILED)
                    }

                }
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(true)
                .setAllowedOverMetered(false)
                .setAllowedOverRoaming(false)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL_CURR_REPO =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.file_downloaded_notification_channel_id),
                "download",
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun sendNotification(status: Byte) {
        val notificationManager =
            ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.cancelNotifications()

        notificationManager.sendNotification(
            applicationContext.getString(R.string.file_downloaded),
            applicationContext,
            repo,
            status
        )
    }
}
