# Task 2 Web Service

This directory contains the Maven web app deployed to Tomcat:
- `/api/price`: REST endpoint for the mobile client. Accepts `symbol` (default `BTCUSD`) and optional `clientId`; returns the latest Binance quote and timestamp.
- `/dashboard`: Ops dashboard that visualizes request totals, success rate, avg latency, popular symbols, and the most recent logs.

## Requirements
- JDK 11+
- Maven 3.9+
- MongoDB Atlas (configure via `MONGODB_URI`, `MONGODB_DATABASE`, `MONGODB_COLLECTION`)
- Optional `BINANCE_API_BASE` to override the default Binance price endpoint

## Build
```bash
mvn -f server/pom.xml clean package
```
The WAR is produced at `server/target/ROOT.war`. The provided `Dockerfile` builds and deploys it to Tomcat inside Codespaces/containers.

## Logged Fields
Every `/api/price` request records:
1. `requestId`, `requestReceivedAt`, `responseSentAt`
2. `clientId` and origin IP
3. Requested `symbol`
4. Binance HTTP status, latency, and endpoint
5. Returned price (if successful) plus total processing latency
6. Success/failure flag and any error message

The dashboard aggregates these metrics to display popular symbols, success rate, and average response times.
