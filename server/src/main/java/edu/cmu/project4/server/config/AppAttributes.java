/*
 * Author: Siyuan Liu (sliu5)
 */

package edu.cmu.project4.server.config;

/**
 * Attribute names stored on the ServletContext.
 */
public final class AppAttributes {
    public static final String CONFIG = "appConfig";
    public static final String BINANCE_CLIENT = "binanceClient";
    public static final String MONGO_REPOSITORY = "mongoRepository";
    public static final String OBJECT_MAPPER = "objectMapper";

    private AppAttributes() {
    }
}
