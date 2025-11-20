/*
 * Author: Siyuan Liu (sliu5)
 */

package edu.cmu.project4.server.data;

import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable representation of a request/response interaction that is persisted to MongoDB.
 */
public final class RequestLog {
    private final String requestId;
    private final Instant requestReceivedAt;
    private final Instant responseSentAt;
    private final String clientIp;
    private final String clientId;
    private final String symbol;
    private final boolean success;
    private final String errorMessage;
    private final BigDecimal price;
    private final int binanceStatus;
    private final long binanceLatencyMs;
    private final long totalLatencyMs;
    private final String binanceEndpoint;

    public RequestLog(String requestId,
                      Instant requestReceivedAt,
                      Instant responseSentAt,
                      String clientIp,
                      String clientId,
                      String symbol,
                      boolean success,
                      String errorMessage,
                      BigDecimal price,
                      int binanceStatus,
                      long binanceLatencyMs,
                      long totalLatencyMs,
                      String binanceEndpoint) {
        this.requestId = requestId;
        this.requestReceivedAt = requestReceivedAt;
        this.responseSentAt = responseSentAt;
        this.clientIp = clientIp;
        this.clientId = clientId;
        this.symbol = symbol;
        this.success = success;
        this.errorMessage = errorMessage;
        this.price = price;
        this.binanceStatus = binanceStatus;
        this.binanceLatencyMs = binanceLatencyMs;
        this.totalLatencyMs = totalLatencyMs;
        this.binanceEndpoint = binanceEndpoint;
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getRequestReceivedAt() {
        return requestReceivedAt;
    }

    public Instant getResponseSentAt() {
        return responseSentAt;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getBinanceStatus() {
        return binanceStatus;
    }

    public long getBinanceLatencyMs() {
        return binanceLatencyMs;
    }

    public long getTotalLatencyMs() {
        return totalLatencyMs;
    }

    public String getBinanceEndpoint() {
        return binanceEndpoint;
    }

    public Document toDocument() {
        Document document = new Document()
                .append("requestId", requestId)
                .append("requestReceivedAt", requestReceivedAt.toString())
                .append("responseSentAt", responseSentAt.toString())
                .append("clientIp", clientIp)
                .append("clientId", clientId)
                .append("symbol", symbol)
                .append("success", success)
                .append("errorMessage", errorMessage)
                .append("binanceStatus", binanceStatus)
                .append("binanceLatencyMs", binanceLatencyMs)
                .append("totalLatencyMs", totalLatencyMs)
                .append("binanceEndpoint", binanceEndpoint);
        if (price != null) {
            document.append("price", new Decimal128(price));
        }
        return document;
    }

    public static RequestLog fromDocument(Document document) {
        String requestId = document.getString("requestId");
        Instant requestReceivedAt = Instant.parse(document.getString("requestReceivedAt"));
        Instant responseSentAt = Instant.parse(document.getString("responseSentAt"));
        String clientIp = document.getString("clientIp");
        String clientId = document.getString("clientId");
        String symbol = document.getString("symbol");
        boolean success = Boolean.TRUE.equals(document.getBoolean("success"));
        String errorMessage = document.getString("errorMessage");
        Decimal128 priceValue = document.get("price", Decimal128.class);
        BigDecimal price = priceValue != null ? priceValue.bigDecimalValue() : null;
        int binanceStatus = document.getInteger("binanceStatus", 0);
        long binanceLatencyMs = document.getLong("binanceLatencyMs") == null ? 0 : document.getLong("binanceLatencyMs");
        long totalLatencyMs = document.getLong("totalLatencyMs") == null ? 0 : document.getLong("totalLatencyMs");
        String binanceEndpoint = document.getString("binanceEndpoint");
        return new RequestLog(requestId, requestReceivedAt, responseSentAt, clientIp, clientId, symbol, success,
                errorMessage, price, binanceStatus, binanceLatencyMs, totalLatencyMs, binanceEndpoint);
    }
}
