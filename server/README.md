# Task 2 Web Service

本目录包含部署到 Tomcat 的 Maven Web 应用：
- `/api/price`：移动端调用的 REST 接口。参数 `symbol`（默认 `BTCUSDT`）和可选 `clientId`，返回 Binance 现价与时间戳。
- `/dashboard`：运维仪表盘，展示请求统计（总请求、成功率、平均延迟、热门代币）以及格式化的最新日志表格。

## 运行依赖
- JDK 11+
- Maven 3.9+
- 外部 MongoDB Atlas 实例（通过环境变量 `MONGODB_URI`、`MONGODB_DATABASE`、`MONGODB_COLLECTION` 配置）
- 可选：`BINANCE_API_BASE` 重写默认 Binance 现价端点

## 构建
```bash
mvn -f server/pom.xml clean package
```
产物位于 `server/target/ROOT.war`，`Dockerfile` 会在 Codespace/容器中自动构建并部署到 Tomcat。

## 关键日志字段
每次 `/api/price` 调用会记录：
1. `requestId` 与 `requestReceivedAt`/`responseSentAt`
2. `clientId` 与来源 IP
3. 请求的 `symbol`
4. Binance HTTP 状态、延迟、调用 URL
5. 返回价格（如成功）及总处理延迟
6. 成功/失败标记与错误信息

仪表盘会基于这些字段计算热门币种、成功率以及平均响应时间。
