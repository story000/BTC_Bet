# WebView Game Integration Notes

## Overview
The React-based Bitcoin prediction game now runs inside the Android app via WebView while preserving the full visual design.

## Result
- CSS animations, gradients, and blur effects remain intact.
- SVG icons, charts, and interactive buttons behave exactly like the browser version.
- Countdown and victory/defeat overlays work without changes.

## Structure
```
android-app/
├── app/src/main/assets/game/      # React build (index.html + assets)
└── app/src/main/java/.../game/
    ├── PredictionGameActivity.kt        # legacy native version (deprecated)
    └── WebViewPredictionGameActivity.kt # WebView implementation
```

## JavaScript Bridge
- Android → JS: `window.updateBalance(amount)` and `window.updatePrice(price)`.
- JS → Android: `window.AndroidBridge.onGameFinished(win, stake, newBalance, finalPrice, startPrice)`.

## Usage
1. Build/install the Android app:
   ```bash
   cd android-app
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```
2. Tap **Prediction Game** to launch the WebView screen.

## Updating the Game
```bash
cd bitcoin-game
# edit src/BitcoinGame.jsx
npm run build
cp -r dist/* ../android-app/app/src/main/assets/game/
```
Rebuild the Android APK afterwards.

## Extending the Bridge
- Add new JS handlers inside `useEffect` in `BitcoinGame.jsx`.
- Add matching `@JavascriptInterface` functions inside `AndroidBridge` in `WebViewPredictionGameActivity`.

## Fetching Real Prices
Within the bridge you can call Kotlin coroutines/Retrofit, then push values back via `evaluateJavascript()`.

## Performance Notes
- Hardware acceleration is enabled.
- Assets are bundled locally, so no runtime network fetch is needed.
- WebView runs in the main process; profile with `chrome://inspect` if you see jank.

## Troubleshooting
- Verify assets exist under `app/src/main/assets/game/` if the screen is blank.
- Use Chrome remote debugging (`chrome://inspect`) to inspect console errors.
- Use `adb logcat | grep WebViewGameActivity` to see bridge logs.

## Next Steps
- Hook the WebView to the live price API.
- Persist user balances.
- Add history/offline stats or alternate game modes.
- Optimize asset size/loading.
