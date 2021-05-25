package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selectedRepo: Repo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().apply {
                    setFilterById(downloadID)
                }
                val cursor = downloadManager.query(query)
                var statusCode = -1
                if (cursor.moveToFirst()) {
                    statusCode = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                }
                val status = when (statusCode) {
                    DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.download_success)
                    DownloadManager.STATUS_FAILED -> getString(R.string.download_fail)
                    else -> getString(R.string.download_unknown)
                }
                sendNotification(status)
                custom_button.setState(ButtonState.Idle)
            }
        }
    }

    private fun sendNotification(status: String) {
        val repo = selectedRepo ?: return

        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_FILE_NAME, repo.description)
        intent.putExtra(DetailActivity.EXTRA_STATUS, status)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val action = NotificationCompat.Action.Builder(
                0, getString(R.string.notification_button), pendingIntent).build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_description, repo.name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(action)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
    }

    private fun download() {
        selectedRepo = getSelectedRepo()
        if (selectedRepo == null) {
            Toast.makeText(this, R.string.toast_select_file, Toast.LENGTH_SHORT).show()
            return
        }

        val request =
                DownloadManager.Request(Uri.parse(selectedRepo!!.url))
                        .setTitle(getString(R.string.app_name))
                        .setDescription(getString(R.string.app_description))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        custom_button.setState(ButtonState.Loading)
    }

    private fun getSelectedRepo(): Repo? {
        return when (radio_group.checkedRadioButtonId) {
            R.id.radio_glide -> Repo(
                    getString(R.string.glide_name),
                    getString(R.string.glide_description),
                    getString(R.string.glide_url))
            R.id.radio_load_app -> Repo(
                    getString(R.string.load_app_name),
                    getString(R.string.load_app_description),
                    getString(R.string.load_app_url))
            R.id.radio_retrofit -> Repo(
                    getString(R.string.retrofit_name),
                    getString(R.string.retrofit_description),
                    getString(R.string.retrofit_url))
            else -> null
        }
    }

    private data class Repo(val name: String, val description: String, val url: String)

    companion object {
        private const val CHANNEL_ID = "defaultChannel"
        const val NOTIFICATION_ID = 1
    }

}
