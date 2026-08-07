# 🌀 TyphoonEye

[![Build Android APK](https://github.com/Seamain/TyphoonEyeAndroid/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Seamain/TyphoonEyeAndroid/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/API-29%2B-3DDC84.svg?style=flat&logo=android)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

English | [中文](README_CN.md)

**TyphoonEye** is a modern, feature-packed Android application built with **Jetpack Compose** and **Material Design 3** for real-time typhoon tracking, quadrant wind radius visualization, and location-aware weather alerts.

---

## ✨ Features

- 🌀 **Interactive Map & Wind Radii Visualization**: Real-time rendering of typhoon historical tracks, forecast paths, and 7-kt / 10-kt / 12-kt 4-quadrant wind radius polygons using MapLibre vector engine.
- 📍 **Location-Aware Proximity Alerts**: Calculates real-time distance from the user's location to active typhoon centers and issues emergency warnings.
- 📊 **Comprehensive Typhoon Details**: View central air pressure, max sustained wind speed, movement speed, moving direction, and detailed time-series observation data.
- 🔔 **Live Status & Background Updates**: Periodic background updates powered by `WorkManager` with system status notifications.
- 🎨 **Material Design 3 & Dynamic Color**: Fully adopts Material 3 design system, supporting system dark/light themes and Android 12+ Monet dynamic color palette.
- 🌐 **Multilingual Support**: Supports Simplified Chinese (简体中文), Traditional Chinese (繁體中文), Cantonese (粵語), and English.
- 📜 **Open Source Credits & Privacy**: Built-in Open Source Licenses display and offline cache capability.

---

## 🛠 Tech Stack & Architecture

TyphoonEye is engineered adhering to **Clean Architecture** and **MVVM** principles with standard Android Jetpack libraries.

- **UI & Navigation**: [Jetpack Compose](https://developer.android.com/jetpack/compose), [Material 3](https://m3.material.io/), Navigation Compose
- **Map Engine**: [MapLibre Android SDK](https://github.com/maplibre/maplibre-native) (Vector map rendering)
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) (Dagger)
- **Concurrency & State**: Kotlin Coroutines, StateFlow, SharedFlow
- **Network**: [Retrofit 2](https://square.github.io/retrofit/), [OkHttp 4](https://square.github.io/okhttp/), Custom Ed25519 JWT Authentication
- **Persistence & Storage**: [Room Database](https://developer.android.com/training/data-storage/room), [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **CI/CD Pipeline**: GitHub Actions for automated APK packaging and Release publishing.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.1+) or newer
- JDK 21
- Android SDK 36 (minSdk 29)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/Seamain/TyphoonEyeAndroid.git
   cd TyphoonEyeAndroid
   ```

2. (Optional) Configure QWeather API Keys:
   Copy `local.properties.example` to `local.properties` and add your API credentials:
   ```properties
   QWEATHER_API_KEY=your_qweather_api_key
   QWEATHER_KID=your_key_id
   QWEATHER_PROJECT_ID=your_project_id
   QWEATHER_PRIVATE_KEY=your_private_key
   ```
   *(Note: The app will run in Demo/Offline mode if no API key is provided)*

3. Build and install:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🤖 Continuous Integration & Releases

Automated builds are configured via **GitHub Actions** (`.github/workflows/build-apk.yml`).

- **Artifacts**: Every push to `main` generates a downloadable Debug APK.
- **Releases**: Creating a Git tag (e.g. `v1.0.0`) automatically compiles a Release APK, writes that tag into `versionName` (and commit count into `versionCode`), extracts the latest Git Changelog, and publishes it to [GitHub Releases](https://github.com/Seamain/TyphoonEyeAndroid/releases).

---

## 📄 License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
