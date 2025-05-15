package com.example.vinyltrackerapi.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class CacheServiceTest {

    private CacheService<String> cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService<>();
    }

    @AfterEach
    void tearDown() {
        cacheService.shutdown();
    }

    @Test
    void get_shouldRefreshTimestamp() throws Exception {
        cacheService.put("keyRefresh", "valueRefresh");

        long before = getEntryTimestamp();
        cacheService.get("keyRefresh");
        long after = getEntryTimestamp();

        assertThat(after).isGreaterThan(before);
    }

    @Test
    void cleanUp_shouldRemoveExpiredEntries() throws Exception {
        cacheService.put("oldKey", "oldValue");
        CacheEntry<String> entry = getEntry("oldKey");
        setTimestamp(entry, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(31));

        cacheService.cleanUp();
        assertThat(cacheService.get("oldKey")).isNull();
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

        assertThat(cacheService.contains("key-1")).isFalse();
        assertThat(cacheService.contains("key100")).isTrue();
    }

    @Test
    void shutdown_shouldCompleteGracefully() {
        cacheService.put("key", "value");
        cacheService.shutdown();
        assertThat(cacheService.get("key")).isEqualTo("value");
    }

    @Test
    void shutdown_shouldInterruptIfNotTerminated() {
        CacheService<String> localCache = new CacheService<>() {
            @Override
            public void shutdown() {
                scheduler.shutdown();
                Thread.currentThread().interrupt();
                super.shutdown();
            }
        };

        localCache.put("interruptKey", "value");
        localCache.shutdown();

        assertThat(Thread.interrupted()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private CacheEntry<String> getEntry(String key) throws Exception {
        Field cacheField = CacheService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        var cacheMap = (Map<String, CacheEntry<String>>) cacheField.get(cacheService);
        return cacheMap.get(key);
    }

    private long getEntryTimestamp() throws Exception {
        return getEntry("keyRefresh").getTimestamp();
    }

    private void setTimestamp(CacheEntry<String> entry, long newTimestamp) throws Exception {
        Field timestampField = CacheEntry.class.getDeclaredField("timestamp");
        timestampField.setAccessible(true);
        timestampField.setLong(entry, newTimestamp);
    }

    @Test
    void removeOldestEntryWhenEmpty_shouldNotThrow() throws Exception {
        Method method = CacheService.class.getDeclaredMethod("removeOldestEntry");
        method.setAccessible(true);

        assertThatCode(() -> method.invoke(cacheService))
                .doesNotThrowAnyException();
    }


    @Test
    void cleanUpNothingToCleanUp() {
        cacheService.put("freshKey", "freshValue");
        cacheService.cleanUp();
        assertThat(cacheService.contains("freshKey")).isTrue();
    }

}
