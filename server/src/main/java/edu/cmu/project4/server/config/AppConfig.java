/*
 * Author: Siyuan Liu (sliu5)
 */

package edu.cmu.project4.server.config;

import java.util.Objects;

/**
 * Reads environment variables that configure the web service.
 */
public final class AppConfig {
    private static final String DEFAULT_BINANCE_URL = "https://api.binance.com/api/v3/ticker/price";
    private static final String DEFAULT_DATABASE = "project4";
    private static final String DEFAULT_COLLECTION = "requestLogs";

    private final String binanceBaseUrl;
    private final String mongoUri;
    private final String mongoDatabase;
    private final String mongoCollection;

    public AppConfig() {
        this.binanceBaseUrl = envOrDefault("BINANCE_API_BASE", DEFAULT_BINANCE_URL);
        this.mongoUri = Objects.requireNonNullElse(System.getenv("MONGODB_URI"), "");
        this.mongoDatabase = envOrDefault("MONGODB_DATABASE", DEFAULT_DATABASE);
        this.mongoCollection = envOrDefault("MONGODB_COLLECTION", DEFAULT_COLLECTION);

        if (mongoUri.isBlank()) {
            throw new IllegalStateException("Environment variable MONGODB_URI must be configured for the web service.");
        }
    }

    public String getBinanceBaseUrl() {
        return binanceBaseUrl;
    }

    public String getMongoUri() {
        return mongoUri;
    }

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public String getMongoCollection() {
        return mongoCollection;
    }

    private static String envOrDefault(String envName, String fallback) {
        String value = System.getenv(envName);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
