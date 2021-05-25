package com.udacity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        text_detail_file_name.text = intent.getStringExtra(EXTRA_FILE_NAME)
        text_detail_status.text = intent.getStringExtra(EXTRA_STATUS)
        when (text_detail_status.text) {
            getString(R.string.download_fail) -> text_detail_status.setTextColor(Color.RED)
            getString(R.string.download_unknown) -> text_detail_status.setTextColor(Color.YELLOW)
        }

        button_detail_ok.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        NotificationManagerCompat.from(this).cancel(MainActivity.NOTIFICATION_ID)
    }

    companion object {
        const val EXTRA_FILE_NAME = "extra_file_name"
        const val EXTRA_STATUS = "extra_status"
    }
}
