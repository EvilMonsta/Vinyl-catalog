package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.VinylStatus;
import com.example.vinyltrackerapi.api.repositories.VinylStatusRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VinylStatusService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VinylStatusService.class);
    private final VinylStatusRepository vinylStatusRepository;
    private final Map<Integer, VinylStatus> statusCache = new HashMap<>();

    public VinylStatusService(VinylStatusRepository vinylStatusRepository) {
        this.vinylStatusRepository = vinylStatusRepository;
    }

    @PostConstruct
    public void init() {
        List<VinylStatus> allStatuses = vinylStatusRepository.findAll();
        allStatuses.forEach(status -> statusCache.put(status.getId(), status));
        LOGGER.info("[VINYL-STATUS] Все жанры загружены в кэш");
    }

    public Optional<VinylStatus> getById(Integer id) {
        LOGGER.info("[VINYL-STATUS] Статус получен");
        return Optional.ofNullable(statusCache.get(id));
    }

    public void refreshCache() {
        statusCache.clear();
        vinylStatusRepository.findAll()
                .forEach(status -> statusCache.put(status.getId(), status));
    }
}