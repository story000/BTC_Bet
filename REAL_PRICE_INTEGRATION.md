# Real Price Integration

The WebView game now consumes live Bitcoin prices end to end.

```
Server (Binance + Mongo)
    └─ /api/price?symbol=BTCUSD&clientId=...
Android app (polls every second)
    └─ WebView bridge → window.updatePrice
React game (renders the chart)
```

## Server
`/api/price` already returns:
```json
{
  "symbol": "BTCUSD",
  "price": "64230.50",
  "fetchedAt": "2024-11-20T12:52:00Z"
}
```
The servlet logs every request to MongoDB for analytics.

## Android
`WebViewPredictionGameActivity` launches a coroutine:
```kotlin
priceUpdateJob = lifecycleScope.launch {
    while (isActive) {
        val quote = repository.fetchPrice(...)
        webView.evaluateJavascript(
            "window.updatePrice(${quote.price});",
            null
        )
        delay(1000)
    }
}
```
- Starts in `onCreate`, resumes in `onResume`, pauses/cancels in `onPause` and `onDestroy`.

## React Game
```javascript
useEffect(() => {
  const isWebView = typeof window.AndroidBridge !== 'undefined'
  setIsAndroid(isWebView)

  window.updatePrice = (newPrice) => {
    currentPriceRef.current = newPrice
    setCurrentPrice(newPrice)
  }
}, [])

useEffect(() => {
  if (isAndroid) return // Android feeds real prices

  priceUpdateRef.current = setInterval(() => {
    const change = (Math.random() - 0.5) * 150
    const newPrice = currentPriceRef.current + change
    setCurrentPrice(newPrice)
  }, 100)

  return () => clearInterval(priceUpdateRef.current)
}, [isAndroid])
```
- Android mode: real server prices (1 Hz).
- Browser mode: simulated prices (10 Hz) for local development.

## Game Flow
1. Android polls `/api/price` every second.
2. Each response is logged in MongoDB.
3. Prices are forwarded into WebView via `window.updatePrice`.
4. When a round starts, the entry price is taken from the latest real value.
5. Countdown uses the streaming prices; win/loss is evaluated against the same feed.
6. React notifies Android through `AndroidBridge.onGameFinished(...)` to show a Snackbar.

## Debugging
```bash
adb logcat | grep WebViewGameActivity  # shows price updates
```
Chrome DevTools (`chrome://inspect`) indicates whether the game is running in Android mode or browser simulation.

## Notes
- Extend the bridge to send final prices back to the server if you want on-chain logging.
- Consider batching server requests or moving to WebSockets if you need higher frequency updates.
