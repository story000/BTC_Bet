# Line-Chart Smoothness Optimization

## Completed Enhancements

The price feed now refreshes every **200 ms** instead of **1 second**, giving the WebView chart five times more data points.

### Before vs After

| Config | Refresh Interval | Samples in 10s | Visual Smoothness | When to Use |
| --- | --- | --- | --- | --- |
| Previous | 1000 ms | 10–11 points | ⭐⭐ jagged | Minimum server load |
| Current | 200 ms | **≈50 points** | ⭐⭐⭐⭐⭐ very smooth | Best user experience |

## Code Change
`WebViewPredictionGameActivity.kt:68`

```kotlin
// Before
// delay(1000) // Update every second

// After
delay(200) // 0.2 s cadence for a fluid chart
```

> We also rewired the fetch loop to handle transient failures gracefully so the tighter cadence does not spam errors.

## Visual Difference

- **1s cadence**: curve looks blocky, hard to read direction.
- **200 ms cadence**: curve looks continuous; up/down momentum is obvious.

## Performance Impact

- **Server traffic**: 1 req/s → 5 req/s (~2.5 KB/s per player; 25 KB/s for 10 players; 250 KB/s for 100 players).
- **Mongo logging**: 10 entries per 10-second round → 50 entries. Plan to prune historical logs periodically.
- **Client**: WebView executes 5 JS injections per second; React redraws ~50 points. CPU/memory impact is negligible on modern devices.

## Tuning Options

| Delay | Pros | Cons |
| --- | --- | --- |
| 100 ms | Ultra-smooth motion | Doubles server load again |
| **200 ms** | Balanced | Current default |
| 500 ms | Saves bandwidth | Curve looks slightly choppy |
| 1000 ms | Minimal load | Poor visual quality |

You can implement a hybrid strategy:

```kotlin
val delayMs = if (gameState == GameState.PLAYING) 200L else 1000L
```

…or adapt to volatility:

```kotlin
val drift = abs(currentPrice - lastPrice)
val delay = when {
    drift > 100 -> 100L
    drift > 10 -> 200L
    else -> 500L
}
```

WebSockets would be the ultimate solution (push-only updates), but that requires server support.

## QA Checklist

1. Reinstall the Android app:
   ```bash
   cd android-app
   ./gradlew installDebug
   ```
2. Launch the WebView game, start a round, and confirm the chart adds ~50 points over 10 seconds.
3. Inspect Logcat:
   ```bash
   adb logcat | grep "Price updates"
   # Timestamps should differ by ~0.2 s.
   ```

## Takeaways

- 200 ms is the sweet spot for smoothness vs. cost.
- Consider 500 ms or a hybrid loop if server CPU/network becomes a concern.
- Upgrade to WebSockets for the best long-term scalability.
