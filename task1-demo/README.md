# Task 1 Demo Programs

This folder contains two small Java utilities:
1. Fetch the latest Binance spot price for a symbol.
2. Write/read a string entry in MongoDB Atlas.

## Prerequisites
- JDK 11+
- Maven 3.9+
- MongoDB Atlas cluster and SRV connection string

## Build
```bash
cd task1-demo
mvn clean package
```
Creates `target/task1-demo-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Binance Price Demo
```bash
# Optional: switch to Binance US if the default endpoint is blocked
export BINANCE_API_BASE="https://api.binance.us/api/v3/ticker/price"

java -cp target/task1-demo-1.0-SNAPSHOT-jar-with-dependencies.jar \
  edu.cmu.project4.task1.FetchBinancePrice BTCUSD
```
Sample output:
```
Base URL: https://api.binance.us/api/v3/ticker/price
Symbol: BTCUSD
Price: 103722.01000000
Retrieved at: 2025-11-06T04:21:11.466822Z
```
Capture the console screenshot for the Task 1 PDF.

## MongoDB Atlas Demo
```bash
export MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>/?retryWrites=true&w=majority"
export MONGODB_DATABASE="project4"
export MONGODB_COLLECTION="task1Strings"

java -cp target/task1-demo-1.0-SNAPSHOT-jar-with-dependencies.jar \
  edu.cmu.project4.task1.MongoStringDemo
```
The program prompts for a string, inserts it, then prints all stored entries. Screenshot the console output for the PDF.

## Suggested PDF Outline
1. Name + Andrew ID.
2. API name: Binance Symbol Price Ticker.
3. Reference: https://binance-docs.github.io/apidocs/spot/en/#symbol-price-ticker.
4. Mobile app concept (1–3 sentences).
5. Screenshot of the Binance price demo.
6. Screenshot of the MongoDB demo.
7. Export to PDF and submit before Nov 7, 23:59.

Tip: Mention how the same API/Mongo setup will be reused in Task 2 for logging and analytics.
