package edu.cmu.project4.server.data;

/**
 * Represents the number of requests per symbol for dashboard analytics.
 */
public final class SymbolStats {
    private final String symbol;
    private final long count;

    public SymbolStats(String symbol, long count) {
        this.symbol = symbol;
        this.count = count;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getCount() {
        return count;
    }
}
