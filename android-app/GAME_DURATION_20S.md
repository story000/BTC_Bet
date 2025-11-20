# Game Duration Updated to 20 Seconds

## Summary
- `GAME_DURATION` increased from 10 to 20 seconds.
- `MAX_HISTORY` increased from 40 to 100 points to maintain density.
- WebView fetch cadence remains 200 ms (≈50 samples in 10 s → ≈100 samples in 20 s).

```javascript
// bitcoin-game/src/BitcoinGame.jsx
const GAME_DURATION = 20;
const MAX_HISTORY = 100;
```

## Experience Comparison

| Version | Round Length | Samples | Notes |
| --- | --- | --- | --- |
| 10 s | 50 points | Fast rounds, light server load, shallow price swings |
| **20 s** | **100 points** | Longer observation window, clearer trends, more suspense |

Typical BTC volatility: ~0.01–0.3% in 10 s vs. 0.05–0.8% in 20 s, so the new duration creates more meaningful highs/lows.

## Performance Impact

- Requests per player: 100 per round (vs. 50).
- Data per player: ~10 KB per round.
- Mongo writes: 100 log rows per round.
- With 10 concurrent players: ≈50 req/s (~25 KB/s). With 100 players: ≈500 req/s (~250 KB/s).

### Mitigation
- Reduce polling to 500 ms if bandwidth becomes an issue.
- Buffer log writes in batches.
- Share cached prices among players when a <1 s delay is acceptable.

## Build Checklist

```bash
# Rebuild the React game
cd bitcoin-game
npm run build
cp -r dist/* ../android-app/app/src/main/assets/game/

# Rebuild and install Android
cd ../android-app
./gradlew assembleDebug
./gradlew installDebug
```

## Test Plan
- Launch the WebView game.
- Start a round and verify the countdown begins at 20 seconds.
- Confirm the chart shows ~100 points with smooth motion.
- Ensure the round finishes with the correct VICTORY/DEFEAT state and balance update.
- Tap **Play Again** to verify the state resets.
