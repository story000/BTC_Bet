# 游戏时长调整至 20 秒

## ✅ 已完成更新

将预测游戏的时长从 **10 秒** 调整为 **20 秒**

## 🔧 修改内容

### 1. React 游戏配置

**文件**: `bitcoin-game/src/BitcoinGame.jsx`

```javascript
// 修改前
const GAME_DURATION = 10;  // 10 秒
const MAX_HISTORY = 40;    // 40 个数据点

// 修改后
const GAME_DURATION = 20;  // 20 秒
const MAX_HISTORY = 100;   // 100 个数据点
```

### 2. 数据点密度

**更新频率**: 200ms (0.2秒) 每次

| 配置 | 游戏时长 | 更新频率 | 数据点数量 | 图表流畅度 |
|------|---------|---------|-----------|-----------|
| **旧版本** | 10 秒 | 200ms | 50 个点 | ⭐⭐⭐⭐⭐ 流畅 |
| **新版本** | 20 秒 | 200ms | **100 个点** | ⭐⭐⭐⭐⭐ 非常流畅 |

## 📊 对比分析

### 游戏体验变化

#### 10 秒版本
- ✅ 快节奏，适合快速游戏
- ✅ 服务器压力较小（每局 50 次请求）
- ❌ 时间较短，波动可能不明显
- ❌ 预测难度较低

#### 20 秒版本
- ✅ 更长的观察期，价格趋势更明显
- ✅ 预测更有挑战性
- ✅ 图表更长，视觉效果更好
- ⚠️ 服务器压力翻倍（每局 100 次请求）

### 典型价格波动

**Bitcoin 价格波动（基于历史数据）**:

| 时间段 | 典型波动范围 | 适合预测难度 |
|--------|-------------|-------------|
| 10 秒 | 0.01% - 0.3% | 较容易 |
| 20 秒 | 0.05% - 0.8% | 适中 |
| 60 秒 | 0.2% - 2% | 较难 |

## 🎮 游戏流程（20秒版本）

### 完整流程时间线

```
0s   - 用户选择 RISE 或 FALL
0s   - 点击 START GAME
0s   - 记录起始价格（基准线）
     - 开始倒计时
1s   - 20s 剩余
2s   - 19s 剩余
...
10s  - 11s 剩余（游戏进行一半）
...
19s  - 2s 剩余
20s  - 1s 剩余
20s  - 游戏结束，显示结果
```

### 数据采集

- **总采样次数**: 约 100 次（20 秒 ÷ 0.2 秒）
- **图表数据点**: 100 个点（完整覆盖）
- **折线长度**: 300 像素宽度
- **点间距**: 约 3 像素/点（300 ÷ 100）

## 📈 性能影响

### 服务器负载

**单用户单局游戏**:
- **总请求数**: 100 次（从 50 次翻倍）
- **总数据量**: ~10 KB（从 ~5 KB 增加）
- **MongoDB 写入**: 100 条日志（从 50 条增加）

**10 个并发用户**:
- **峰值请求**: 50 req/s（10 用户 × 5 次/秒）
- **带宽**: ~25 KB/s
- **MongoDB 写入**: ~50 docs/s

**100 个并发用户**:
- **峰值请求**: 500 req/s
- **带宽**: ~250 KB/s
- **MongoDB 写入**: ~500 docs/s

### 优化建议

如果服务器压力过大，可以考虑：

1. **降低更新频率**（牺牲流畅度）
   ```kotlin
   delay(500) // 改为 0.5 秒更新
   // 结果: 20 秒内 40 个数据点（仍然较流畅）
   ```

2. **实现批量写入**（减少数据库压力）
   ```kotlin
   // 每 5 秒批量写入一次，而不是每次请求都写
   ```

3. **缓存机制**（减少 Binance API 调用）
   ```kotlin
   // 多个用户共享同一价格数据（延迟 <1 秒可接受）
   ```

## 🛠️ 技术细节

### 构建流程

```bash
# 1. 修改 React 代码
cd bitcoin-game
# 编辑 src/BitcoinGame.jsx（已完成）

# 2. 构建生产版本
npm run build
# 输出: dist/assets/index-B0xJJ-_7.js (157.34 kB)

# 3. 复制到 Android 资源
rm -rf android-app/app/src/main/assets/game
mkdir -p android-app/app/src/main/assets/game
cp -r bitcoin-game/dist/* android-app/app/src/main/assets/game/

# 4. 构建 Android APK
cd android-app
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk

# 5. 安装到设备（可选）
./gradlew installDebug
# 或手动: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 文件变更

```
bitcoin-game/
├── src/
│   └── BitcoinGame.jsx           (修改)
│       - GAME_DURATION: 10 → 20
│       - MAX_HISTORY: 40 → 100
└── dist/                         (重新构建)
    ├── index.html
    └── assets/
        └── index-B0xJJ-_7.js     (157.34 kB)

android-app/
└── app/src/main/assets/game/     (更新)
    ├── index.html
    └── assets/
        └── index-B0xJJ-_7.js
```

## 🧪 测试清单

安装新版本后，测试以下内容：

- [ ] 游戏启动正常（显示初始界面）
- [ ] 价格实时更新（每 0.2 秒）
- [ ] 选择 RISE 或 FALL
- [ ] 点击 START GAME
- [ ] 倒计时从 20 秒开始
- [ ] 折线图持续更新（20 秒内约 100 个点）
- [ ] 倒计时归零时显示结果
- [ ] VICTORY 或 DEFEAT 正确显示
- [ ] 积分正确增减
- [ ] 点击 PLAY AGAIN 可以重新开始
- [ ] 第二局游戏折线图正确重置

## 📱 部署

### 安装 APK

```bash
cd android-app
./gradlew installDebug
```

或手动安装：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 验证运行

1. 启动 Android App
2. 确保服务器正在运行（`http://10.0.2.2:8080/`）
3. 点击「预测游戏」按钮
4. 观察倒计时是否为 20 秒
5. 观察折线图是否流畅（100 个数据点）

## 🎯 用户体验

### 优点

✅ **更长的观察期**: 20 秒足以看清价格趋势
✅ **更平滑的折线图**: 100 个数据点，视觉效果极佳
✅ **更有挑战性**: 预测难度适中，增加游戏趣味性
✅ **更真实的市场感觉**: 接近真实交易的决策时间

### 可能的用户反馈

| 反馈 | 应对策略 |
|------|---------|
| "20 秒太长了" | 可以添加时长选择（10s/20s/30s） |
| "我的流量用完了" | 添加 WiFi 提示，或降低更新频率 |
| "图表太流畅看不清趋势" | 添加移动平均线辅助 |
| "想要更长时间" | 可以扩展到 30s 或 60s |

## 🚀 后续增强

可以考虑的功能：

- [ ] **时长选择**: 让用户选择 10s/20s/30s/60s
- [ ] **难度模式**:
  - 简单：30 秒
  - 中等：20 秒
  - 困难：10 秒
- [ ] **倍率调整**: 根据时长调整积分倍率
  - 10 秒：2x 积分
  - 20 秒：3x 积分
  - 30 秒：5x 积分
- [ ] **暂停功能**: 允许暂停游戏（扣除积分）
- [ ] **回放功能**: 游戏结束后重放价格走势

## 📊 数据统计建议

可以记录以下数据：

```javascript
{
  "gameId": "uuid",
  "userId": "clientId",
  "symbol": "BTCUSD",
  "duration": 20,
  "startPrice": 64230.50,
  "endPrice": 64280.30,
  "priceChange": 0.078,
  "prediction": "RISE",
  "result": "VICTORY",
  "pointsChange": +100,
  "timestamp": "2024-11-20T14:04:00Z",
  "priceHistory": [64230.50, 64231.20, ...] // 100 个点
}
```

这样可以分析：
- 平均价格波动幅度
- 用户胜率
- 最佳游戏时段
- 优化预测算法

## ✅ 完成状态

- [x] 修改 React 游戏时长配置
- [x] 增加历史数据数组容量
- [x] 构建生产版本
- [x] 复制到 Android 资源目录
- [x] 构建 Android APK
- [ ] 安装到测试设备（待执行）
- [ ] 用户测试（待反馈）

**现在游戏时长已经是 20 秒了！** 🎉

### 快速安装

```bash
cd "/Users/liusiyuan/Desktop/CMU Study/DistributedSystem/project4/android-app"
./gradlew installDebug
```
