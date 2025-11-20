# Bitcoin Prediction Game – Front-End Interaction and Logic Plan

## 1. Game Concept
- The Android app lets users select a trading symbol (default **BTCUSD**) and enter the server URL.
- The app offers a **10-second up/down prediction** module:
  - Players choose a stake in points (client-side balance or a balance returned by the server).
  - Select the direction (Rise/Fall).
  - Press **Start Round** to send a bet request to the server, which records the entry price.
  - The UI shows a 10-second countdown, then hits the result endpoint to retrieve win/loss outcome, payout, and final price.

## 2. Front-End Requirements
1. **Layout**
   - Keep the price query form (symbol + server URL) and add a dedicated “Game Panel” card:
     - Display the current balance (default 100 points or the server’s value).
     - Provide an amount input or preset buttons.
     - Two Material buttons to select Rise/Fall.
     - A status area showing entry price, countdown timer, and result.
2. **Flow**
   - Tapping either prediction button disables both buttons and sends `POST /api/bet/start` (returns `{betId, entryPrice, expiresAt, balance}`).
   - The front end starts a 10-second countdown. When it finishes (or when the user taps **Check Result**), call `GET /api/bet/result?betId=...`.
   - The server compares the entry and settlement prices, computes the payout, and returns `{finalPrice, outcome, deltaPoints, newBalance}`. The UI refreshes and re-enables betting.
3. **Error Handling**
   - Show a Snackbar plus inline error text if the bet or result call fails, then re-enable the buttons.
   - Persist `betId` and the remaining time inside the ViewModel so a relaunch can restore the countdown and allow manual “Check Result.”

## 3. Server-Side Changes
1. **APIs**
   - `POST /api/bet/start` with `{symbol, clientId, direction, stake}`. The server fetches the current Binance price, writes a `bet` record with status `pending`, and returns metadata plus the current balance.
   - `GET /api/bet/result?betId=...&clientId=...` fetches the settlement price once the 10-second window ends, computes the outcome, updates Mongo, and returns `{finalPrice, outcome, pointsDelta, newBalance}`.
   - The existing `/api/price` endpoint remains for single quotes.
2. **Data Model**
   - `Bet` document shape: `{ betId, clientId, symbol, direction, stake, initialPrice, finalPrice, outcome, startTime, completedTime, pointsDelta, status }`.
   - Use aggregation to maintain per-client balances or add a `clients` collection.
3. **Dashboard**
   - New metrics: total bets, win rate, average payout, top clients by balance, etc.
   - Extend the log table with `betId`, `stake`, `outcome`, `pointsDelta`.
4. **Android ViewModel**
   - Track `currentBalance`, `currentBet`, `countdownSeconds`, and `resultMessage`.
   - Add repository methods `startBet()` and `fetchResult()` using coroutines.
   - Use `CountDownTimer` or `Flow` to drive the countdown.
5. **Logging & Analytics**
   - Each request still records core latency fields plus new columns for bet metadata.
   - The dashboard can show lifetime balance and win rate per `clientId`.

Update the README/Writeup after implementation to document the new round flow, external APIs used, log schema, and dashboard metrics. Include Android UI screenshots of the finished game panel.
