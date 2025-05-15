package com.example.vinyltrackerapi.service;

import jakarta.annotation.PreDestroy;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CacheService<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);
    private final Map<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRATION_TIME_MS = 30L * 60 * 1000;
    private static final int MAX_CACHE_SIZE = 100;
    final ScheduledExecutorService scheduler;

    public CacheService() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanUp, 5, 5, TimeUnit.MINUTES);
    }

    public void put(String key, T value) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            removeOldestEntry();
        }
        cache.put(key, new CacheEntry<>(value));
        LOGGER.info("[CACHE] Добавлено: {}", key);
    }

    public T get(String key) {
        CacheEntry<T> entry = cache.get(key);
        if (entry != null) {
            entry.refresh();
            LOGGER.info("[CACHE] Найдено в кеше: {}", key);
            return entry.getValue();
        }
        return null;
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public void cleanUp() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now - entry.getValue().getTimestamp() > CACHE_EXPIRATION_TIME_MS);
        LOGGER.info("[CACHE] Очистка старых записей...");
    }

    private void removeOldestEntry() {
        cache.entrySet().stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().getTimestamp()))
                .ifPresent(entry -> {
                    cache.remove(entry.getKey());
                    LOGGER.info("[CACHE] Удалена самая старая запись: {}", entry.getKey());
                });
    }

    public void remove(String key) {
        cache.remove(key);
        LOGGER.info("[CACHE] Удален ключ: {}", key);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            LOGGER.info("[CACHE] Планировщик успешно завершён.");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.warn("[CACHE] Прерывание при завершении планировщика.");
        }
    }
}