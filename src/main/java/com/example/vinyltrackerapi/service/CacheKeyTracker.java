package com.example.vinyltrackerapi.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CacheKeyTracker {
    private final Map<Integer, Set<String>> vinylCacheKeys = new HashMap<>();

    public void addVinylCacheKey(Integer vinylId, String cacheKey) {
        vinylCacheKeys.computeIfAbsent(vinylId, k -> new HashSet<>()).add(cacheKey);
    }

    public Set<String> getVinylCacheKeys(Integer vinylId) {
        return vinylCacheKeys.getOrDefault(vinylId, Collections.emptySet());
    }

    public void removeVinylCacheKeys(Integer vinylId) {
        vinylCacheKeys.remove(vinylId);
    }
}