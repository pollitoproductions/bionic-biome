🎮 Bionic Biome — Web Game to Android App

This repository documents the exact, working process for turning an HTML/JavaScript web game into an Android App Bundle (.aab) that is accepted by Google Play.

The game itself is hosted on GitHub Pages and wrapped for Android using a lightweight WebView.

No local Android Studio install required.

📌 Overview

Game type: HTML5 / JavaScript (pixel art game)

Hosting: GitHub Pages

Android build method: WebView wrapper

Build environment: GitHub Codespaces

Output: Signed .aab (Google Play compliant)

This approach avoids emulator setup, local SDK issues, and VM complexity.

🧠 How It Works (Conceptually)
GitHub Pages  → hosts the game
Codespaces   → builds the Android app
Google Play  → distributes the app


Each tool does one job.

✅ Requirements

Before starting:

Your game is live on GitHub Pages (HTTPS)

You are logged into GitHub

You can copy and paste commands (no Android experience required)

🚀 Step-by-Step Build Guide

Follow the instructions below exactly.
You do not need to understand Android or Java to complete this.

PHASE 1 — Open Your Cloud Computer

Go to this GitHub repository.

Click the green Code button.

Select the Codespaces tab.

Click Create codespace on main.

Wait for VS Code to load in your browser.

Open the terminal:

Press `Ctrl + ``

(The backtick key ` is usually under the ESC key.)

PHASE 2 — Install Android Tools

Paste each block into the terminal and press Enter.

1. Install Java
sudo apt update
sudo apt install openjdk-17-jdk -y

2. Download Android Command Line Tools
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip
mv cmdline-tools latest

3. Set Environment Variables
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
source ~/.bashrc

4. Install SDK Components
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.1"

PHASE 3 — Get the Android Template
cd ~
git clone https://github.com/slymax/webview-android.git bionic-android
cd bionic-android


Then open the folder:

File → Open Folder

Select bionic-android

Click OK

Re-open the terminal if needed

PHASE 4 — Customize the App (Critical)
1. Fix Package Structure
mkdir -p app/src/main/java/com/bionicbiome/game
mv app/src/main/java/com/example/webview/MainActivity.java app/src/main/java/com/bionicbiome/game/
rm -rf app/src/main/java/com/example

2. Edit MainActivity.java

File:

app/src/main/java/com/bionicbiome/game/MainActivity.java


Line 1:

package com.bionicbiome.game;


Update the URL:

mWebView.loadUrl("https://YOURNAME.github.io/YOURGAME/");


⚠️ The URL must start with https://

3. Update App ID

File:

app/build.gradle


Change:

applicationId "com.example.webview"


To:

applicationId "com.bionicbiome.game"

4. Update App Name

File:

app/src/main/res/values/strings.xml


Change:

<string name="app_name">WebView</string>


To:

<string name="app_name">Bionic Biome</string>

5. Manifest Check

File:

app/src/main/AndroidManifest.xml


If you see:

package="com.example.webview"


Change it to:

package="com.bionicbiome.game"

PHASE 5 — Sign & Build
1. Create a Keystore
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-game-alias


Choose a password

Write it down

Type yes when prompted

2. Build the App Bundle

Replace YOUR_PASSWORD with your password:

./gradlew bundleRelease \
  -Pandroid.injected.signing.store.file=$HOME/bionic-android/release.jks \
  -Pandroid.injected.signing.store.password=YOUR_PASSWORD \
  -Pandroid.injected.signing.key.alias=my-game-alias \
  -Pandroid.injected.signing.key.password=YOUR_PASSWORD


Wait for:

BUILD SUCCESSFUL

PHASE 6 — Download Files
App Bundle
app/build/outputs/bundle/release/app-release.aab


Right-click → Download

Keystore (CRITICAL)
release.jks


Right-click → Download

⚠️ If you lose this file, you can never update the app.

Back it up securely.

✅ Done

You now have:

A Google Play–ready .aab

A signing key

No local Android installation required

You may safely delete the Codespace after finishing.
