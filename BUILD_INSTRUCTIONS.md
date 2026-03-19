Build instructions (local & CI)

Local build (Linux) — prerequisites: JDK 17, Android SDK command-line tools, unzip, curl

1. Install JDK 17 and set `JAVA_HOME`.

2. Install Android SDK command-line tools and required packages (example):

```bash
export ANDROID_SDK_ROOT="$PWD/android/android-sdk"
mkdir -p "$ANDROID_SDK_ROOT"
curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -o /tmp/cmdline-tools.zip
unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
mv /tmp/cmdline-tools/cmdline-tools "$ANDROID_SDK_ROOT/cmdline-tools/latest"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

3. Use Gradle 8.1.1 (either `./gradlew` or download Gradle):

```bash
curl -fsSL https://services.gradle.org/distributions/gradle-8.1.1-bin.zip -o /tmp/gradle-8.1.1.zip
unzip -q /tmp/gradle-8.1.1.zip -d /tmp/gradle
/tmp/gradle/gradle-8.1.1/bin/gradle -p android :app:bundleRelease --no-daemon
```

4. Output AAB will be in `android/app/build/outputs/bundle/release/`.

CI (GitHub Actions)

- The workflow `.github/workflows/build-aab.yml` will run on pushes to `main` and can be triggered manually from the Actions tab.
- It builds with Java 17, installs Android SDK platform 35 and build-tools 35.0.0, runs Gradle 8.1.1 and uploads the resulting AAB as an artifact.
