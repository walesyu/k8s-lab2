package com.example.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryService.class);
    private final CuratorFramework client;
    private final String serviceDiscoveryPath = "/services";
    private final String serviceName = "my-app-service"; // 你的服務名稱

    public ServiceDiscoveryService(String zkConnectionString) {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.newClient(zkConnectionString, retryPolicy);
        this.client.start();
        logger.info("ZooKeeper Service Discovery Service initialized with connection: {}", zkConnectionString);
        ensureServicePathExists();
    }

    private void ensureServicePathExists() {
        try {
            if (client.checkExists().forPath(serviceDiscoveryPath) == null) {
                client.create().creatingParentsIfNeeded().forPath(serviceDiscoveryPath);
                logger.info("Created service discovery path: {}", serviceDiscoveryPath);
            }
        } catch (Exception e) {
            logger.error("Error ensuring service path exists: {}", e.getMessage());
        }
    }

    public void registerService(String host, int port) {
        String serviceInstancePath = ZKPaths.makePath(serviceDiscoveryPath, serviceName, host + ":" + port);
        try {
            client.create()
                    .withMode(CreateMode.EPHEMERAL) // 臨時節點，服務下線會自動刪除
                    .forPath(serviceInstancePath, (host + ":" + port).getBytes(StandardCharsets.UTF_8));
            logger.info("Registered service instance at: {}", serviceInstancePath);
        } catch (Exception e) {
            logger.error("Error registering service instance: {}", e.getMessage());
        }
    }

    public List<String> discoverServices() {
        try {
            List<String> instances = client.getChildren().forPath(ZKPaths.makePath(serviceDiscoveryPath, serviceName));
            return instances.stream()
                    .map(instance -> {
                        try {
                            byte[] data = client.getData().forPath(ZKPaths.makePath(serviceDiscoveryPath, serviceName, instance));
                            return new String(data, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            logger.error("Error getting service instance data for {}: {}", instance, e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (KeeperException.NoNodeException e) {
            logger.warn("No instances found for service: {}", serviceName);
            return List.of();
        } catch (Exception e) {
            logger.error("Error discovering services: {}", e.getMessage());
            return List.of();
        }
    }

    public void close() {
        if (client != null) {
            client.close();
            logger.info("ZooKeeper Service Discovery Service closed.");
        }
    }
}