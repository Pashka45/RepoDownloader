package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.utils.FIELD_FILE_NAME
import com.udacity.utils.FIELD_NOTIFICATION_ID
import com.udacity.utils.FIELD_STATUS
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val notificationID = intent.getIntExtra(FIELD_NOTIFICATION_ID, -1)

        if (notificationID != -1) {
            val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.cancel(notificationID)
        }

        file_name.text = intent.getStringExtra(FIELD_FILE_NAME)

        when (intent.getByteExtra(FIELD_STATUS, -1)) {
            NOTIFICATION_SUCCESS -> {
                status_text.text = getString(R.string.success)
                status_text.setTextColor(getColor(R.color.success_color))
            }
            NOTIFICATION_FAILED -> {
                status_text.text = getString(R.string.failed)
                status_text.setTextColor(getColor(R.color.failed_color))
            }

        }

        show_main_activity.setOnClickListener {
           openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
