# Project 4 Task 2 – Crypto Monitor & Prediction Game
Siyuan Liu (sliu5)

**Description:** The Android client lets users track live Binance spot prices and play a prediction mini-game backed by the same price feed. A Java servlet deployed via GitHub Codespaces proxies Binance, logs every request to MongoDB Atlas, and exposes an operations dashboard that summarizes usage analytics.

***
## Requirement 1 – Native Android Application
**Project:** `android-app`

**a. Multiple views**  
`activity_main.xml` combines `TextView`, `TextInputEditText`, `MaterialButton`, `ProgressBar`, `MPAndroidChart`'s `LineChart`, and `Snackbar` elements inside a `NestedScrollView`, satisfying the “three different views” rule.

**b. Requires user input**  
Users type the trading symbol and server URL. Additional controls on `activity_home.xml` let the user jump between the monitor and the WebView game.

**c. Makes an HTTP request**  
`PriceRepository` builds a Retrofit client (`PriceApi#fetchPrice`) that issues a `GET /api/price?symbol=…&clientId=…` request to the servlet. The WebView game reuses the same repository when streaming quotes.

**d. Parses an XML/JSON reply**  
The app consumes the JSON payload via Moshi into `PriceResponse { symbol, price, fetchedAt }`. The ViewModel then converts the timestamp into `Instant` objects for charting.

**e. Displays new information**  
`MainActivity` renders the latest price, timestamp, status, and any error. The price history also animates inside the line chart, and the WebView shows the live price inside the React game UI.

**f. Repeatable**  
`PriceViewModel` polls continuously in `startRealtimeStream()` and exposes a manual **Fetch Price** button. Users can submit arbitrary symbols and repeat without relaunching the activity.

Screenshots:

![Home navigation screen](docs/snapshot/home.png)

![Monitoring workflow with live chart](docs/snapshot/monitor.png)

![Prediction game fed by live prices](docs/snapshot/game.png)

![Jackpot win state surfaced inside the WebView game](docs/snapshot/jackpot.png)

***
## Requirement 2 – Web Service
**Project:** `server`

**a. Simple servlet API**  
`PriceServlet` is mapped to `/api/price`. `DashboardServlet` is mapped to `/dashboard` for human viewers. No frameworks beyond Servlets/JSP.

**b. Receives requests from Android**  
The mobile client calls `/api/price` with `symbol` and `clientId` query params. The servlet reads both, normalizes the symbol, and stamps metadata (requestId, timestamps, IP).

**c. Business logic & third-party API**  
`BinanceClient` performs the outbound HTTP call to Binance’s [Symbol Price Ticker](https://binance-docs.github.io/apidocs/spot/en/#symbol-price-ticker) endpoint and measures latency. The servlet stores the price, status, latencies, and client metadata in MongoDB before replying.

**d. JSON reply**  
`respondWithQuote` writes JSON with just the fields the app needs (`symbol`, `price`, `fetchedAt`). Errors are returned as JSON `{ "error": "…" }` with appropriate HTTP status codes.

![Standalone price fetch verification](docs/snapshot/price_fetch.png)

***
## Requirement 3 – Handle Error Conditions
- **Invalid mobile input:** Empty or null symbols default to `BTCUSD`; URL fields are validated via `TextInputLayout`. Retrofit failures surface via Snackbars with actionable text.
- **Server-side validation:** Servlet sanitizes/uppercases symbols, generates UUID request IDs, and wraps Binance failures in `BinanceClientException` to ensure deterministic error responses.
- **Network failures:** ViewModel catches network exceptions, stops the loading spinner, and shows the error string. WebView game throttles retries and warns the user every five failed polls.
- **Third-party API issues:** Non-200 responses and malformed Binance payloads raise `BinanceClientException` with the HTTP status and partial body for logging. The servlet returns `502` (Binance error) versus `500` (internal error).

***
## Requirement 4 – Log Useful Information
`RequestLog` captures more than six attributes per request and explains server state:
1. `requestId`, `requestReceivedAt`, `responseSentAt` – correlate frontend and backend timings.
2. `clientId` and `clientIp` – aggregate analytics by device or network.
3. Requested `symbol` – drives the “Top Symbols” card.
4. `price` – useful for auditing prediction-game rounds.
5. Binance `statusCode`, `latencyMs`, and the queried endpoint – highlight upstream health.
6. Overall `totalLatencyMs`, `success` flag, and `errorMessage` – measure SLA and failure modes.
7. `binanceEndpoint` – documents whether Binance Global or Binance US was used.
Only `/api/price` traffic is logged; dashboard page views are intentionally excluded per the spec.

***
## Requirement 5 – Store Logs in MongoDB Atlas
- **Driver:** `mongodb-driver-sync` (v4.11.1) inside `MongoLogRepository`.
- **Atlas connection string (three shards):**  
  `mongodb://gushi10546_db_user:suqianye10546@ac-dw50pnt-shard-00-00.igaq6gr.mongodb.net:27017,ac-dw50pnt-shard-00-01.igaq6gr.mongodb.net:27017,ac-dw50pnt-shard-00-02.igaq6gr.mongodb.net:27017/?ssl=true&replicaSet=atlas-mxqpie-shard-0&authSource=admin&retryWrites=true&w=majority`
- **Database & collection:** `project4` / `requestLogs` (configurable via `MONGODB_DATABASE` and `MONGODB_COLLECTION`).
`AppContextListener` creates the Mongo client at startup, shares it via `ServletContext`, and closes it cleanly when Tomcat shuts down.

![MongoDB Atlas collection populated with logs](docs/snapshot/mongodb.png)

***
## Requirement 6 – Dashboard (Analytics + Logs)
**URL:** `/dashboard` (served from `WEB-INF/jsp/dashboard.jsp`).

**Analytics shown:**
1. **Total Requests** – via `MongoLogRepository#totalCount()`.
2. **Success Rate** – `successCount / totalCount` rendered as a percent.
3. **Average Response Latency** – aggregation pipeline calculating avg `totalLatencyMs`.
4. **Top Requested Symbols** – aggregated counts limited to the top five.

**Formatted logs:**  
The JSP renders a full-width HTML table with timestamp, request ID, client identifier, symbol, price, Binance status, upstream latency, total latency, status (OK/Failed), and error text. Each row corresponds to a MongoDB document so TAs can audit individual requests without reading JSON.

![Dashboard analytics + formatted logs](docs/snapshot/dashboard.png)

***
## Requirement 7 – Deployment to GitHub Codespaces
1. The repository includes `.devcontainer.json` + `Dockerfile` at the root. Codespaces builds Tomcat, copies `ROOT.war`, and launches Catalina automatically.
2. After Codespace launch, port **8080** is exposed and set to **Public** so the Android app can reach it without GitHub auth.
3. Current deployment URL: `https://musical-funicular-x4wr6rq7j9rhpg5p-8080.app.github.dev/`. This matches the default value baked into `BuildConfig.DEFAULT_BASE_URL`, so installing a fresh APK immediately targets the cloud instance.
4. `mvn -f server/pom.xml clean package` produces `ROOT.war`, which is copied to the repo before pushing to GitHub Classroom.
5. Verified from an Incognito browser plus an emulator hitting the same URL.

![Codespaces port made public](docs/snapshot/codespace_port.png)

***
## Appendix – Screenshot Index
- `docs/snapshot/home.png` – Landing page with navigation buttons.
- `docs/snapshot/monitor.png` – Monitoring screen showing live price + chart (Req. 1/2).
- `docs/snapshot/game.png` – Prediction mini-game inside WebView.
- `docs/snapshot/price_fetch.png` – Standalone servlet response for `/api/price`.
- `docs/snapshot/dashboard.png` – Dashboard analytics and formatted logs (Req. 6).
- `docs/snapshot/codespace_port.png` – Codespaces Ports tab with 8080 set to Public (Req. 7).
- `docs/snapshot/jackpot.png` – Bonus view of the jackpot alert used in the WebView game.
- `docs/snapshot/mongodb.png` – Atlas collection showing persisted request logs (Req. 5).

These files now live in `docs/snapshot/` and are ready for inclusion when exporting to PDF.
