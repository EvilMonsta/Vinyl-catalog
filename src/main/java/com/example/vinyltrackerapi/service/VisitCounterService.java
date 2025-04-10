package com.example.vinyltrackerapi.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final Map<String, AtomicInteger> urlCounters = new ConcurrentHashMap<>();

    public void increment(String uri) {
        urlCounters.computeIfAbsent(uri, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getCount(String uri) {
        return urlCounters.getOrDefault(uri, new AtomicInteger(0)).get();
    }

    public Map<String, Integer> getAllCounts() {
        return urlCounters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    public void reset() {
        urlCounters.clear();
    }
}