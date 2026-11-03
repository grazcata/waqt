package com.graz.waqt.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.graz.waqt.AdhanAlarmActivity
import com.graz.waqt.R

/**
 * Foreground service to play the sequential multi-part Adhan audio with WakeLock.
 * Sequentially plays: debut -> [fajr if Fajr prayer] -> milieu -> fin
 * Inspired by Salatuk's AdhanService architecture.
 */
class AdhanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val playlist = mutableListOf<Int>()
    private var currentIndex = 0
    private var prayerName: String = "Prière"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ADHAN) {
            stopAdhan()
            return START_NOT_STICKY
        }

        prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prière"
        val isFajr = prayerName.equals("Fajr", ignoreCase = true)
        val style = intent?.getStringExtra(EXTRA_ADHAN_STYLE) ?: "maghrebi"

        acquireWakeLock()
        startForegroundServiceNotification()
        buildPlaylist(style, isFajr)
        playNextTrack()

        // Also launch the full screen alarm activity
        val alarmIntent = Intent(this, AdhanAlarmActivity::class.java).apply {
            putExtra(AdhanAlarmActivity.EXTRA_PRAYER_NAME, prayerName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(alarmIntent)

        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "waqt:AdhanPlaybackWakeLock"
        ).apply {
            acquire(5 * 60 * 1000L) // 5 minutes max
        }
    }

    private fun buildPlaylist(style: String, isFajr: Boolean) {
        playlist.clear()
        currentIndex = 0

        when (style) {
            "maghrebi_2" -> {
                // Style Marocain n°2 (Salatuk officiel)
                playlist.add(R.raw.maghrebi_02_debut)
                if (isFajr) playlist.add(R.raw.maghrebi_02_fajr)
                playlist.add(R.raw.maghrebi_02_milieu)
                playlist.add(R.raw.maghrebi_02_fin)
            }
            "haramayn" -> {
                playlist.add(R.raw.haramayn_01_debut)
                if (isFajr) playlist.add(R.raw.haramayn_01_fajr)
                playlist.add(R.raw.haramayn_01_milieu)
                playlist.add(R.raw.haramayn_01_fin)
            }
            else -> {
                // Style Marocain n°1 (Maghrebi traditionnel)
                playlist.add(R.raw.maghrebi_01_debut)
                if (isFajr) playlist.add(R.raw.maghrebi_01_fajr)
                playlist.add(R.raw.maghrebi_01_milieu)
                playlist.add(R.raw.maghrebi_01_fin)
            }
        }
    }

    private fun playNextTrack() {
        if (currentIndex >= playlist.size) {
            stopAdhan()
            return
        }

        val rawResId = playlist[currentIndex]
        currentIndex++

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(
                this,
                rawResId,
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build(),
                0
            )?.apply {
                setOnCompletionListener {
                    playNextTrack()
                }
                setOnErrorListener { _, _, _ ->
                    stopAdhan()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            stopAdhan()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "waqt_adhan_playback"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lecture de l'Adhan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Affiché pendant la récitation de l'Adhan"
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 Adhan en cours : $prayerName")
            .setContentText("Appuyez pour couper le son de l'Adhan")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_media_pause, "Arrêter l'Adhan", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopAdhan() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAdhan()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_ADHAN = "com.graz.waqt.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.graz.waqt.ACTION_STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_ADHAN_STYLE = "extra_adhan_style"
        private const val NOTIFICATION_ID = 2001
    }
}
