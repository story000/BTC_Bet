# BTC Bet – Web-Only

This repo now contains a web backend plus a static web frontend (no Android code).

- **Frontend**: `bitcoin-game/dist/` (static files ready for Vercel/GitHub Pages). Default backend is wired to the Cloud Run URL.
- **Backend**: `server/` (Java Servlet app on Tomcat). REST endpoint `/api/price` + dashboard `/dashboard`, logs to MongoDB Atlas.
- **Docker**: Root `Dockerfile` builds the WAR and runs Tomcat.
- **Deployment (examples)**:
  - Vercel: `vercel --prod --cwd bitcoin-game/dist --yes`
  - Cloud Run: `gcloud run deploy ... --image gcr.io/<PROJECT>/btc-bet-backend --allow-unauthenticated --port 8080 --env-vars-file=env.yaml`

Key environment variables for the backend:
- `MONGODB_URI` (required), `MONGODB_DATABASE`, `MONGODB_COLLECTION`
- `BINANCE_API_BASE` (optional override)
