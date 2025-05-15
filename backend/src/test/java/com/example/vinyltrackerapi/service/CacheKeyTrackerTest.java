package com.example.vinyltrackerapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyTrackerTest {

    private CacheKeyTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new CacheKeyTracker();
    }

    @Test
    void addVinylCacheKey_shouldAddNewKey() {
        tracker.addVinylCacheKey(1, "search-vinyl-rock");

        Set<String> keys = tracker.getVinylCacheKeys(1);
        assertThat(keys).containsExactly("search-vinyl-rock");
    }

    @Test
    void addVinylCacheKey_shouldAddMultipleKeys() {
        tracker.addVinylCacheKey(1, "key1");
        tracker.addVinylCacheKey(1, "key2");

        Set<String> keys = tracker.getVinylCacheKeys(1);
        assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
    }

    @Test
    void getVinylCacheKeys_shouldReturnEmptyIfNoneExist() {
        Set<String> keys = tracker.getVinylCacheKeys(99);
        assertThat(keys).isEmpty();
    }

    @Test
    void removeVinylCacheKeys_shouldRemoveAllKeysForVinyl() {
        tracker.addVinylCacheKey(1, "key1");
        tracker.removeVinylCacheKeys(1);

        assertThat(tracker.getVinylCacheKeys(1)).isEmpty();
    }
}
