package com.graz.waqt.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.graz.waqt.util.CalculationMethod
import com.graz.waqt.util.PrayerTimesCalculator
import com.graz.waqt.widget.WaqtLargeWidgetProvider
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Reschedules all prayer alarms and refreshes widgets upon device reboot.
 * Discovered in Salatuk architecture as BootCompletedReceiver.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val prefs = context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE)
            val savedLat = prefs.getFloat("saved_lat", 30.35535f).toDouble()
            val savedLng = prefs.getFloat("saved_lng", -9.53639f).toDouble()

            val now = Date()
            val cal = Calendar.getInstance().apply { time = now }
            val tz = TimeZone.getDefault().getOffset(now.time) / 3600000.0

            val calc = PrayerTimesCalculator(CalculationMethod.MOROCCO)
            val times = calc.calculate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                savedLat,
                savedLng,
                tz
            )

            // Reschedule all alarms seamlessly
            PrayerAlarmReceiver.schedulePrayerAlarms(
                context,
                times.fajrHours,
                times.dhuhrHours,
                times.asrHours,
                times.maghribHours,
                times.ishaHours
            )

            // Update Widgets immediately
            WaqtLargeWidgetProvider.updateAllWidgets(context)
        }
    }
}
