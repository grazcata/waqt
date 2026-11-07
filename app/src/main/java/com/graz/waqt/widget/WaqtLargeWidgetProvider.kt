package com.graz.waqt.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.graz.waqt.MainActivity
import com.graz.waqt.R
import com.graz.waqt.util.CalculationMethod
import com.graz.waqt.util.HijriCalendar
import com.graz.waqt.util.PrayerTimesCalculator
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Android AppWidget reproducing Salatuk's 4x2 Home Screen Widget:
 * - City name (top left)
 * - Hijri Date (top right)
 * - Large emerald banner with next prayer name & countdown (center)
 * - Table of 5 daily prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) at the bottom
 */
class WaqtLargeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WaqtLargeWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.graz.waqt.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WaqtLargeWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE)
            val lat = prefs.getFloat("saved_lat", 30.35535f).toDouble()
            val lng = prefs.getFloat("saved_lng", -9.53639f).toDouble()
            val city = prefs.getString("saved_city", "أكادير") ?: "أكادير"

            val cal = Calendar.getInstance()
            val now = cal.time
            val tz = TimeZone.getDefault().getOffset(now.time) / 3600000.0

            val calc = PrayerTimesCalculator(CalculationMethod.MOROCCO)
            val times = calc.calculate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                lat,
                lng,
                tz
            )
            val hijri = HijriCalendar.fromGregorian(now)

            // Calculate Next Prayer and Countdown
            val nowSec = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)
            val prayers = listOf(
                Triple("الفجر", times.fajr, times.fajrHours),
                Triple("الظهر", times.dhuhr, times.dhuhrHours),
                Triple("العصر", times.asr, times.asrHours),
                Triple("المغرب", times.maghrib, times.maghribHours),
                Triple("العشاء", times.isha, times.ishaHours),
            )

            var nextPrayerName = "الفجر"
            var diffSec = 0
            val found = prayers.firstOrNull { (_, _, h) ->
                (h * 3600).toInt() > nowSec
            }

            if (found != null) {
                nextPrayerName = found.first
                diffSec = (found.third * 3600).toInt() - nowSec
            } else {
                // Next day's Fajr
                nextPrayerName = "الفجر"
                diffSec = (prayers[0].third * 3600).toInt() + 86400 - nowSec
            }

            val hoursLeft = diffSec / 3600
            val minutesLeft = (diffSec % 3600) / 60
            val secondsLeft = diffSec % 60
            val countdownStr = "- %02d:%02d:%02d".format(hoursLeft, minutesLeft, secondsLeft)

            val views = RemoteViews(context.packageName, R.layout.widget_salatuk_large)

            // Header Texts
            views.setTextViewText(R.id.widget_city, city)
            views.setTextViewText(R.id.widget_date, "${hijri.day} ${hijri.monthNameAr} ${hijri.year}")

            // Center Banner
            views.setTextViewText(R.id.widget_next_prayer_name, nextPrayerName)
            views.setTextViewText(R.id.widget_next_prayer_countdown, countdownStr)

            // Table of 5 Prayers
            views.setTextViewText(R.id.widget_time_fajr, times.fajr)
            views.setTextViewText(R.id.widget_time_dhuhr, times.dhuhr)
            views.setTextViewText(R.id.widget_time_asr, times.asr)
            views.setTextViewText(R.id.widget_time_maghrib, times.maghrib)
            views.setTextViewText(R.id.widget_time_isha, times.isha)

            // Color highlight active prayer in table
            fun getHighlightColor(name: String): Int =
                if (name == nextPrayerName) Color.parseColor("#3FB68B") else Color.parseColor("#AAAAAA")

            views.setTextColor(R.id.widget_name_fajr, getHighlightColor("الفجر"))
            views.setTextColor(R.id.widget_name_dhuhr, getHighlightColor("الظهر"))
            views.setTextColor(R.id.widget_name_asr, getHighlightColor("العصر"))
            views.setTextColor(R.id.widget_name_maghrib, getHighlightColor("المغرب"))
            views.setTextColor(R.id.widget_name_isha, getHighlightColor("العشاء"))

            // Global Click to open main application on the ENTIRE widget surface
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_city, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_date, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_next_prayer_name, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_next_prayer_countdown, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_col_fajr, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_col_dhuhr, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_col_asr, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_col_maghrib, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_col_isha, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
