# Android App 安装说明

## 问题已修复 ✅

已解决 WebView 资源路径问题：
- ✅ 将 Vite 构建配置改为相对路径 (`base: './'`)
- ✅ 重新构建 React 应用
- ✅ 更新 Android assets
- ✅ 重新编译 Android 应用

## 安装方法

### 方法 1：使用 Gradle 安装（推荐）

```bash
cd android-app
./gradlew installDebug
```

### 方法 2：手动安装 APK

1. APK 位置：
```
android-app/app/build/outputs/apk/debug/app-debug.apk
```

2. 将 APK 传输到 Android 设备：
   - 通过 USB 传输
   - 通过邮件/云盘下载
   - 使用 Android Studio 的 Device File Explorer

3. 在设备上点击 APK 文件安装

### 方法 3：使用 Android Studio

1. 打开 Android Studio
2. 打开项目 `android-app/`
3. 连接设备或启动模拟器
4. 点击运行按钮 ▶️

## 验证是否成功

1. 打开 App
2. 点击"预测游戏"按钮
3. 应该能看到完整的游戏界面（带动画、渐变等效果）

## 如果还是不行

检查 Logcat 日志：
```bash
# 方法 1：通过 Android Studio
# View -> Tool Windows -> Logcat
# 筛选标签：WebViewGameActivity

# 方法 2：命令行（如果有 adb）
adb logcat | grep -i "WebViewGameActivity\|chromium\|console"
```

## 调试 WebView

启用 Chrome 远程调试：
1. 连接设备到电脑
2. Chrome 浏览器访问：`chrome://inspect`
3. 在 "Remote Target" 中找到 WebView
4. 点击 "inspect" 查看控制台

## 更新游戏内容

如果需要修改游戏：

```bash
# 1. 修改 React 代码
cd bitcoin-game
# 编辑 src/BitcoinGame.jsx

# 2. 重新构建
npm run build

# 3. 复制到 Android
cp -r dist/* ../android-app/app/src/main/assets/game/

# 4. 重新构建 Android
cd ../android-app
./gradlew assembleDebug

# 5. 安装
./gradlew installDebug
```
