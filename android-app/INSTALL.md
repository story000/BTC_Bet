# Android App Installation Guide

## Fixed Issues

The WebView assets now load correctly because:
- The Vite build uses a relative base path (`base: './'`).
- The React game was rebuilt with the new configuration.
- The generated assets were copied into the Android project.
- The Android app was rebuilt with the latest assets.

## Installation Options

### Option 1: Install with Gradle (Recommended)

```bash
cd android-app
./gradlew installDebug
```

### Option 2: Install the APK Manually

1. Locate the APK at:
   `android-app/app/build/outputs/apk/debug/app-debug.apk`
2. Transfer the APK to your Android device via USB, cloud storage, or Android Studio's Device File Explorer.
3. Open the APK on the device and confirm the installation.

### Option 3: Install from Android Studio

1. Open Android Studio and load the `android-app/` module.
2. Connect a physical device or start an emulator.
3. Press the Run ▶️ button to deploy the debug build.

## Smoke Test

1. Launch the app.
2. Tap **Prediction Game**.
3. You should see the full WebView-based game with gradients and animations.

## Troubleshooting

### Inspect Logcat

```bash
# Android Studio: View -> Tool Windows -> Logcat and filter by WebViewGameActivity
# CLI (with adb installed):
adb logcat | grep -i "WebViewGameActivity\|chromium\|console"
```

### Debug the WebView with Chrome

1. Connect the device to your computer.
2. Open Chrome and navigate to `chrome://inspect`.
3. Locate the WebView under **Remote Target**.
4. Click **inspect** to open DevTools and view console output.

## Updating the Game Assets

```bash
# 1. Modify the React code
cd bitcoin-game
# edit src/BitcoinGame.jsx

# 2. Rebuild the web bundle
npm run build

# 3. Copy the build output into the Android project
cp -r dist/* ../android-app/app/src/main/assets/game/

# 4. Rebuild the Android app
cd ../android-app
./gradlew assembleDebug

# 5. Install the updated build
./gradlew installDebug
```
