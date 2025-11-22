/*
 * Author: Siyuan Liu (sliu5)
 */

package edu.cmu.project4.server.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cmu.project4.server.biz.BinanceClient;
import edu.cmu.project4.server.biz.BinanceClient.BinanceClientException;
import edu.cmu.project4.server.biz.BinanceClient.PriceQuote;
import edu.cmu.project4.server.biz.BinanceClient.PriceResult;
import edu.cmu.project4.server.config.AppAttributes;
import edu.cmu.project4.server.data.MongoLogRepository;
import edu.cmu.project4.server.data.RequestLog;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST endpoint consumed by the Android client. Returns latest price information for a symbol.
 */
@WebServlet(name = "PriceServlet", urlPatterns = "/api/price")
public class PriceServlet extends HttpServlet {
    private transient BinanceClient binanceClient;
    private transient MongoLogRepository repository;
    private transient ObjectMapper mapper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.binanceClient = (BinanceClient) config.getServletContext().getAttribute(AppAttributes.BINANCE_CLIENT);
        this.repository = (MongoLogRepository) config.getServletContext().getAttribute(AppAttributes.MONGO_REPOSITORY);
        this.mapper = (ObjectMapper) config.getServletContext().getAttribute(AppAttributes.OBJECT_MAPPER);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(resp);
        String clientId = req.getParameter("clientId");
        String symbol = req.getParameter("symbol");
        if (symbol == null || symbol.isBlank()) {
            symbol = "BTCUSD";
        }
        symbol = symbol.toUpperCase();

        Instant requestTime = Instant.now();
        String requestId = UUID.randomUUID().toString();
        int binanceStatus = 0;
        long binanceLatency = 0;
        BigDecimal priceValue = null;
        boolean success = false;
        String errorMessage = null;
        PriceQuote quote = null;

        try {
            PriceResult result = binanceClient.fetchPrice(symbol);
            binanceStatus = result.getStatusCode();
            binanceLatency = result.getLatencyMs();
            quote = result.getQuote();
            priceValue = quote.getPrice();
            respondWithQuote(resp, result.getQuote());
            success = true;
        } catch (BinanceClientException e) {
            binanceStatus = e.getStatusCode();
            binanceLatency = e.getLatencyMs();
            errorMessage = "Binance error: " + e.getMessage();
            respondWithError(resp, HttpServletResponse.SC_BAD_GATEWAY, errorMessage);
        } catch (Exception e) {
            errorMessage = "Server error: " + e.getMessage();
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
        } finally {
            Instant responseTime = Instant.now();
            long totalLatency = Duration.between(requestTime, responseTime).toMillis();
            RequestLog log = new RequestLog(
                    requestId,
                    requestTime,
                    responseTime,
                    req.getRemoteAddr(),
                    clientId,
                    symbol,
                    success,
                    errorMessage,
                    priceValue,
                    binanceStatus,
                    binanceLatency,
                    totalLatency,
                    binanceClient.getBaseUrl()
            );
            repository.insert(log);
        }
    }

    private void respondWithQuote(HttpServletResponse resp, PriceQuote quote) throws IOException {
        resp.setContentType("application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("symbol", quote.getSymbol());
        body.put("price", quote.getPrice());
        body.put("fetchedAt", quote.getFetchedAt().toString());
        mapper.writeValue(resp.getOutputStream(), body);
    }

    private void respondWithError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        mapper.writeValue(resp.getOutputStream(), body);
    }

    private void addCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
