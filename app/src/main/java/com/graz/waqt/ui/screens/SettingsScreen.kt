package com.graz.waqt.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graz.waqt.AppState
import com.graz.waqt.util.AsrJuristic
import com.graz.waqt.util.CalculationMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: AppState,
    onUpdate: (AppState) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("waqt_prefs", Context.MODE_PRIVATE) }

    var expandedMethod by remember { mutableStateOf(false) }
    var expandedAsr by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Réglages",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Personnalisation & calculs",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // SECTION 1 : Calcul astronomique
        SettingsSectionHeader(
            icon = Icons.Default.Calculate,
            title = "CALCUL ASTRONOMIQUE",
            subtitle = "Conventions & règles fiqh"
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                // Method dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedMethod,
                    onExpandedChange = { expandedMethod = it }
                ) {
                    OutlinedTextField(
                        value = state.method.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Méthode / Convention") },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMethod) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMethod,
                        onDismissRequest = { expandedMethod = false }
                    ) {
                        CalculationMethod.entries.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(m.label, fontWeight = FontWeight.Medium)
                                        Text(
                                            "Fajr: ${m.fajrAngle}° • Isha: ${if (m.ishaMinutes > 0) "${m.ishaMinutes}min" else "${m.ishaAngle}°"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    onUpdate(state.copy(method = m))
                                    expandedMethod = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Asr Juristic dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedAsr,
                    onExpandedChange = { expandedAsr = it }
                ) {
                    OutlinedTextField(
                        value = state.asrJuristic.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Calcul du Asr (École)") },
                        leadingIcon = {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedAsr) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAsr,
                        onDismissRequest = { expandedAsr = false }
                    ) {
                        AsrJuristic.entries.forEach { a ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(a.label, fontWeight = FontWeight.Medium)
                                        Text(
                                            if (a == AsrJuristic.SHAFI) "Ombre = 1x taille de l'objet (Majorité)" else "Ombre = 2x taille de l'objet",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    onUpdate(state.copy(asrJuristic = a))
                                    expandedAsr = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // SECTION 2 : Voix de l'Adhan & Style
        SettingsSectionHeader(
            icon = Icons.Default.Campaign,
            title = "APPEL À LA PRIÈRE (ADHAN)",
            subtitle = "Récitations & volume"
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Voix complète de l'Adhan",
                    subtitle = "Joue l'Adhan vocal à l'heure exacte",
                    checked = state.adhanAudioEnabled,
                    onCheckedChange = { checked ->
                        prefs.edit().putBoolean("adhan_audio_enabled", checked).apply()
                        onUpdate(state.copy(adhanAudioEnabled = checked))
                    }
                )

                if (state.adhanAudioEnabled) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Style de récitation",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AudioStyleCard(
                            title = "Marocain officiel",
                            subtitle = "Style authentique (Habous)",
                            selected = state.adhanStyle == "maghrebi_2",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                prefs.edit().putString("adhan_style", "maghrebi_2").apply()
                                onUpdate(state.copy(adhanStyle = "maghrebi_2"))
                            }
                        )
                        AudioStyleCard(
                            title = "Maghrébin classique",
                            subtitle = "Style traditionnel",
                            selected = state.adhanStyle == "maghrebi",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                prefs.edit().putString("adhan_style", "maghrebi").apply()
                                onUpdate(state.copy(adhanStyle = "maghrebi"))
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    AudioStyleCard(
                        title = "Haramayn",
                        subtitle = "La Mecque & Médine",
                        selected = state.adhanStyle == "haramayn",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            prefs.edit().putString("adhan_style", "haramayn").apply()
                            onUpdate(state.copy(adhanStyle = "haramayn"))
                        }
                    )
                }

                Divider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(0.2f))

                SettingsSwitchRow(
                    title = "Retourner pour couper l'Adhan",
                    subtitle = "Posez le téléphone face contre table pour couper le son",
                    checked = state.flipToMuteEnabled,
                    onCheckedChange = { checked ->
                        prefs.edit().putBoolean("flip_to_mute_enabled", checked).apply()
                        onUpdate(state.copy(flipToMuteEnabled = checked))
                    }
                )

                Divider(Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(0.2f))

                SettingsSwitchRow(
                    title = "Notifications simples",
                    subtitle = "Bannière et sonnerie discrète si l'Adhan est coupé",
                    checked = state.notifEnabled,
                    onCheckedChange = { checked ->
                        prefs.edit().putBoolean("notif_enabled", checked).apply()
                        onUpdate(state.copy(notifEnabled = checked))
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // SECTION 3 : Rappels d'anticipation
        SettingsSectionHeader(
            icon = Icons.Default.HourglassTop,
            title = "RAPPELS D'ANTICIPATION",
            subtitle = "Avant l'entrée de la prière"
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Activer les rappels d'anticipation",
                    subtitle = "Notification envoyée avant l'heure de la prière",
                    checked = state.preReminderEnabled,
                    onCheckedChange = { checked ->
                        prefs.edit().putBoolean("pre_reminder_enabled", checked).apply()
                        onUpdate(state.copy(preReminderEnabled = checked))
                    }
                )

                if (state.preReminderEnabled) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Délai d'anticipation", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(0.12f)
                        ) {
                            Text(
                                text = "${state.preReminderMinutes} min avant",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Slider(
                        value = state.preReminderMinutes.toFloat(),
                        onValueChange = { min ->
                            val m = min.toInt()
                            prefs.edit().putInt("pre_reminder_minutes", m).apply()
                            onUpdate(state.copy(preReminderMinutes = m))
                        },
                        valueRange = 5f..30f,
                        steps = 4
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Prières avec rappel anticipé :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    Spacer(Modifier.height(6.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SelectablePrayerChip(
                            label = "Fajr (Sahur)",
                            selected = state.preReminderFajr,
                            onClick = {
                                val v = !state.preReminderFajr
                                prefs.edit().putBoolean("pre_reminder_fajr", v).apply()
                                onUpdate(state.copy(preReminderFajr = v))
                            }
                        )
                        SelectablePrayerChip(
                            label = "Dhuhr",
                            selected = state.preReminderDhuhr,
                            onClick = {
                                val v = !state.preReminderDhuhr
                                prefs.edit().putBoolean("pre_reminder_dhuhr", v).apply()
                                onUpdate(state.copy(preReminderDhuhr = v))
                            }
                        )
                        SelectablePrayerChip(
                            label = "Asr",
                            selected = state.preReminderAsr,
                            onClick = {
                                val v = !state.preReminderAsr
                                prefs.edit().putBoolean("pre_reminder_asr", v).apply()
                                onUpdate(state.copy(preReminderAsr = v))
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SelectablePrayerChip(
                            label = "Maghrib (Iftar)",
                            selected = state.preReminderMaghrib,
                            onClick = {
                                val v = !state.preReminderMaghrib
                                prefs.edit().putBoolean("pre_reminder_maghrib", v).apply()
                                onUpdate(state.copy(preReminderMaghrib = v))
                            }
                        )
                        SelectablePrayerChip(
                            label = "Isha",
                            selected = state.preReminderIsha,
                            onClick = {
                                val v = !state.preReminderIsha
                                prefs.edit().putBoolean("pre_reminder_isha", v).apply()
                                onUpdate(state.copy(preReminderIsha = v))
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // SECTION 4 : Mode Silencieux
        SettingsSectionHeader(
            icon = Icons.Default.VolumeOff,
            title = "MODE SILENCIEUX AUTOMATIQUE",
            subtitle = "Tranquillité pendant la prière"
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Mode silencieux intelligent",
                    subtitle = "Bascule en vibreur à l'Adhan puis restaure",
                    checked = state.autoSilentEnabled,
                    onCheckedChange = { checked ->
                        prefs.edit().putBoolean("auto_silent_enabled", checked).apply()
                        onUpdate(state.copy(autoSilentEnabled = checked))
                    }
                )

                if (state.autoSilentEnabled) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Durée du silence", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(0.12f)
                        ) {
                            Text(
                                text = "${state.autoSilentDuration} min",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Slider(
                        value = state.autoSilentDuration.toFloat(),
                        onValueChange = { dur ->
                            val d = dur.toInt()
                            prefs.edit().putInt("auto_silent_duration", d).apply()
                            onUpdate(state.copy(autoSilentDuration = d))
                        },
                        valueRange = 10f..45f,
                        steps = 6
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("Prières concernées :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SelectablePrayerChip(
                            label = "Fajr",
                            selected = state.silentFajr,
                            onClick = {
                                val v = !state.silentFajr
                                prefs.edit().putBoolean("silent_fajr", v).apply()
                                onUpdate(state.copy(silentFajr = v))
                            }
                        )
                        SelectablePrayerChip(
                            label = "Dhuhr",
                            selected = state.silentDhuhr,
                            onClick = {
                                val v = !state.silentDhuhr
                                prefs.edit().putBoolean("silent_dhuhr", v).apply()
                                onUpdate(state.copy(silentDhuhr = v))
                            }
                        )
                        SelectablePrayerChip(
                            label = "Asr",
                            selected = state.silentAsr,
                            onClick = {
                                val v = !state.silentAsr
                                prefs.edit().putBoolean("silent_asr", v).apply()
                                onUpdate(state.copy(silentAsr = v))
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SelectablePrayerChip(
                            label = "Maghrib",
                            selected = state.silentMaghrib,
                            onClick = {
                                val v = !state.silentMaghrib
                                prefs.edit().putBoolean("silent_maghrib", v).apply()
                                onUpdate(state.copy(silentMaghrib = v))
                            }
                        )
                        SelectablePrayerChip(
                            label = "Isha",
                            selected = state.silentIsha,
                            onClick = {
                                val v = !state.silentIsha
                                prefs.edit().putBoolean("silent_isha", v).apply()
                                onUpdate(state.copy(silentIsha = v))
                            }
                        )
                    }

                    Divider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(0.2f))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Vendredi (Joumou'a) spécial", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "Silence allongé à ${state.silentJumuaaDuration} min pour le prêche (Khutba)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                        Switch(
                            checked = state.silentJumuaaSpecial,
                            onCheckedChange = { checked ->
                                prefs.edit().putBoolean("silent_jumuaa_special", checked).apply()
                                onUpdate(state.copy(silentJumuaaSpecial = checked))
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // SECTION 5 : Informations & Ville
        SettingsSectionHeader(
            icon = Icons.Default.Info,
            title = "INFORMATIONS LOCALES",
            subtitle = "Données GPS & Hégiriennes"
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                InfoItemRow(icon = Icons.Default.LocationCity, label = "Ville détectée", value = state.city)
                InfoItemRow(
                    icon = Icons.Default.PinDrop,
                    label = "Coordonnées",
                    value = "Lat: %.4f • Lng: %.4f".format(state.lat, state.lng)
                )
                InfoItemRow(
                    icon = Icons.Default.Explore,
                    label = "Direction Qibla",
                    value = "%.1f° depuis le Nord".format(state.qiblaAngle)
                )
                InfoItemRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Aujourd'hui (Hégirien)",
                    value = state.hijriDate.formatAr()
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // App Footer Brand
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "وقت • WAQT",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                "Version 2.1.0 • Usage personnel\nDesign émeraude & or unifié (Navigation bar)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.4f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ─── Helper UI Components ───

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AudioStyleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }
    }
}

@Composable
fun SelectablePrayerChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
        }
    }
}

@Composable
fun InfoItemRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(0.8f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
