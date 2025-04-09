package com.example.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private final CuratorFramework client;
    private final String configPath = "/app/config"; // ZooKeeper 中配置的路徑

    public ConfigurationService(String zkConnectionString) {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.newClient(zkConnectionString, retryPolicy);
        this.client.start();
        logger.info("ZooKeeper Configuration Service initialized with connection: {}", zkConnectionString);
        ensureConfigPathExists();
    }

    private void ensureConfigPathExists() {
        try {
            if (client.checkExists().forPath(configPath) == null) {
                client.create().creatingParentsIfNeeded().forPath(configPath, "default".getBytes(StandardCharsets.UTF_8));
                logger.info("Created default configuration path: {}", configPath);
            }
        } catch (Exception e) {
            logger.error("Error ensuring config path exists: {}", e.getMessage());
        }
    }

    public String getConfig(String key) {
        String path = configPath + "/" + key;
        try {
            byte[] data = client.getData().forPath(path);
            return new String(data, StandardCharsets.UTF_8);
        } catch (KeeperException.NoNodeException e) {
            logger.warn("Configuration key '{}' not found in ZooKeeper.", key);
            return null;
        } catch (Exception e) {
            logger.error("Error getting configuration for key '{}' from ZooKeeper: {}", key, e.getMessage());
            return null;
        }
    }

    public void setConfig(String key, String value) {
        String path = configPath + "/" + key;
        try {
            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().forPath(path, value.getBytes(StandardCharsets.UTF_8));
                logger.info("Created configuration key '{}' with value '{}' in ZooKeeper.", key, value);
            } else {
                client.setData().forPath(path, value.getBytes(StandardCharsets.UTF_8));
                logger.info("Updated configuration key '{}' with value '{}' in ZooKeeper.", key, value);
            }
        } catch (Exception e) {
            logger.error("Error setting configuration for key '{}' in ZooKeeper: {}", key, e.getMessage());
        }
    }

    public void close() {
        if (client != null) {
            client.close();
            logger.info("ZooKeeper Configuration Service closed.");
        }
    }
}