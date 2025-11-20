package edu.cmu.project4.server.data;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Thin wrapper around the MongoDB collection that stores logs.
 */
public class MongoLogRepository {
    private final MongoCollection<Document> collection;

    public MongoLogRepository(MongoClient client, String databaseName, String collectionName) {
        MongoDatabase database = client.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
    }

    public void insert(RequestLog log) {
        collection.insertOne(log.toDocument());
    }

    public long totalCount() {
        return collection.countDocuments();
    }

    public long successCount() {
        return collection.countDocuments(Filters.eq("success", true));
    }

    public double averageLatencyMs() {
        List<Document> result = collection.aggregate(Arrays.asList(
                Aggregates.group(null, Accumulators.avg("avgLatency", "$totalLatencyMs"))
        )).into(new ArrayList<>());
        if (result.isEmpty()) {
            return 0.0;
        }
        Object value = result.get(0).get("avgLatency");
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    public List<SymbolStats> topSymbols(int limit) {
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
                Aggregates.group("$symbol", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count")),
                Aggregates.limit(limit)
        ));
        List<SymbolStats> stats = new ArrayList<>();
        for (Document document : iterable) {
            stats.add(new SymbolStats(document.getString("_id"), document.getInteger("count", 0)));
        }
        return stats;
    }

    public List<RequestLog> recentLogs(int limit) {
        List<RequestLog> logs = new ArrayList<>();
        for (Document document : collection.find()
                .sort(Sorts.descending("requestReceivedAt"))
                .limit(limit)) {
            logs.add(RequestLog.fromDocument(document));
        }
        return logs;
    }
}
