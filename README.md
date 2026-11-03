# Waqt (وقت)

Application Android native (Kotlin / Jetpack Compose) pour les horaires de prière et l'orientation Qibla.

## Fonctionnalités

- 🕌 **Horaires des 5 prières + Chourouq** — calculs astronomiques locaux (aucune API externe requise)
- 📢 **Moteur Audio d'Adhan complet** — lecture séquentielle avec WakeLock (début → fajr → milieu → fin)
  - Deux styles de récitation inclus : **Maghrébin (Maroc)** et **Haramayn (Makkah)**
  - Formule spéciale Fajr incluse automatiquement (*الصلاة خير من النوم*)
- 🚨 **Écran d'alarme plein écran (`AdhanAlarmActivity`)** — s'affiche par-dessus l'écran de verrouillage avec bouton d'arrêt
- 📅 **Calendrier Hégirien (Hijri)** — conversion astronomique locale avec affichage en arabe et en français
- 🔔 **Rappels de prière** — alarmes programmées via `AlarmManager` avec notification haute priorité et sonnerie
- 🔕 **Mode silencieux automatique** — bascule automatiquement en vibreur pendant la prière puis restaure le volume initial (durée réglable de 10 à 45 min)
- 🧭 **Boussole Qibla dynamique** — pointe vers la Kaaba avec les capteurs du téléphone (magnétomètre + accéléromètre)
- ⏱️ **Compte à rebours dynamique** — temps restant avant la prochaine prière
- 📍 **Géolocalisation automatique** — détection de la ville et adaptation de la méthode
- 🌙 **10 méthodes de calcul** intégrées :
  - Habous (Maroc - 19°/17°)
  - Ligue Islamique Mondiale (MWL)
  - ISNA (Amérique du Nord)
  - Autorité Générale Égyptienne
  - Umm al-Qura (Makkah)
  - Université de Karachi
  - Institut de Téhéran
  - Diyanet (Turquie)
  - UOIF (France - 12°/12°)
  - Région du Golfe
- ⚖️ **Écoles juridiques pour le Asr** — Shafi'i / Maliki / Hanbali (ombre x1) et Hanafi (ombre x2)
- 🌓 **Thème sombre / clair** automatique

## Téléchargement

Les APK signés sont disponibles dans la section [Releases](https://github.com/grazcata/waqt/releases).

## Compilation

```bash
export ANDROID_HOME=/home/graz/android-sdk
./gradlew assembleRelease
```

L'APK sera généré dans `app/build/outputs/apk/release/Waqt-v<version>.apk`.

## Licence

Usage personnel.
