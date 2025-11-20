# Task1 演示代码说明

本目录包含两段 Java 小程序，分别演示：
1. 调用 Binance 行情 API 并打印指定代币的最新价格。
2. 向 MongoDB Atlas 云数据库写入并读取字符串。

## 环境准备
- JDK 11 及以上（课程环境默认 macOS + `openjdk`）。
- Maven 3.9+。
- MongoDB Atlas 帐号，已创建数据库集群，并获得 SRV 连接字符串。

## 构建
```bash
cd task1-demo
mvn clean package
```
构建后会生成 `target/task1-demo-1.0-SNAPSHOT-jar-with-dependencies.jar`，用于直接运行。

## 1. Binance 行情演示
```bash
# 如果默认域名受限，可改用 Binance US
export BINANCE_API_BASE="https://api.binance.us/api/v3/ticker/price"

# BTC 对美元
java -cp target/task1-demo-1.0-SNAPSHOT-jar-with-dependencies.jar \
  edu.cmu.project4.task1.FetchBinancePrice BTCUSD
```
运行结果示例：
```
Base URL: https://api.binance.us/api/v3/ticker/price
Symbol: BTCUSD
Price: 103722.01000000
Retrieved at: 2025-11-06T04:21:11.466822Z
```
将该控制台输出截图收录到 Task1 PDF 文档中。

## 2. MongoDB Atlas 读写演示
先设置环境变量：
```bash
export MONGODB_URI="mongodb+srv://<用户名>:<密码>@<集群地址>/?retryWrites=true&w=majority"
export MONGODB_DATABASE="project4"           # 可选，默认 project4
export MONGODB_COLLECTION="task1Strings"     # 可选，默认 task1Strings
```
然后运行：
```bash
java -cp target/task1-demo-1.0-SNAPSHOT-jar-with-dependencies.jar \
  edu.cmu.project4.task1.MongoStringDemo
```
程序会提示输入字符串，成功写入后再打印集合内的所有字符串。将控制台输出截图收录到 PDF。

## Task1 文档建议结构
1. 姓名 + Andrew ID。
2. API 名称：Binance Spot Price API。
3. API 文档链接：https://binance-docs.github.io/apidocs/spot/en/#symbol-price-ticker
4. 移动端应用构想（1–3 句）。例如：
   > 移动端允许用户自选币种，展示实时价格与 24 小时涨跌幅，并可设置阈值提醒。
5. Binance 行情程序截图。
6. MongoDB 读写程序截图。
7. 将文档导出为 PDF，于 11 月 7 日 23:59 前提交 Canvas。

> 提示：后续 Task2 需要复用该 API 与 MongoDB，因此建议在文档中注明未来扩展方向（例如记录移动端请求日志、统计异常等）。
