# Android 客户端 – Crypto Monitor Mobile

该 Android 应用负责调用你在 Task 2 中实现的 `/api/price` 接口，并把设备信息作为 `clientId` 上传，方便服务器端日志记录。

## 功能概览
- 在界面上填写交易对（symbol）与服务器 URL，点击“获取价格”即可发起请求。
- 页面会显示当前价格、服务器返回时间戳、最后一次请求状态与错误信息。
- `clientId` 自动由设备品牌、型号、Android 版本与 `ANDROID_ID` 拼接，无需手动输入。

## 运行步骤
1. **打开 Android Studio**，选择 `File > Open...` 并指向仓库中的 `android-app` 目录。
2. 首次同步如果提示缺少 Gradle Wrapper，可在 Android Studio 的 Terminal 中执行 `gradlew wrapper --gradle-version 8.6` 生成；随后点击 *Sync Now*。
3. 连接实体设备或启动模拟器（API 24+）。
4. 运行 `app` 目标即可在设备上安装。

## 服务器 URL
- 默认 `BuildConfig.DEFAULT_BASE_URL` 设为 `http://10.0.2.2:8080/`，方便在本地 Android 模拟器访问 Codespace/本地 Tomcat。
- 如果已经将 Web 服务部署到公网（例如 Codespaces 公网地址），直接在应用输入框中替换成真实 URL（记得以 `/` 结尾）。

## 依赖
- Kotlin + ViewModel + Coroutines
- Retrofit + Moshi + OkHttp Logging
- Material3 组件、ViewBinding

## 日志关联
客户端会自动将 `clientId=品牌-型号-Android版本-ANDROID_ID` 附加到 API 请求参数，服务器端日志/仪表盘可据此区分不同设备。
