package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.VinylStatus;
import com.example.vinyltrackerapi.api.repositories.VinylStatusRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VinylStatusService {
    private final VinylStatusRepository vinylStatusRepository;
    private final Map<Integer, VinylStatus> statusCache = new HashMap<>();

    public VinylStatusService(VinylStatusRepository vinylStatusRepository) {
        this.vinylStatusRepository = vinylStatusRepository;
    }

    @PostConstruct
    public void init() {
        List<VinylStatus> allStatuses = vinylStatusRepository.findAll();
        allStatuses.forEach(status -> statusCache.put(status.getId(), status));
    }

    public Optional<VinylStatus> getById(Integer id) {
        return Optional.ofNullable(statusCache.get(id));
    }

    public List<VinylStatus> getAll() {
        return new ArrayList<>(statusCache.values());
    }

    public void refreshCache() {
        statusCache.clear();
        vinylStatusRepository.findAll()
                .forEach(status -> statusCache.put(status.getId(), status));
    }
}