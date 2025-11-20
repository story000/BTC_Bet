# Android Client – Crypto Monitor Mobile

This Android app calls the `/api/price` endpoint from Task 2 and uploads device information as `clientId` so the server can associate requests with specific devices.

## Feature Overview
- Enter the trading symbol and server URL, then press **Fetch Price** to send a request.
- The screen shows the latest price, server timestamp, last request status, and any error message.
- `clientId` is generated automatically from the device make, model, Android version, and `ANDROID_ID`, so no manual entry is required.

## Getting Started
1. Open Android Studio and choose **File → Open...**, pointing to the `android-app` directory in this repo.
2. If Gradle wrapper files are missing, run `gradlew wrapper --gradle-version 8.6` inside the Android Studio terminal, then click **Sync Now**.
3. Connect a physical device or launch an emulator (API 24+).
4. Run the `app` configuration to install the debug build.

## Server URL
- `BuildConfig.DEFAULT_BASE_URL` defaults to `http://10.0.2.2:8080/`, which lets the emulator reach a locally running Tomcat instance.
- If your server runs at a public URL (for example, Codespaces), replace the value in the input field with the reachable address (include a trailing slash).

## Dependencies
- Kotlin, ViewModel, Coroutines
- Retrofit, Moshi, OkHttp Logging
- Material 3 components and ViewBinding

## Logging Context
Every API request automatically appends `clientId=brand-model-androidVersion-ANDROID_ID`, which makes it easy to group metrics and logs per device on the server dashboard.
