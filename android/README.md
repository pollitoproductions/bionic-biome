Android module for Bionic Biome

Requirements
- Java 17 installed
- Android SDK (compileSdk 34)
- Gradle (or run `gradle wrapper` to create wrapper)

Quick build steps
1. From the `android/` folder, create the Gradle wrapper (if you don't have it):

   ```bash
   gradle wrapper --gradle-version 8.1
   ```

   This generates the `gradlew` scripts and wrapper jar.

2. Update the AdMob App ID in `app/src/main/AndroidManifest.xml` (replace the placeholder value).

3. Build the release AAB:

   ```bash
   ./gradlew :app:bundleRelease
   ```

   The AAB will be in `app/build/outputs/bundle/release/`.

Notes
- The project uses Android Gradle Plugin 8.1.0 (declared in `build.gradle`) and targets Java 17 compatibility in the module config.
- The dependency `com.google.android.gms:play-services-ads` covers banner, interstitial, rewarded and native ads. Consult Google Mobile Ads docs for usage patterns and sample code.

Icon and HTML wrapper notes
- Place your launcher image file at `app/src/main/res/drawable/app_icon.png`. The adaptive icon XML already references this drawable and will use it as the foreground. For best results, use a square image that fills the canvas; the adaptive icon background color is in `values/colors.xml`.
- The app loads a WebView URL from `app/src/main/res/values/strings.xml` key `start_url`. Replace `https://YOUR_DEPLOYED_URL_HERE/` with your deployed HTML URL.
