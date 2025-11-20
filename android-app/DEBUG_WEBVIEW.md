# WebView 调试指南

## ✅ 已修复的问题

### 1. 资源路径问题
- ✅ 将 Vite 配置改为相对路径 `base: './'`
- ✅ HTML 中的资源从 `/assets/` 改为 `./assets/`

### 2. WebView 权限问题
已添加关键配置：
```kotlin
allowFileAccessFromFileURLs = true      // 允许 file:// 访问其他 file://
allowUniversalAccessFromFileURLs = true // 允许跨域（ES modules 必需）
```

## 🔍 如何查看日志

### Android Studio Logcat
1. 打开 Android Studio
2. View → Tool Windows → Logcat
3. 在过滤器中输入：`WebViewGameActivity`

### 常见日志标记
- `WebViewGameActivity` - 我们的自定义日志
- `chromium` - WebView 内部日志
- `Console` - JavaScript console.log

### 查找关键信息
```
// 页面加载成功
Page loaded: file:///android_asset/game/index.html

// JavaScript 错误
Console: SyntaxError: Unexpected token

// 资源加载失败
WebView Error: -1 - File not found at file://...
```

## 🧪 测试步骤

### 1. 基础测试
- [ ] App 能启动
- [ ] 能打开主界面
- [ ] 点击"预测游戏"按钮

### 2. WebView 测试
- [ ] 页面不是空白的
- [ ] 能看到黑色背景
- [ ] 能看到 CURRENT POINTS 显示
- [ ] 能看到两个大按钮（RISE / FALL）

### 3. 功能测试
- [ ] 点击 RISE 按钮有反应（按钮变大、发光）
- [ ] 点击 FALL 按钮有反应
- [ ] 能看到价格数字在跳动
- [ ] 点击 START GAME 开始倒计时
- [ ] 10秒后显示结果（VICTORY 或 DEFEAT）

## 🐛 常见问题

### 问题 1：页面完全空白
**症状**：只看到白色屏幕

**原因**：资源路径错误或 WebView 权限不足

**检查**：
```bash
# 检查 assets 文件是否存在
ls -la app/src/main/assets/game/

# 应该看到：
# index.html
# assets/index-*.js
# assets/index-*.css
```

**解决**：确保已经：
1. 运行 `npm run build` (在 bitcoin-game 目录)
2. 复制文件到 Android assets
3. WebView 配置正确

### 问题 2：看到黑色背景但没有内容
**症状**：页面加载了但没有游戏界面

**原因**：JavaScript 加载失败或执行错误

**检查 Logcat**：
```
adb logcat | grep -i "chromium\|console"
```

**常见错误**：
- `CORS error` - 跨域问题（需要 allowUniversalAccessFromFileURLs）
- `Module not found` - ES module 加载失败
- `Uncaught SyntaxError` - JavaScript 语法错误

### 问题 3：游戏能看到但动画卡顿
**原因**：WebView 硬件加速未启用

**解决**：在 AndroidManifest.xml 中添加：
```xml
<application
    android:hardwareAccelerated="true">
```

## 🔧 高级调试

### Chrome DevTools 远程调试
1. 连接 Android 设备到电脑
2. 在设备上打开游戏页面
3. Chrome 浏览器访问：`chrome://inspect`
4. 找到你的 WebView，点击 "inspect"
5. 可以查看：
   - Console（JavaScript 日志和错误）
   - Elements（DOM 结构）
   - Network（资源加载）
   - Performance（性能分析）

### 检查 JavaScript Bridge
在 Chrome DevTools Console 中测试：
```javascript
// 检查 Bridge 是否存在
typeof AndroidBridge

// 测试日志函数
AndroidBridge.log("Hello from WebView!")
```

### 手动触发 Balance 更新
```javascript
// 在 DevTools Console 中
window.updateBalance(99999)
```

## 📱 在真机上测试

### USB 调试
1. 手机开启"开发者选项"
2. 开启"USB 调试"
3. 连接到电脑
4. 运行：
```bash
cd android-app
./gradlew installDebug
```

### 无线调试（Android 11+）
1. 手机和电脑连接同一 WiFi
2. 手机开启"无线调试"
3. 使用配对码连接：
```bash
adb pair <IP>:<PORT>
adb connect <IP>:<PORT>
```

## 📊 性能检查

### 检查包大小
```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
# 应该约 7-8 MB
```

### 检查 assets 大小
```bash
du -sh app/src/main/assets/game/
# 应该约 180-200 KB
```

## 🚀 如果一切正常

你应该看到：
- ✨ 流畅的动画效果
- 🎨 渐变背景和模糊光晕
- 📈 实时价格图表
- ⚡ 按钮交互反馈
- 🎯 倒计时和结果展示

与浏览器版本视觉效果完全一致！

## 🆘 还是不行？

提供以下信息：
1. Logcat 完整日志（搜索 WebViewGameActivity）
2. Chrome DevTools Console 截图
3. 当前看到的界面截图
4. Android 版本和设备型号
