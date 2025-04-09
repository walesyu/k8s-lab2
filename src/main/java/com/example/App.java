package com.example;

import com.example.services.CacheService;
import com.example.services.ConfigurationService;
import com.example.services.ServiceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // 替換為你的 Azure Cache for Redis 連線資訊
        String redisHost = "your-redis-instance.redis.cache.windows.net";
        int redisPort = 6379;
        String redisPassword = "your-redis-password";

        // 替換為你的 Azure HDInsight ZooKeeper 連線字串 (例如: zk0-xxx.azurehdinsight.net:2181,zk1-xxx.azurehdinsight.net:2181)
        String zkConnectionString = "your-zookeeper-quorum:2181";

        CacheService cacheService = new CacheService(redisHost, redisPort, redisPassword);
        ConfigurationService configService = new ConfigurationService(zkConnectionString);
        ServiceDiscoveryService discoveryService = new ServiceDiscoveryService(zkConnectionString);

        // 範例：使用 Redis 快取
        String dataKey = "my-data";
        String cachedData = cacheService.get(dataKey);
        if (cachedData != null) {
            logger.info("Data retrieved from cache: {}", cachedData);
        } else {
            // 從資料庫或其他來源獲取資料
            String freshData = "This is the fresh data!";
            cacheService.set(dataKey, freshData, 60); // 快取 60 秒
            logger.info("Data retrieved from source and cached: {}", freshData);
        }

        // 範例：使用 ZooKeeper 進行配置管理
        String dbUrlKey = "database.url";
        String dbUrl = configService.getConfig(dbUrlKey);
        if (dbUrl != null) {
            logger.info("Database URL from ZooKeeper: {}", dbUrl);
        } else {
            configService.setConfig(dbUrlKey, "jdbc:mydb://localhost:3306/mydatabase");
            logger.info("Database URL not found, set default in ZooKeeper.");
        }

        // 範例：使用 ZooKeeper 進行服務發現
        discoveryService.registerService("localhost", 8080);
        logger.info("Registered this service instance.");
        java.util.List<String> availableServices = discoveryService.discoverServices();
        logger.info("Available services: {}", availableServices);

        // 模擬應用程式運行一段時間
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 關閉連接
        cacheService.close();
        configService.close();
        discoveryService.close();
    }
}