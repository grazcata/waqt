package com.graz.waqt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graz.waqt.AppState
import com.graz.waqt.util.HijriCalendar
import com.graz.waqt.util.HijriDate
import com.graz.waqt.util.PrayerTimesCalculator
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val gregorianDay: Int,
    val gregorianDate: Date,
    val hijriDate: HijriDate,
    val isToday: Boolean,
    val isWhiteDay: Boolean, // Ayyam al-Bid (13, 14, 15 Hijri)
    val isFriday: Boolean
)

@Composable
fun MonthlyCalendarScreen(state: AppState) {
    var calendarMonthOffset by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf(Date()) }

    val currentMonthCalendar = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthYearTitle = remember(currentMonthCalendar) {
        SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(currentMonthCalendar.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
    }

    // Days generation for current month
    val daysInMonth = remember(currentMonthCalendar) {
        val days = mutableListOf<CalendarDay?>()
        val cal = currentMonthCalendar.clone() as Calendar
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Day of week of first day (1 = Sunday, 2 = Monday in standard Calendar)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 to 7
        val emptySlotsBefore = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        for (i in 0 until emptySlotsBefore) {
            days.add(null)
        }

        val todayCal = Calendar.getInstance()
        for (d in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            val isToday = cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
            val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            val hijri = HijriCalendar.fromGregorian(cal.time)
            val isWhiteDay = hijri.day in 13..15

            days.add(
                CalendarDay(
                    gregorianDay = d,
                    gregorianDate = cal.time,
                    hijriDate = hijri,
                    isToday = isToday,
                    isWhiteDay = isWhiteDay,
                    isFriday = isFriday
                )
            )
        }
        days
    }

    // Calculate prayer times for selected day
    val selectedTimes = remember(selectedDate, state.method, state.asrJuristic) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        val tz = TimeZone.getDefault().rawOffset / 3600000.0
        val calc = PrayerTimesCalculator(state.method, state.asrJuristic)
        calc.calculate(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            state.lat,
            state.lng,
            tz
        )
    }

    val selectedHijri = remember(selectedDate) {
        HijriCalendar.fromGregorian(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { calendarMonthOffset-- }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mois précédent")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthYearTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${daysInMonth.filterNotNull().firstOrNull()?.hijriDate?.monthNameAr ?: ""} / ${daysInMonth.filterNotNull().lastOrNull()?.hijriDate?.monthNameAr ?: ""}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = { calendarMonthOffset++ }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mois suivant")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Weekday headers (Lun, Mar, Mer, Jeu, Ven, Sam, Dim)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEachIndexed { idx, day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (idx == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Days Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(8.dp)) {
                val chunkedDays = daysInMonth.chunked(7)
                chunkedDays.forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    val isSelected = remember(selectedDate, day.gregorianDate) {
                                        val cal1 = Calendar.getInstance().apply { time = selectedDate }
                                        val cal2 = Calendar.getInstance().apply { time = day.gregorianDate }
                                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    day.isToday -> MaterialTheme.colorScheme.primary.copy(0.18f)
                                                    day.isWhiteDay -> MaterialTheme.colorScheme.secondary.copy(0.18f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDate = day.gregorianDate },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${day.gregorianDay}",
                                                fontSize = 13.sp,
                                                fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    day.isFriday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Text(
                                                text = "${day.hijriDate.day}",
                                                fontSize = 9.sp,
                                                color = when {
                                                    isSelected -> Color.White.copy(0.85f)
                                                    day.isWhiteDay -> MaterialTheme.colorScheme.secondary
                                                    else -> MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Legend: White days / Friday
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🟡 Jours blancs (13, 14, 15 Hijri)", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            Text("🟢 Vendredi (Joumou'a)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(16.dp))

        // Selected Day Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH).format(selectedDate)
                        .replaceFirstChar { it.uppercase() },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedHijri.formatAr(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                val prayers = listOf(
                    Triple("Fajr", "🌙", selectedTimes.fajr),
                    Triple("Lever", "🌅", selectedTimes.sunrise),
                    Triple("Dhuhr", "☀️", selectedTimes.dhuhr),
                    Triple("Asr", "🌤️", selectedTimes.asr),
                    Triple("Maghrib", "🌇", selectedTimes.maghrib),
                    Triple("Isha", "🌃", selectedTimes.isha),
                )

                prayers.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { (name, icon, time) ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$icon $name", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                                Text(time, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
