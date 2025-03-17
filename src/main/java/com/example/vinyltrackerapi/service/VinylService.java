package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import com.example.vinyltrackerapi.api.specifications.VinylSpecification;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VinylService {
    private final VinylRepository vinylRepository;
    private final UserService userService;
    private final UserVinylService userVinylService;
    private final CacheService<Vinyl> vinylCache;
    private final CacheService<List<Vinyl>> vinylListCache;
    private final CacheKeyTracker vinylKeyTracker;
    private final String keyAll = "all-vinyls";
    private final String keyId = "vinyl-";

    public VinylService(VinylRepository vinylRepository, UserService userService,
                        @Lazy UserVinylService userVinylService,
                        CacheService<Vinyl> vinylCache, CacheService<List<Vinyl>> vinylListCache,
                        CacheKeyTracker vinylKeyTracker) {
        this.vinylRepository = vinylRepository;
        this.userService = userService;
        this.userVinylService = userVinylService;
        this.vinylCache = vinylCache;
        this.vinylListCache = vinylListCache;
        this.vinylKeyTracker = vinylKeyTracker;
    }

    public List<Vinyl> getAllVinyls() {
        String cacheKey = keyAll;

        if (vinylListCache.contains(cacheKey)) {
            return vinylListCache.get(cacheKey);
        }

        List<Vinyl> vinyls = vinylRepository.findAll();
        vinylListCache.put(cacheKey, vinyls);
        return vinyls;
    }

    public Optional<Vinyl> getVinyl(Integer id) {
        String cacheKey = keyId + id;

        if (vinylCache.contains(cacheKey)) {
            return Optional.of(vinylCache.get(cacheKey));
        }

        Optional<Vinyl> vinyl = vinylRepository.findById(id);

        vinyl.ifPresent(value -> vinylCache.put(cacheKey, value));

        return vinyl;
    }

    public List<VinylDto> searchVinyls(String title, String artist, Integer releaseYear, String genre) {
        String cacheKey = "search-vinyl-" + title + "-" + artist + "-" + releaseYear + "-" + genre;

        if (vinylListCache.contains(cacheKey)) {
            return vinylListCache.get(cacheKey).stream().map(VinylDto::new).toList();
        }

        Specification<Vinyl> spec = Specification
                .where(VinylSpecification.hasTitle(title))
                .and(VinylSpecification.hasArtist(artist))
                .and(VinylSpecification.hasReleaseYear(releaseYear))
                .and(VinylSpecification.hasGenre(genre));

        List<Vinyl> result = vinylRepository.findAll(spec);

        for (Vinyl vinyl : result) {
            vinylKeyTracker.addVinylCacheKey(vinyl.getId(), cacheKey);
        }

        vinylListCache.put(cacheKey, result);
        return result.stream().map(VinylDto::new).toList();
    }

    public Vinyl createVinyl(VinylDto vinylDto) {
        Vinyl vinyl = vinylDto.toEntity();

        if (vinylDto.getAddedById() != null) {
            User addedBy = userService.getUser(vinylDto.getAddedById())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Пользователь не найден!"));
            vinyl.setAddedBy(addedBy);
        }

        Vinyl savedVinyl = vinylRepository.save(vinyl);

        vinylCache.put(keyId + savedVinyl.getId(), savedVinyl);
        vinylListCache.put(keyAll, vinylRepository.findAll());

        return savedVinyl;
    }

    public Vinyl updateVinyl(Integer id, Vinyl newVinylData) {
        return vinylRepository.findById(id).map(vinyl -> {
            vinyl.setTitle(newVinylData.getTitle());
            vinyl.setArtist(newVinylData.getArtist());
            vinyl.setGenre(newVinylData.getGenre());
            vinyl.setReleaseYear(newVinylData.getReleaseYear());
            vinyl.setDescription(newVinylData.getDescription());
            vinyl.setCoverUrl(newVinylData.getCoverUrl());

            Vinyl updatedVinyl = vinylRepository.save(vinyl);

            vinylCache.put(keyId + id, updatedVinyl);

            Set<String> affectedCacheKeys = vinylKeyTracker.getVinylCacheKeys(id);
            for (String key : affectedCacheKeys) {
                vinylListCache.remove(key);
            }
            vinylKeyTracker.removeVinylCacheKeys(id);

            vinylListCache.put(keyAll, vinylRepository.findAll());
            return updatedVinyl;
        }).orElseThrow(() -> new RuntimeException("Винил не найден!"));
    }

    public void deleteVinyl(Integer id) {
        if (!vinylRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Винил с ID " + id + " не найден!");
        }
        userVinylService.handleVinylDeletion(id);
        vinylRepository.deleteById(id);
        vinylCache.remove(keyId + id);
        Set<String> affectedCacheKeys = vinylKeyTracker.getVinylCacheKeys(id);
        for (String key : affectedCacheKeys) {
            vinylListCache.remove(key);
        }
        vinylKeyTracker.removeVinylCacheKeys(id);
        vinylListCache.put(keyAll, vinylRepository.findAll());
    }

    public void detachUserFromVinyl(User user) {
        List<Vinyl> vinylsAddedByUser = vinylRepository.findByAddedBy(user);
        vinylsAddedByUser.forEach(vinyl -> vinyl.setAddedBy(null));
        vinylRepository.saveAll(vinylsAddedByUser);
    }
}
