package com.graz.waqt.util

import java.util.Calendar
import java.util.Date
import kotlin.math.floor

/**
 * Hijri (Islamic) Calendar Calculator
 * Pure astronomical conversion using the Kuwaiti / Umm al-Qura standard algorithm.
 * No external API required.
 */
data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameAr: String,
    val monthNameFr: String,
) {
    fun formatAr(): String = "$day $monthNameAr $year هـ"
    fun formatFr(): String = "$day $monthNameFr $year AH"
}

object HijriCalendar {
    private val MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الثانية", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val MONTHS_FR = listOf(
        "Mouharram", "Safar", "Rabi' al-awwal", "Rabi' ath-thani",
        "Joumada al-oula", "Joumada ath-thaniya", "Rajab", "Cha'bane",
        "Ramadan", "Chawwal", "Dhou al-qi'da", "Dhou al-hijja"
    )

    /**
     * Converts a Gregorian Date to HijriDate with an optional manual day adjustment (-2 to +2).
     */
    fun fromGregorian(date: Date = Date(), dayAdjustment: Int = 0): HijriDate {
        val cal = Calendar.getInstance()
        cal.time = date
        if (dayAdjustment != 0) {
            cal.add(Calendar.DAY_OF_MONTH, dayAdjustment)
        }

        var year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH) + 1 // 1-indexed
        val day = cal.get(Calendar.DAY_OF_MONTH)

        if (month < 3) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5

        // Julian Day to Hijri conversion
        val z = jd - 1948439.5
        val cyc = floor(z / 10631.0)
        val rem = z - 10631.0 * cyc
        val j = floor((rem - 0.5) / 354.366)
        val hYear = (30 * cyc + j).toInt() + 1
        val doy = rem - floor(j * 354.366) - 0.5
        val hMonth = minOf(12, maxOf(1, floor((doy + 0.5) / 29.5305).toInt() + 1))
        val hDay = maxOf(1, (doy - floor((hMonth - 1) * 29.5305) + 0.5).toInt() + 1)

        val monthIdx = (hMonth - 1).coerceIn(0, 11)
        return HijriDate(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameAr = MONTHS_AR[monthIdx],
            monthNameFr = MONTHS_FR[monthIdx]
        )
    }
}
