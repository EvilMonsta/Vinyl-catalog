package com.example.vinyltrackerapi.service;

import lombok.Getter;

@Getter
public class CacheEntry<T> {
    private final T value;
    private long timestamp;

    public CacheEntry(T value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public void refresh() {
        this.timestamp = System.currentTimeMillis();
    }
}