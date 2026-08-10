# 🌀 TyphoonEye (台风眼)

[![Build Android APK](https://github.com/Seamain/TyphoonEyeAndroid/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Seamain/TyphoonEyeAndroid/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/API-29%2B-3DDC84.svg?style=flat&logo=android)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

[English](README.md) | 中文

**台风眼 (TyphoonEye)** 是一款极具现代感、基于 **Jetpack Compose** 与 **Material Design 3** 构建的 Android 台风实时追踪与预警防护应用。支持多象限风圈可视化渲染、离线数据缓存及基于距离的预警提醒。

---

## ✨ 核心特性

- 🌀 **交互式地图与风圈渲染**：基于 MapLibre 矢量地图引擎，实时渲染台风历史轨迹、预报路径以及 7级/10级/12级 东北、东南、西南、西北四象限风圈多边形。
- 📍 **距离感知与紧急预警**：计算用户当前定位与台风中心的实时距离，并在接近风险区域时触发紧急预警与通知。
- 📊 **详尽的台风观测详情**：提供中心气压、最大持续风速、移动速度、移动方向及完整的观测时间线序列数据。
- 🔔 **实时状态与后台定时更新**：基于 `WorkManager` 实现常驻后台更新调度，提供实时状态通知栏常驻卡片。
- 🎨 **Material Design 3 与动态色彩**：全面采用 Material 3 设计规范，完美适配浅色/深色主题，并支持 Android 12+ Monet 动态调色盘。
- 🌐 **多语言国际化**：原生支持简体中文、繁體中文、粵語及英文。
- 📜 **开源致谢与隐私保障**：内置完整的开源许可致谢页面与离线防走丢演示模式。

---

## 🛠 技术栈与架构设计

应用严格遵循 **Clean Architecture** 架构理念与 **MVVM** 模式，结合官方 Jetpack 组件进行标准化开发。

- **界面与导航**：[Jetpack Compose](https://developer.android.com/jetpack/compose)、[Material 3](https://m3.material.io/)、Navigation Compose
- **地图引擎**：[MapLibre Android SDK](https://github.com/maplibre/maplibre-native)；默认 [高德地图](https://lbs.amap.com/api) 栅格底图（GCJ-02），可在设置中切换国际 Carto/OSM（WGS-84）
- **依赖注入**：[Hilt](https://dagger.dev/hilt/) (Dagger)
- **并发与响应式**：Kotlin 协程 (Coroutines)、StateFlow、SharedFlow
- **网络层**：[Retrofit 2](https://square.github.io/retrofit/)、[OkHttp 4](https://square.github.io/okhttp/)、Ed25519 密码学 JWT 身份认证
- **持久化与存储**：[Room Database](https://developer.android.com/training/data-storage/room)、[DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **后台任务**：[WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **CI/CD 流水线**：GitHub Actions 自动化编译打包与 Release 版本发布。

---

## 🚀 快速开始

### 开发环境要求

- Android Studio Ladybug (2024.2.1+) 或更新版本
- JDK 21
- Android SDK 36 (最低支持 minSdk 29)

### 本地编译

1. 克隆代码仓库：
   ```bash
   git clone https://github.com/Seamain/TyphoonEyeAndroid.git
   cd TyphoonEyeAndroid
   ```

2. (可选) 配置 API Key：
   复制 `local.properties.example` 为 `local.properties` 并填写密钥：
   ```properties
   QWEATHER_API_KEY=your_qweather_api_key
   QWEATHER_KID=your_key_id
   QWEATHER_PROJECT_ID=your_project_id
   QWEATHER_PRIVATE_KEY=your_private_key
   # 高德开放平台 Key（中国大陆底图，https://console.amap.com/dev/key/app ）
   AMAP_KEY=your_amap_web_key
   # 底图强制：auto | amap | open
   # MAP_BASEMAP=auto
   # 应用内更新默认读取 GitHub Releases（Seamain/TyphoonEyeAndroid）
   ```

### 应用内更新

应用启动时会静默检查 GitHub Releases（每天最多一次）；在 **设置 → 关于 → 检查更新** 可手动检查。  
发现新版本后可下载 APK 并调起系统安装器（需授予「安装未知应用」权限）。  

**版本号写入规则：** `versionName` 取自最近的 GitHub Tag（如 `v1.1.0` → `1.1.0`），`versionCode` 为 commit 计数；CI 推送 `v*` 标签构建时会自动注入。发版请推送 `vX.Y.Z` 并附带 `.apk`（GitHub Actions 已支持）。

   *(注：未配置密钥时，应用将自动运行在 Demo / 演示模式)*

3. 编译并安装：
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🤖 自动化构建与发布

本工程通过 **GitHub Actions** (`.github/workflows/build-apk.yml`) 实现了全自动 CI/CD。

- **构建产物 (Artifacts)**：每次代码提交都会自动编译产出 Debug 测试包供下载。
- **自动发布 (Releases)**：推送带有版本号的 Git 标签（例如 `v1.0.0`）时，Actions 会自动提取最新更新日志并打包发布 Release APK 至 [GitHub Release 页面](https://github.com/Seamain/TyphoonEyeAndroid/releases)。

---

## 📄 开源许可证

**台风眼（TyphoonEye）** 以 **[Apache License 2.0](LICENSE)** 开源发布  
（[SPDX](https://spdx.org/licenses/Apache-2.0.html)：`Apache-2.0`）。

| 文件 | 说明 |
|------|------|
| [LICENSE](LICENSE) | Apache License 2.0 全文 |
| [NOTICE](NOTICE) | 版权与第三方组件致谢 |

应用内路径：**设置 → 关于 → 开源许可与致谢**。

地图瓦片与气象数据服务条款以各提供方为准，不在本 Apache-2.0 授权范围内。
