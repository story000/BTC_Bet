# BTC 猜涨跌游戏 – 前端交互需求 & 逻辑改造计划

## 1. 游戏设定
- 用户在 Android App 中选择交易对（默认 BTCUSDT）、输入服务器 URL。
- App 提供一个“10 秒涨/跌预测”模块：
  - 输入/选择下注积分（使用客户端本地积分或服务器返回的当前积分）。
  - 选择预测方向（涨 / 跌）。
  - 点击“开始一局”后立即向服务器发送下注请求，记录初始价格。
  - App 显示 10 秒倒计时，倒计时结束后调用服务器端的结果接口获取结算结果（胜/负/平、奖励积分、最新价格和时间）。

## 2. 前端交互需求
1. **主界面布局**
   - 保留现有价格查询表单（symbol + server URL），新增一个“游戏面板”卡片：
     - 显示当前积分（初始 100 / 来自服务器）。
     - 数字输入框或预设按钮选择下注额。
     - 两个 Material Button：预测“涨”或“跌”。
     - 当前局状态区域：初始价格、倒计时、结果文本、赢/输积分。
2. **流程**
   - 点击任意一个预测按钮 → disable 按钮 → 调服务器 `/api/bet/start`（POST）返回 `{betId, initialPrice, expiresAt, balance}`。
   - 前端启动 10 秒倒计时 UI；倒计时结束自动或用户手动点击“查看结果”，调用 `/api/bet/result?betId=...`。
   - 服务器计算 10 秒后的价格，返回 `{finalPrice, outcome, deltaPoints, newBalance}`。App 更新界面、重置可以再次下注。
3. **错误处理**
   - 下注或结算接口出错时显示 Snackbar + 错误卡片文本，恢复按钮。
   - 若倒计时过程中用户退出/APP 重启，可在 ViewModel 保存当前 betId 和剩余时间，重新进入时继续显示倒计时并允许手动请求结果。

## 3. 服务端/逻辑改造计划
1. **API 设计**
   - `POST /api/bet/start`：Body `{symbol, clientId, direction, stake}`。
     - 服务器即时查询当前价格（调用 Binance）。
     - 在 Mongo `bets` 集合记录 bet 文档：`betId, clientId, symbol, direction, stake, initialPrice, startTime, status=pending`。
     - 返回 `betId`, `initialPrice`, `startTime`, `expiresAt=startTime+10s`, `currentBalance`。
   - `GET /api/bet/result?betId=...&clientId=...`：
     - 若 `status=pending` 且 `Instant.now()` >= `startTime+10s`，再次调用 Binance 获取 `finalPrice`，判定涨跌，计算赢/输积分，更新 Mongo 文档。记录日志 (`RequestLog` 加字段 `betId`, `stake`, `outcome`, `pointsDelta`)。
     - 返回 `finalPrice`, `outcome`, `pointsDelta`, `newBalance`, `completedAt`。
   - 现有 `/api/price` 可保留作为简单查询接口。
2. **数据模型**
   - 新建 `Bet` 文档结构：`{ betId, clientId, symbol, direction, stake, initialPrice, finalPrice, outcome, startTime, completedTime, pointsDelta, status }`。
   - Mongo `clients` 或在 `bets` 中聚合，用于计算每个 clientId 的余额（可通过 `MongoLogRepository` 新增方法计算总积分）。
3. **Dashboard 更新**
   - 新增指标：
     - 总下注次数 & 胜率。
     - 平均积分收益。
     - Top N 客户积分或下注额。
   - 日志表格增加列：`betId`, `stake`, `outcome`, `pointsDelta`。
4. **前端数据处理**
   - ViewModel 新增状态：`currentBalance`, `currentBet`, `countdownSeconds`, `resultMessage`。
   - 新增 `BetRepository` 或在原 PriceRepository 扩展 API 接口；使用协程处理 `startBet()` 和 `fetchResult()`。
   - 倒计时使用 `CountDownTimer` 或 `flow` + `delay`，UI 按状态更新卡片。

5. **写日志 & 积分**
   - 每个请求至少继续记录 6+ 字段（已有 requestId, clientId, symbol, latencies...），并添加游戏相关数据。
   - Mongo 中保存积分变化，用 dashboard 展示“客户端累计积分”或“获胜率”作为 analytics。

以上计划完成后，需要更新 README/Writeup，描述新的游戏流程、使用的第三方 API、日志字段和 dashboard 指标，并在 Android App/UI 截图里展示新的游戏界面。
