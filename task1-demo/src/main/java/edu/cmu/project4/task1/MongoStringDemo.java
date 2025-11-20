package edu.cmu.project4.task1;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.Scanner;

/**
 * Minimal CLI utility that writes a user-supplied string to MongoDB Atlas and prints all stored strings.
 */
public final class MongoStringDemo {
    private static final String DEFAULT_DATABASE = "project4";
    private static final String DEFAULT_COLLECTION = "task1Strings";

    private MongoStringDemo() {
    }

    public static void main(String[] args) {
        String connectionString = System.getenv("MONGODB_URI");
        if (connectionString == null || connectionString.isBlank()) {
            System.err.println("Environment variable MONGODB_URI is missing. Set it to your MongoDB Atlas connection string.");
            return;
        }

        String databaseName = envOrDefault("MONGODB_DATABASE", DEFAULT_DATABASE);
        String collectionName = envOrDefault("MONGODB_COLLECTION", DEFAULT_COLLECTION);

        try (MongoClient client = MongoClients.create(buildSettings(connectionString))) {
            MongoDatabase database = client.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a string to store in MongoDB: ");
            String input = scanner.nextLine();

            Document document = new Document()
                    .append("message", input)
                    .append("createdAt", Instant.now().toString());
            collection.insertOne(document);
            System.out.printf("Stored document with id: %s%n", document.get("_id"));

            System.out.println("\nStrings currently persisted:");
            for (Document doc : collection.find()) {
                System.out.printf("- %s%n", doc.getString("message"));
            }
        } catch (Exception e) {
            System.err.printf("MongoDB operation failed: %s%n", e.getMessage());
        }
    }

    private static MongoClientSettings buildSettings(String connectionString) {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();
    }

    private static String envOrDefault(String envName, String fallback) {
        String value = System.getenv(envName);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
