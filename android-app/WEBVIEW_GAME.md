# WebView 游戏集成说明

## 概述

已成功将 React 版本的 Bitcoin 预测游戏集成到 Android App 中，使用 WebView 技术实现。

## 实现效果

✅ **100% 保留所有视觉效果**
- 所有 CSS 动画和过渡效果
- 渐变背景和模糊光晕
- SVG 图标和实时图表
- 交互反馈和按钮动画
- 倒计时和结果展示

## 架构说明

### 文件结构

```
android-app/
├── app/src/main/
│   ├── assets/game/              # React 游戏构建文件
│   │   ├── index.html
│   │   └── assets/
│   │       ├── index-*.js
│   │       └── index-*.css
│   └── java/.../ui/game/
│       ├── PredictionGameActivity.kt           # 原生实现（已弃用）
│       └── WebViewPredictionGameActivity.kt    # WebView 实现（新）
```

### JavaScript Bridge

#### Android → JavaScript

```kotlin
// 更新余额
webView.evaluateJavascript("window.updateBalance(12345);", null)

// 更新价格
webView.evaluateJavascript("window.updatePrice(64230.50);", null)
```

#### JavaScript → Android

```javascript
// 游戏结束时通知 Android
window.AndroidBridge.onGameFinished(
  isWin,           // boolean
  betAmount,       // number
  newBalance,      // number
  finalPrice,      // number
  startPrice       // number
);
```

## 使用方式

### 运行应用

1. 构建并安装 APK：
```bash
cd android-app
./gradlew assembleDebug
./gradlew installDebug
```

2. 打开 App，点击"预测游戏"按钮

### 更新游戏内容

如果需要修改游戏 UI 或逻辑：

1. 修改 React 代码：
```bash
cd bitcoin-game
# 修改 src/BitcoinGame.jsx
```

2. 重新构建并复制到 Android：
```bash
npm run build
cp -r dist/* ../android-app/app/src/main/assets/game/
```

3. 重新构建 Android 应用

## 扩展功能

### 添加新的 Bridge 方法

**在 React 中**（src/BitcoinGame.jsx）：
```javascript
useEffect(() => {
  window.customFunction = (param) => {
    // 处理 Android 调用
  };
}, []);
```

**在 Android 中**（WebViewPredictionGameActivity.kt）：
```kotlin
inner class AndroidBridge {
    @JavascriptInterface
    fun customMethod(param: String) {
        // 处理 JavaScript 调用
    }
}
```

### 集成真实服务器 API

可以在 `AndroidBridge` 中添加网络请求：

```kotlin
@JavascriptInterface
fun fetchRealPrice(symbol: String) {
    viewModelScope.launch {
        val price = repository.getPrice(symbol)
        webView.evaluateJavascript(
            "window.updatePrice($price);",
            null
        )
    }
}
```

## 性能优化

- ✅ WebView 硬件加速已启用
- ✅ JavaScript 执行优化
- ✅ 资源本地化（无网络延迟）
- ✅ 独立进程（不影响主应用）

## 对比原生实现

| 特性 | WebView 版本 | 原生版本 |
|-----|------------|---------|
| 开发速度 | ⚡ 快 | 慢 |
| UI 效果 | ✅ 完美 | 😐 基础 |
| 维护成本 | ✅ 低 | 高 |
| 性能 | ✅ 优秀 | ✅ 最佳 |
| 跨平台 | ✅ 可复用 | ❌ 平台特定 |

## 故障排查

### WebView 空白

检查 assets 文件是否正确复制：
```bash
ls -la app/src/main/assets/game/
```

### JavaScript 报错

启用 Chrome 远程调试：
1. 连接设备
2. Chrome 访问 `chrome://inspect`
3. 选择 WebView 页面查看控制台

### Bridge 调用失败

检查 Logcat 输出：
```bash
adb logcat | grep WebViewGameActivity
```

## 下一步改进

- [ ] 接入真实的服务器价格 API
- [ ] 添加用户积分持久化
- [ ] 实现游戏历史记录
- [ ] 添加更多游戏模式
- [ ] 优化资源加载速度
