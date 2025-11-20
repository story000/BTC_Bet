package edu.cmu.project4.server.biz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * Simple HTTP client around the Binance ticker endpoint.
 */
public class BinanceClient {
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public BinanceClient(String baseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public PriceResult fetchPrice(String symbol) throws IOException, InterruptedException, BinanceClientException {
        String normalizedSymbol = symbol.toUpperCase();
        String url = baseUrl + "?symbol=" + URLEncoder.encode(normalizedSymbol, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        Instant start = Instant.now();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        long latencyMs = Duration.between(start, Instant.now()).toMillis();
        if (response.statusCode() != 200) {
            throw new BinanceClientException("Unexpected Binance status", response.statusCode(), latencyMs, response.body());
        }
        JsonNode root = mapper.readTree(response.body());
        JsonNode priceNode = root.get("price");
        if (priceNode == null) {
            throw new BinanceClientException("Response missing price field", response.statusCode(), latencyMs, response.body());
        }
        BigDecimal price = new BigDecimal(priceNode.asText());
        Instant fetchedAt = Instant.now();
        PriceQuote quote = new PriceQuote(normalizedSymbol, price, fetchedAt);
        return new PriceResult(quote, response.statusCode(), latencyMs, url);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public static final class PriceResult {
        private final PriceQuote quote;
        private final int statusCode;
        private final long latencyMs;
        private final String endpoint;

        public PriceResult(PriceQuote quote, int statusCode, long latencyMs, String endpoint) {
            this.quote = quote;
            this.statusCode = statusCode;
            this.latencyMs = latencyMs;
            this.endpoint = endpoint;
        }

        public PriceQuote getQuote() {
            return quote;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }

    public static final class BinanceClientException extends Exception {
        private final int statusCode;
        private final long latencyMs;
        private final String responseBody;

        public BinanceClientException(String message, int statusCode, long latencyMs, String responseBody) {
            super(message);
            this.statusCode = statusCode;
            this.latencyMs = latencyMs;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public long getLatencyMs() {
            return latencyMs;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }

    public static final class PriceQuote {
        private final String symbol;
        private final BigDecimal price;
        private final Instant fetchedAt;

        public PriceQuote(String symbol, BigDecimal price, Instant fetchedAt) {
            this.symbol = symbol;
            this.price = price;
            this.fetchedAt = fetchedAt;
        }

        public String getSymbol() {
            return symbol;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public Instant getFetchedAt() {
            return fetchedAt;
        }
    }
}
