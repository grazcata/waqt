package com.graz.waqt.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.graz.waqt.MainActivity
import com.graz.waqt.R
import java.util.Calendar

/**
 * Handles prayer time alarms, notifications and automatic silent mode.
 * Based on Salatuk's AdhanReceiver & SilentService architecture discovered via REA.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prefs = context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE)

        when (action) {
            ACTION_PRE_PRAYER_REMINDER -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prière"
                val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 15)
                showPreReminderNotification(context, prayerName, minutesBefore)
            }
            ACTION_PRAYER_ALARM -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prière"
                val prayerIcon = intent.getStringExtra(EXTRA_PRAYER_ICON) ?: "🕌"

                // 1. Play Adhan audio via AdhanPlaybackService if enabled for this specific prayer
                val adhanAudioEnabled = prefs.getBoolean("adhan_audio_enabled", true)
                val prayerKey = prayerName.lowercase()
                val isSoundForPrayerEnabled = prefs.getBoolean("sound_$prayerKey", true)

                if (adhanAudioEnabled && isSoundForPrayerEnabled) {
                    val adhanStyle = prefs.getString("adhan_style", "maghrebi_2") ?: "maghrebi_2"
                    val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
                        setAction(AdhanPlaybackService.ACTION_START_ADHAN)
                        putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayerName)
                        putExtra(AdhanPlaybackService.EXTRA_ADHAN_STYLE, adhanStyle)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } else if (prefs.getBoolean("notif_enabled", true)) {
                    // Standard notification without full Adhan voice (or silent notification if sound disabled)
                    showPrayerNotification(context, prayerName, prayerIcon)
                }

                // 2. Trigger auto-silent if enabled for this specific prayer
                if (prefs.getBoolean("auto_silent_enabled", false) && isPrayerSilentAllowed(prefs, prayerName)) {
                    val isFriday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
                    val durationMin = if (isFriday && prayerName.equals("Dhuhr", ignoreCase = true) && prefs.getBoolean("silent_jumuaa_special", true)) {
                        prefs.getInt("silent_jumuaa_duration", 45)
                    } else {
                        prefs.getInt("auto_silent_duration", 20)
                    }
                    enableSilentMode(context, durationMin)
                }
            }
            ACTION_RESTORE_RINGER -> {
                restoreRingerMode(context)
            }
        }
    }

    private fun isPrayerSilentAllowed(prefs: android.content.SharedPreferences, prayerName: String): Boolean {
        return when (prayerName.lowercase()) {
            "fajr" -> prefs.getBoolean("silent_fajr", true)
            "dhuhr" -> prefs.getBoolean("silent_dhuhr", true)
            "asr" -> prefs.getBoolean("silent_asr", true)
            "maghrib" -> prefs.getBoolean("silent_maghrib", true)
            "isha" -> prefs.getBoolean("silent_isha", true)
            else -> true
        }
    }

    private fun showPreReminderNotification(context: Context, prayerName: String, minutesBefore: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "waqt_pre_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels d'anticipation",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications avant l'entrée de la prière"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏳ Rappel : $prayerName dans $minutesBefore minutes")
            .setContentText("Préparez-vous pour la prière de $prayerName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(("pre_" + prayerName).hashCode(), notification)
    }

    private fun showPrayerNotification(context: Context, prayerName: String, prayerIcon: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "waqt_prayers"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Horaires de Prière",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications lors de l'entrée de chaque prière"
                enableVibration(true)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("$prayerIcon Heure de la prière : $prayerName")
            .setContentText("C'est l'heure de la prière de $prayerName. حي على الصلاة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }

    private fun enableSilentMode(context: Context, durationMinutes: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check Do Not Disturb policy permission on Android 6+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notifManager.isNotificationPolicyAccessGranted) {
            return
        }

        val previousMode = audioManager.ringerMode
        val prefs = context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("saved_ringer_mode", previousMode).apply()

        // Set to VIBRATE mode
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

        // Schedule restore alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val restoreIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_RESTORE_RINGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RESTORE,
            restoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun restoreRingerMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notifManager.isNotificationPolicyAccessGranted) {
            return
        }

        val prefs = context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE)
        val savedMode = prefs.getInt("saved_ringer_mode", AudioManager.RINGER_MODE_NORMAL)
        audioManager.ringerMode = savedMode
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.graz.waqt.ACTION_PRAYER_ALARM"
        const val ACTION_PRE_PRAYER_REMINDER = "com.graz.waqt.ACTION_PRE_PRAYER_REMINDER"
        const val ACTION_RESTORE_RINGER = "com.graz.waqt.ACTION_RESTORE_RINGER"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PRAYER_ICON = "prayer_icon"
        const val EXTRA_MINUTES_BEFORE = "minutes_before"
        const val REQUEST_CODE_RESTORE = 9999

        /**
         * Schedules exact alarms for all prayers of the day.
         */
        fun schedulePrayerAlarms(
            context: Context,
            fajrHours: Double,
            dhuhrHours: Double,
            asrHours: Double,
            maghribHours: Double,
            ishaHours: Double
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val prayers = listOf(
                Triple("Fajr", "🌙", fajrHours),
                Triple("Dhuhr", "☀️", dhuhrHours),
                Triple("Asr", "🌤️", asrHours),
                Triple("Maghrib", "🌇", maghribHours),
                Triple("Isha", "🌃", ishaHours),
            )

            val now = Calendar.getInstance()
            val nowSec = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND)

            prayers.forEachIndexed { index, (name, icon, hours) ->
                val targetSec = (hours * 3600).toInt()
                val cal = Calendar.getInstance().apply {
                    val h = hours.toInt()
                    val m = ((hours - h) * 60).toInt()
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (targetSec <= nowSec) {
                        add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
                    }
                }

                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = ACTION_PRAYER_ALARM
                    putExtra(EXTRA_PRAYER_NAME, name)
                    putExtra(EXTRA_PRAYER_ICON, icon)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    index + 100,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                cal.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            // Fallback to inexact alarm if user hasn't granted exact alarm permission
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                cal.timeInMillis,
                                pendingIntent
                            )
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis,
                            pendingIntent
                        )
                    }

                // Schedule pre-prayer reminder ONLY if master toggle is ON and specific prayer enabled
                val prefs = context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE)
                val preRemindersMasterEnabled = prefs.getBoolean("pre_reminder_enabled", false)
                val preRemindKey = "pre_reminder_" + name.lowercase()

                if (preRemindersMasterEnabled && prefs.getBoolean(preRemindKey, false)) {
                    val preMin = prefs.getInt("pre_reminder_minutes", 15)
                    val preCal = (cal.clone() as Calendar).apply {
                        add(Calendar.MINUTE, -preMin)
                    }
                    if (preCal.timeInMillis > System.currentTimeMillis()) {
                        val preIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                            action = ACTION_PRE_PRAYER_REMINDER
                            putExtra(EXTRA_PRAYER_NAME, name)
                            putExtra(EXTRA_MINUTES_BEFORE, preMin)
                        }
                        val prePendingIntent = PendingIntent.getBroadcast(
                            context,
                            index + 200,
                            preIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (alarmManager.canScheduleExactAlarms()) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preCal.timeInMillis, prePendingIntent)
                                } else {
                                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preCal.timeInMillis, prePendingIntent)
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preCal.timeInMillis, prePendingIntent)
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, preCal.timeInMillis, prePendingIntent)
                            }
                        } catch (_: SecurityException) {}
                    }
                }
                } catch (e: SecurityException) {
                    // Fail gracefully with inexact alarm if security exception occurs
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }
}
