package com.example.services;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final JedisPool jedisPool;

    public CacheService(String redisHost, int redisPort, String redisPassword) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 可以根據需求配置連接池參數
        this.jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 5000, redisPassword);
        logger.info("Redis Cache Service initialized with host: {}, port: {}", redisHost, redisPort);
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            logger.error("Error getting key {} from Redis: {}", key, e.getMessage());
            return null;
        }
    }

    public void set(String key, String value, int expirySeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expirySeconds, value);
            logger.info("Set key '{}' with value '{}' in Redis with expiry {} seconds.", key, value, expirySeconds);
        } catch (Exception e) {
            logger.error("Error setting key {} in Redis: {}", key, e.getMessage());
        }
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            logger.info("Redis Cache Service closed.");
        }
    }
}