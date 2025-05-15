package com.example.vinyltrackerapi.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CacheEntryTest {

    @Test
    void constructor_shouldStoreValueAndTimestamp() {
        String value = "cachedData";
        CacheEntry<String> entry = new CacheEntry<>(value);

        assertThat(entry.getValue()).isEqualTo(value);
        assertThat(entry.getTimestamp()).isGreaterThan(0L);
    }

    @Test
    void refresh_shouldUpdateTimestamp() {
        CacheEntry<String> entry = new CacheEntry<>("test");
        long originalTimestamp = entry.getTimestamp();

        while (System.currentTimeMillis() <= originalTimestamp) {
            Thread.onSpinWait();
        }

        entry.refresh();

        assertThat(entry.getTimestamp()).isGreaterThan(originalTimestamp);
    }
}
