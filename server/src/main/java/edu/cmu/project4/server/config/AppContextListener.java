/*
 * Author: Siyuan Liu (sliu5)
 */

package edu.cmu.project4.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import edu.cmu.project4.server.biz.BinanceClient;
import edu.cmu.project4.server.data.MongoLogRepository;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Initializes shared singletons for the servlet container.
 */
@WebListener
public class AppContextListener implements ServletContextListener {
    private MongoClient mongoClient;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        AppConfig config = new AppConfig();
        this.mongoClient = MongoClients.create(config.getMongoUri());
        MongoLogRepository repository = new MongoLogRepository(mongoClient, config.getMongoDatabase(), config.getMongoCollection());
        BinanceClient binanceClient = new BinanceClient(config.getBinanceBaseUrl());
        ObjectMapper mapper = new ObjectMapper();

        context.setAttribute(AppAttributes.CONFIG, config);
        context.setAttribute(AppAttributes.MONGO_REPOSITORY, repository);
        context.setAttribute(AppAttributes.BINANCE_CLIENT, binanceClient);
        context.setAttribute(AppAttributes.OBJECT_MAPPER, mapper);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
