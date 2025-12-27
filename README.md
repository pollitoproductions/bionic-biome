# Bionic Biome v2.3

**Bionic Biome** is a fast-paced, procedurally generated survival RPG. Explore infinite biomes, fight scaling enemies, and conquer the dungeon bosses.

## 🎮 How to Play
* **Move:** Virtual Joystick (Touch) or WASD (Web).
* **Attack:** Tap to swing Sword / Hold to aim Bow.
* **Goal:** Defeat enemies to level up. Find the Dungeon entrance in each biome to fight the Boss.
* **Win Condition:** Defeat the Boss of Biome 10.

## 📱 Android Build (Play Store)
This project is designed to be wrapped in an Android WebView for the Google Play Store.

### Prerequisites
* Android Studio Ladybug (or newer)
* Minimum SDK: API 24 (Android 7.0)

### Setup Instructions
1.  Clone this repository.
2.  Open **Android Studio** and create a new **Empty Views Activity**.
3.  Copy `index.html` (and any asset folders) into `app/src/main/assets/`.
4.  Ensure `AndroidManifest.xml` includes `<uses-permission android:name="android.permission.INTERNET" />`.
5.  Sync Project with Gradle Files.
6.  Build > Generate Signed Bundle / APK.

## 🌐 Web Version
You can run the game directly in a browser for testing:
1.  Download `index.html`.
2.  Open it in Chrome, Firefox, or Safari.
*(Note: Mobile controls are optimized for touch screens).*

## 🛠️ Features
* **Procedural Generation:** Unique terrain and dungeons every run.
* **Dynamic Lighting:** Ray-casting style lighting in dungeons.
* **Save System:** Local storage saves progress between sessions.
* **AdMob Integration:** Ready for Banner Ads integration (Android only).

## 📄 Privacy Policy
This app respects user privacy. View the full [Privacy Policy](PRIVACY_POLICY.md).

## 📜 License
Copyright © 2025 Brant Fuller. All Rights Reserved.
