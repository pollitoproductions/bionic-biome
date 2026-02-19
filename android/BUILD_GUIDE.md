# Android Build Setup Guide

## Quick Start

The Android wrapper loads your game from GitHub Pages using a WebView.

### Prerequisites

1. **Android SDK** - Install via Android Studio or command line
2. **Java 11+** - Required for Gradle builds
3. **Gradle** - Included with the wrapper script

### Building Locally

```bash
cd android

# For macOS/Linux
chmod +x gradlew
./gradlew assembleRelease

# For Windows
gradlew.bat assembleRelease
```

Output APK: `android/app/build/outputs/apk/release/app-release.apk`

### Building in GitHub Codespaces

```bash
# Install Java
apt-get update && apt-get install -y openjdk-11-jdk

# Build
cd android
./gradlew assembleRelease
```

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/pollitoproductions/bionicbiome/
│   │   │   └── MainActivity.java       # WebView setup
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml
│   │   │   ├── values/strings.xml
│   │   │   └── drawable/ic_launcher.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Key Configuration

- **Game URL**: `https://pollitoproductions.github.io/bionic-biome/`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Screen**: Portrait, Full-screen, No action bar

## Customization

### Change Game URL
Edit [MainActivity.java](android/app/src/main/java/com/pollitoproductions/bionicbiome/MainActivity.java#L10):
```java
private static final String GAME_URL = "https://your-domain.com/game";
```

### Change App Name
Edit [strings.xml](android/app/src/main/res/values/strings.xml):
```xml
<string name="app_name">Your Game Name</string>
```

### Change Version
Edit [build.gradle](android/app/build.gradle):
```gradle
versionCode 1        // Increment for each release
versionName "1.0"
```

### Custom Icon
Replace the icon in [ic_launcher.xml](android/app/src/main/res/drawable/ic_launcher.xml)

## Debugging

Enable WebView debugging in Chrome:
1. Connect Android device via USB
2. Open `chrome://inspect` in Chrome
3. Find your app and click "Inspect"

## Signing for Google Play

### Generate Keystore
```bash
keytool -genkey -v -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### Sign APK
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.jks \
  app-release-unsigned.apk my-key-alias
```

## Next Steps

1. ✅ Build locally and test on emulator
2. ✅ Update app name, version, and icon
3. ✅ Ensure your game loads correctly via WebView
4. ✅ Generate keystore and sign APK
5. ✅ Build App Bundle (.aab) for Google Play
   ```bash
   ./gradlew bundleRelease
   ```
6. ✅ Upload to Google Play Console

## Troubleshooting

**APK won't run**: Ensure `GAME_URL` is accessible and uses HTTPS

**Network errors**: Check `android:usesCleartextTraffic` in AndroidManifest.xml

**Touch controls not working**: Verify `WebSettings` has JavaScript enabled

**Build fails**: Update Android SDK to latest version via Android Studio
