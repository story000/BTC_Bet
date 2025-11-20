/*
 * Author: Siyuan Liu (sliu5)
 */

package edu.cmu.project4.task1;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple CLI utility that calls the Binance ticker endpoint and prints the latest price.
 */
public final class FetchBinancePrice {
    private static final String DEFAULT_BASE_URL = "https://api.binance.com/api/v3/ticker/price";
    private static final String DEFAULT_SYMBOL = "BTCUSD";

    private FetchBinancePrice() {
    }

    public static void main(String[] args) {
        String symbol = args.length > 0 ? args[0].toUpperCase(Locale.ROOT) : DEFAULT_SYMBOL;
        String baseUrl = envOrDefault("BINANCE_API_BASE", DEFAULT_BASE_URL);
        try {
            String price = fetchPrice(baseUrl, symbol);
            System.out.printf("Base URL: %s%nSymbol: %s%nPrice: %s%nRetrieved at: %s%n", baseUrl, symbol, price, Instant.now());
        } catch (Exception e) {
            System.err.printf("Failed to fetch price for %s via %s: %s%n", symbol, baseUrl, e.getMessage());
            System.err.println("Tip: set BINANCE_API_BASE to https://api.binance.us/api/v3/ticker/price if you are in a restricted region.");
        }
    }

    private static String fetchPrice(String baseUrl, String symbol) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s?symbol=%s", baseUrl, symbol)))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected HTTP status: " + response.statusCode() + " body=" + response.body());
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode priceNode = root.get("price");
        if (priceNode == null) {
            throw new IOException("Response did not include a price field");
        }
        return priceNode.asText();
    }

    private static String envOrDefault(String envName, String fallback) {
        String value = System.getenv(envName);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
