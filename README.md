# Waqt (وقت)

Application Android native (Kotlin / Jetpack Compose) pour les horaires de prière et l'orientation Qibla.

## Fonctionnalités

- ⏳ **Rappels d'anticipation avant la prière avec interrupteur dédié** — interrupteur maître dans les réglages pour activer/désactiver l'ensemble de l'anticipation, avec réglage de 5 à 30 min et choix des prières concernées
- 🔔 **Activation / Désactivation du son par prière** — bouton cloche interactif (🔔 / 🔕) directement sur chaque ligne de prière dans l'écran d'accueil
- 📢 **Moteur Audio d'Adhan à l'heure exacte** — sonne rigoureusement aux heures affichées sur l'écran d'accueil (Fajr, Dhuhr, Asr, Maghrib, Isha) sans confusion avec l'anticipation
- 🎨 **Interface Moderne & Épurée** — design Material 3 soigné, cartes thématiques et typographie soignée
- 🕌 **Nouvelle Icône Vectorielle Mosquée** — dôme islamique stylisé, minarets et croissant doré
- 🕌 **Horaires des 5 prières + Chourouq** — calculs astronomiques locaux (aucune API externe requise)
- 📅 **Calendrier Mensuel Interactif & Dates Hégiriennes** — navigation par mois, calcul des horaires de n'importe quel jour, repères des Jours Blancs (13, 14, 15 Hijri) et Joumou'a
- ⏳ **Rappels d'anticipation avant la prière** — notification d'alerte réglable (de 5 à 30 min avant, ex: Sahur avant Fajr) paramétrable par prière
- 🔕 **Mode silencieux intelligent par prière** — activation individuelle par prière avec prise en charge spéciale du Vendredi (**Joumou'a**, durée de silence allongée pour le prêche)
- 📢 **Moteur Audio d'Adhan complet** — lecture séquentielle avec WakeLock (début → fajr → milieu → fin)
  - **Adhan Marocain officiel** : récitation marocaine authentique (Habous) par défaut
  - **Adhan Maghrébin classique** : style traditionnel n°1
  - **Adhan Haramayn** : récitation de La Mecque / Médine
  - Formule spéciale Fajr incluse automatiquement (*الصلاة خير من النوم*)
- 🚨 **Écran d'alarme plein écran (`AdhanAlarmActivity`)** — s'affiche par-dessus l'écran de verrouillage avec bouton d'arrêt
- 🔔 **Rappels de prière** — alarmes programmées via `AlarmManager` avec notification haute priorité et sonnerie
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
