# Line Chart Baseline Fix

## What Changed
The chart now anchors the entry price at the center (Y = 50) and plots percentage change relative to that baseline instead of auto-scaling to min/max.

## Previous Implementation
```javascript
const min = Math.min(...priceHistory)
const max = Math.max(...priceHistory)
const range = max - min
const y = 100 - ((price - min) / range) * 80 - 10
```
- Every round stretched from bottom to top, hiding the actual magnitude.
- A +0.1% move looked identical to +5%.
- Cross-round comparisons were impossible.

## New Algorithm
```javascript
const percentChange = ((price - startPrice) / startPrice) * 100
const yOffset = percentChange * 10   // 1% = 10px
const y = 50 - yOffset
const clampedY = Math.max(10, Math.min(90, y))
```
- Entry price stays on the dashed center line.
- Gains plot above 50; losses plot below 50.
- The visual offset is proportional to the real move, capped at ±4% (Y 10–90).

## Reference Lines
```
Y=10  ── Top boundary (+4%)
Y=45  ── +0.5% guide (green)
Y=50  === Entry baseline (white dashed)
Y=55  ── -0.5% guide (red)
Y=90  ── Bottom boundary (-4%)
```

## Practical Examples
- +0.05% → ~0.5 px above center (barely noticeable).
- +0.50% → 5 px upward (clearly winning).
- +1.00% → 10 px upward (dominant win).
- -0.50% → 5 px downward.
- -2.00% → 20 px downward.

If the move exceeds ±4%, the curve clamps to the boundary while the game logic still uses the real price.

## Testing Checklist
1. Install the latest Android build (`./gradlew installDebug`).
2. Start several rounds and verify:
   - The line always begins at the center.
   - Upward/downward movements stay proportional to the actual price change.
   - Comparing multiple rounds now makes sense because the scale is consistent.

## Implementation Notes
- SVG width = 300, height = 100, so ΔX is evenly distributed across 300 px.
- You can tweak the sensitivity by changing the multiplier (e.g., `percentChange * 5` for a calmer line or `* 20` for exaggerated motion).
