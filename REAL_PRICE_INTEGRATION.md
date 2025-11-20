# 真实价格集成说明

## ✅ 完成的集成

游戏现在已经完全集成了真实的 Bitcoin 价格数据！

### 架构流程

```
服务器 (MongoDB + Binance API)
    ↓
    └─→ GET /api/price?symbol=BTCUSD&clientId=xxx
        ↓
Android App (每秒轮询)
    ↓
    └─→ WebView Bridge (window.updatePrice)
        ↓
React 游戏 (接收并显示实时价格)
```

## 🔧 实现细节

### 1. 服务器端 (已有)

**接口**: `/api/price`
- **参数**:
  - `symbol` - 交易对 (默认: BTCUSD)
  - `clientId` - 客户端标识
- **返回**:
  ```json
  {
    "symbol": "BTCUSD",
    "price": "64230.50",
    "fetchedAt": "2024-11-20T12:52:00Z"
  }
  ```
- **数据来源**: Binance API
- **存储**: MongoDB (RequestLog 集合)

### 2. Android 端

#### WebViewPredictionGameActivity.kt

**新增功能**:
```kotlin
private val repository = PriceRepository()
private var priceUpdateJob: Job? = null

private fun startPriceUpdates() {
    priceUpdateJob = lifecycleScope.launch {
        while (isActive) {
            // 每秒从服务器获取价格
            val response = repository.fetchPrice(
                baseUrl = BuildConfig.DEFAULT_BASE_URL,
                symbol = "BTCUSD",
                clientId = clientId
            )
            val price = response.price.toDouble()

            // 推送到 WebView
            webView.evaluateJavascript(
                "window.updatePrice($price);",
                null
            )

            delay(1000) // 1秒更新一次
        }
    }
}
```

**生命周期管理**:
- `onCreate` - 启动价格更新
- `onResume` - 恢复价格更新
- `onPause` - 暂停价格更新
- `onDestroy` - 停止价格更新

### 3. React 端

#### BitcoinGame.jsx

**智能模式切换**:
```javascript
useEffect(() => {
  // 检测是否在 Android WebView 中运行
  const isAndroidWebView = typeof window.AndroidBridge !== 'undefined';
  setIsAndroid(isAndroidWebView);

  // 暴露价格更新函数给 Android
  window.updatePrice = (newPrice) => {
    console.log('Price updated from Android:', newPrice);
    currentPriceRef.current = newPrice;
    setCurrentPrice(newPrice);
  };
}, []);

// 价格模拟引擎 - 仅在浏览器模式运行
useEffect(() => {
  if (isAndroid) {
    console.log('Using real prices from server');
    return; // 跳过模拟
  }

  // 浏览器模式：使用模拟价格
  priceUpdateRef.current = setInterval(() => {
    const change = (Math.random() - 0.5) * 150;
    const newPrice = currentPriceRef.current + change;
    setCurrentPrice(newPrice);
  }, 100);

  return () => clearInterval(priceUpdateRef.current);
}, [isAndroid]);
```

**双模式运行**:
- **Android 模式**: 使用服务器真实价格 (每秒更新)
- **浏览器模式**: 使用本地模拟价格 (每 100ms 更新)

## 📊 数据流

### 正常游戏流程

1. **游戏启动**
   - Android 检测到 AndroidBridge 存在
   - 禁用本地价格模拟
   - 显示初始价格 (64230.50)

2. **价格更新**
   - Android 每秒调用 `/api/price`
   - 服务器从 Binance 获取最新价格
   - 服务器将请求记录到 MongoDB
   - Android 通过 Bridge 推送价格到 React
   - React 更新显示并加入历史记录

3. **游戏进行**
   - 用户选择 RISE 或 FALL
   - 点击 START GAME
   - 记录起始价格 (来自真实数据)
   - 10 秒倒计时期间持续更新价格
   - 结束时比较最终价格与起始价格
   - 结算胜负

4. **结果通知**
   - React 调用 `AndroidBridge.onGameFinished()`
   - Android 显示 Snackbar 通知
   - 可以记录到本地或服务器 (未实现)

## 🎮 用户体验

### 在 Android App 中
- ✅ 显示真实的 Bitcoin 价格
- ✅ 每秒更新一次
- ✅ 价格历史图表使用真实数据
- ✅ 游戏结算基于真实价格波动
- ✅ 完全保留所有视觉效果和动画

### 在浏览器中
- ✅ 使用模拟价格 (更快的更新频率)
- ✅ 适合开发和演示
- ✅ 无需服务器即可运行

## 🔍 调试和验证

### 查看 Android 日志
```bash
# 过滤游戏相关日志
adb logcat | grep WebViewGameActivity

# 应该看到：
# Updated price: 64230.50
# Updated price: 64231.20
# Updated price: 64229.80
# ...
```

### Chrome DevTools
1. `chrome://inspect`
2. 查看 Console 输出：
```javascript
// Android 模式
Price updated from Android: 64230.50
Running in Android WebView - using real prices from server

// 浏览器模式
Running in browser - using simulated prices
```

### 测试真实价格
在 Chrome DevTools Console:
```javascript
// 手动触发价格更新
window.updatePrice(99999.99)

// 检查当前价格
console.log('Current price:', document.querySelector('[current-price]'))
```

## 📈 性能和优化

### 当前配置
- **更新频率**: 1秒/次
- **网络延迟**: 取决于服务器响应 (通常 < 100ms)
- **数据量**: 每次请求约 100-200 bytes
- **MongoDB 存储**: 每次请求写入一条日志

### 优化建议

1. **降低频率** (如果不需要秒级更新)
   ```kotlin
   delay(2000) // 改为 2 秒更新一次
   ```

2. **本地缓存** (减少服务器压力)
   ```kotlin
   // 只在价格变化超过阈值时更新
   if (abs(price - lastPrice) > 10) {
       webView.evaluateJavascript(...)
   }
   ```

3. **WebSocket** (更高效的实时通信)
   - 替代轮询机制
   - 服务器主动推送价格变化
   - 减少网络请求

## 🛠️ 配置选项

### 修改交易对
在 WebViewPredictionGameActivity.kt:51:
```kotlin
symbol = "ETHUSDT", // 改为以太坊
```

### 修改服务器地址
在 build.gradle 或 BuildConfig:
```groovy
buildConfigField "String", "DEFAULT_BASE_URL", "\"http://your-server:8080/\""
```

### 修改更新频率
在 WebViewPredictionGameActivity.kt:68:
```kotlin
delay(500) // 改为 0.5 秒更新
```

## 🎯 后续增强

可以考虑添加：
- [ ] 价格警报 (价格超过阈值时通知)
- [ ] 多币种支持 (让用户选择交易对)
- [ ] 离线模式 (网络断开时使用缓存价格)
- [ ] 游戏历史记录 (保存到 MongoDB)
- [ ] 排行榜 (记录最高胜率)
- [ ] WebSocket 实时推送
- [ ] 价格预测 AI

## ✅ 测试清单

- [x] 服务器能正确返回价格
- [x] Android 能成功调用 API
- [x] WebView 能接收价格更新
- [x] React 能显示真实价格
- [x] 价格历史图表正确
- [x] 游戏结算使用真实价格
- [x] 生命周期管理正确 (暂停/恢复)
- [x] 浏览器模式仍然可用
- [ ] 网络错误处理
- [ ] 长时间运行稳定性

## 🚀 部署

### 安装 APK
```bash
cd android-app
./gradlew installDebug
```

### 验证运行
1. 确保服务器正在运行
2. 打开 Android App
3. 点击"预测游戏"
4. 观察价格是否实时更新

**现在游戏使用的是真实的 Bitcoin 市场价格！** 🎉
