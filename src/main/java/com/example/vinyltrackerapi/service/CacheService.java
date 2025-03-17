package com.example.vinyltrackerapi.service;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class CacheService<T> {
    private final Map<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRATION_TIME_MS = 5 * 60 * 1000; // 5 минут
    private static final int MAX_CACHE_SIZE = 100; // Ограничение кеша

    public CacheService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanUp, 5, 5, TimeUnit.MINUTES);
    }

    public void put(String key, T value) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            removeOldestEntry();
        }
        cache.put(key, new CacheEntry<>(value));
        System.out.println("[CACHE] Добавлено: " + key);
    }

    public T get(String key) {
        CacheEntry<T> entry = cache.get(key);
        if (entry != null) {
            entry.refresh();
            System.out.println("[CACHE] Найдено в кеше: " + key);
            return entry.getValue();
        }
        return null;
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    private void cleanUp() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now - entry.getValue().getTimestamp() > CACHE_EXPIRATION_TIME_MS);
        System.out.println("[CACHE] Очистка старых записей...");
    }

    private void removeOldestEntry() {
        cache.entrySet().stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().getTimestamp()))
                .ifPresent(entry -> cache.remove(entry.getKey()));
    }

    public void remove(String key) {
        cache.remove(key);
        System.out.println("[CACHE] Удален ключ: " + key);
    }
}