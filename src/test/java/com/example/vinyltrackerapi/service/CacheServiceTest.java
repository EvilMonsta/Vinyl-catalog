package com.example.vinyltrackerapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CacheServiceTest {

    private CacheService<String> cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService<>();
    }

    @Test
    void putAndGet_shouldStoreAndRetrieveValue() {
        cacheService.put("key1", "value1");
        assertThat(cacheService.get("key1")).isEqualTo("value1");
    }

    @Test
    void get_shouldReturnNullIfNotExists() {
        assertThat(cacheService.get("nonexistent")).isNull();
    }

    @Test
    void contains_shouldReturnTrueIfExists() {
        cacheService.put("key2", "value2");
        assertThat(cacheService.contains("key2")).isTrue();
        assertThat(cacheService.contains("key3")).isFalse();
    }

    @Test
    void remove_shouldDeleteEntry() {
        cacheService.put("key4", "value4");
        cacheService.remove("key4");
        assertThat(cacheService.get("key4")).isNull();
    }

    @Test
    void removeOldestEntry_shouldEvictFirstInserted() {
        for (int i = 0; i < 101; i++) {
            cacheService.put("key" + i, "value" + i);
        }

        assertThat(cacheService.contains("key0")).isFalse();
        assertThat(cacheService.contains("key100")).isTrue();
    }

    @Test
    void shutdown_shouldCompleteGracefully() {
        cacheService.put("key", "value");
        cacheService.shutdown();
        assertThat(cacheService.get("key")).isEqualTo("value");
    }
}
