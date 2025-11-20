# WebView Debug Guide

## Fixes Already Applied
- Vite build uses `base: './'` so bundled assets load via relative paths.
- WebView enables:
  ```kotlin
  allowFileAccessFromFileURLs = true
  allowUniversalAccessFromFileURLs = true
  ```
  which is required for `file://` ES modules.

## Inspecting Logs
- **Android Studio** → View → Tool Windows → Logcat → filter by `WebViewGameActivity`.
- `chromium` tags show WebView internals; `Console` shows `console.log` output.

Shortcut:
```bash
adb logcat | grep -i "WebViewGameActivity\|chromium\|console"
```

## Smoke Tests
1. App launches and you can tap **Prediction Game**.
2. WebView shows content (not a blank page).
3. Live BTC panel, Rise/Fall buttons, countdown, and result states behave as expected.

## Common Issues
| Symptom | Root Cause | Fix |
| --- | --- | --- |
| Blank page | Assets missing or blocked | Re-run `npm run build`, copy `dist` into `app/src/main/assets/game`, ensure the `allow*FromFileURLs` flags are set |
| Black screen only | JS crashed | Inspect Logcat `console` output for syntax/module errors |
| Stutter | Hardware acceleration disabled | Ensure `<application android:hardwareAccelerated="true">` |

## Chrome Remote Debugging
1. Connect the device.
2. Browse to `chrome://inspect` on desktop Chrome.
3. Click **inspect** under your WebView to open DevTools (Console, Elements, Network, Performance).
4. Verify the JS bridge:
   ```javascript
   typeof AndroidBridge
   AndroidBridge.log('hello')
   window.updateBalance(99999)
   ```

## Device Setup
- USB: enable Developer Options + USB debugging, run `./gradlew installDebug`.
- Wireless (Android 11+): `adb pair <ip:port>` then `adb connect <ip:port>`.

## Asset Size Checks
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk  # ~7–8 MB
du -sh app/src/main/assets/game/                  # ~180–200 KB
```

## When Everything Works
- The WebView matches the browser version: gradients, blur, responsive buttons, live price chart, countdown, and end-of-round overlay.

If problems persist, capture Logcat output, Chrome DevTools console screenshots, the current UI screenshot, plus device/Android version details.
