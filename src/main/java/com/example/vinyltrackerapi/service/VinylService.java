package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.GenreRepository;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import com.example.vinyltrackerapi.api.specifications.VinylSpecification;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VinylService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VinylService.class);
    private final VinylRepository vinylRepository;
    private final GenreRepository genreRepository;
    private final UserService userService;
    private final GenreService genreService;
    private final CacheService<Vinyl> vinylCache;
    private final CacheService<List<Vinyl>> vinylListCache;
    private final CacheKeyTracker vinylKeyTracker;
    private static final String KEY_ALL = "all-vinyls";
    private static final String KEY_ID = "vinyl-";

    public VinylService(VinylRepository vinylRepository,
                        GenreRepository genreRepository,
                        UserService userService,
                        GenreService genreService,
                        CacheService<Vinyl> vinylCache, CacheService<List<Vinyl>> vinylListCache,
                        CacheKeyTracker vinylKeyTracker) {
        this.vinylRepository = vinylRepository;
        this.genreRepository = genreRepository;
        this.userService = userService;
        this.genreService = genreService;
        this.vinylCache = vinylCache;
        this.vinylListCache = vinylListCache;
        this.vinylKeyTracker = vinylKeyTracker;
    }

    public List<Vinyl> getVinylsByUploaderUsername(String username) {
        List<Vinyl> vinyls = vinylRepository.findVinylsByUploaderUsername(username);

        if (vinyls.isEmpty()) {
            LOGGER.warn("[VINYL] Не найдено ни одной пластинки, загруженной данным пользователем");
        } else {
            LOGGER.info("[VINYL] Найдено {} пластинок, загруженных данным пользователем", vinyls.size());
        }

        return vinyls;
    }

    public List<Vinyl> getAllVinyls() {
        String cacheKey = KEY_ALL;

        if (vinylListCache.contains(cacheKey)) {
            return vinylListCache.get(cacheKey);
        }

        List<Vinyl> vinyls = vinylRepository.findAll();
        vinylListCache.put(cacheKey, vinyls);
        LOGGER.info("[VINYL] Получены все пластинки");
        return vinyls;
    }

    public Vinyl getVinyl(Integer id) {
        String cacheKey = KEY_ID + id;

        if (vinylCache.contains(cacheKey)) {
            return vinylCache.get(cacheKey);
        }

        return vinylRepository.findById(id)
                .map(vinyl -> {
                    vinylCache.put(cacheKey, vinyl);
                    LOGGER.info("[VINYL] Пластинка найдена и добавлена в кэш: ID={}", id);
                    return vinyl;
                })
                .orElseThrow(() -> {
                    LOGGER.warn("[VINYL] Пластинка с ID={} не найдена!", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Пластинка с ID " + id + " не найдена!");
                });
    }

    public List<VinylDto> searchVinylsGlobal(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (normalizedQuery.isEmpty()) return List.of();

        String cacheKey = "search-vinyl-text-" + normalizedQuery;
        if (vinylListCache.contains(cacheKey)) {
            return vinylListCache.get(cacheKey).stream().map(VinylDto::new).toList();
        }

        Specification<Vinyl> spec = Specification.where(null);

        try {
            Integer year = Integer.parseInt(normalizedQuery);
            spec = spec.or(VinylSpecification.hasReleaseYear(year));
        } catch (NumberFormatException e) {
            // не число, значит не год
        }

        List<Genre> matchedGenres = genreRepository.findByNameContainingIgnoreCase(normalizedQuery);
        if (!matchedGenres.isEmpty()) {
            Specification<Vinyl> genreSpec = Specification.where(null);
            for (Genre genre : matchedGenres) {
                genreSpec = genreSpec.or(VinylSpecification.hasGenreId(genre.getId()));
            }
            spec = spec.or(genreSpec);
        }

        spec = spec.or(VinylSpecification.hasTitleLike(normalizedQuery))
                .or(VinylSpecification.hasArtistLike(normalizedQuery));

        List<Vinyl> result = vinylRepository.findAll(spec);

        for (Vinyl vinyl : result) {
            vinylKeyTracker.addVinylCacheKey(vinyl.getId(), cacheKey);
        }

        vinylListCache.put(cacheKey, result);

        LOGGER.info("[VINYL] Выполнен универсальный поиск по строке: {}", query);

        return result.stream().map(VinylDto::new).toList();
    }

    public List<VinylDto> searchVinyls(String title, String artist, Integer releaseYear, Integer genreId) {
        String cacheKey = "search-vinyl-" + title + "-" + artist + "-" + releaseYear + "-" + genreId;

        if (vinylListCache.contains(cacheKey)) {
            return vinylListCache.get(cacheKey).stream().map(VinylDto::new).toList();
        }

        Specification<Vinyl> spec = Specification.where(null);

        if (title != null && !title.isBlank()) {
            spec = spec.and(VinylSpecification.hasTitle(title));
        }
        if (artist != null && !artist.isBlank()) {
            spec = spec.and(VinylSpecification.hasArtist(artist));
        }
        if (releaseYear != null) {
            spec = spec.and(VinylSpecification.hasReleaseYear(releaseYear));
        }
        if (genreId != null) {
            spec = spec.and(VinylSpecification.hasGenreId(genreId));
        }

        List<Vinyl> result = vinylRepository.findAll(spec);

        for (Vinyl vinyl : result) {
            vinylKeyTracker.addVinylCacheKey(vinyl.getId(), cacheKey);
        }

        vinylListCache.put(cacheKey, result);

        LOGGER.info("[VINYL] Получена пластинка по параметрам");

        return result.stream().map(VinylDto::new).toList();
    }

    public Vinyl createVinyl(VinylDto vinylDto) {
        Genre genre = genreService.getGenreById(vinylDto.getGenreId());
        User addedBy;

        if (vinylDto.getAddedById() != null) {
            addedBy = userService.getUser(vinylDto.getAddedById());
        } else {
            addedBy = null;
        }

        Vinyl vinyl = vinylDto.toEntity(genre, addedBy);
        Vinyl savedVinyl = vinylRepository.save(vinyl);

        vinylCache.put(KEY_ID + savedVinyl.getId(), savedVinyl);
        vinylListCache.put(KEY_ALL, vinylRepository.findAll());
        LOGGER.info("[VINYL] Создана пластинка с ID={} title={}", savedVinyl.getId(), vinyl.getTitle());

        return savedVinyl;
    }

    public Vinyl updateVinyl(Integer id, VinylDto vinylDto) {
        Genre genre = genreService.getGenreById(vinylDto.getGenreId());
        User addedBy;

        if (vinylDto.getAddedById() != null) {
            addedBy = userService.getUser(vinylDto.getAddedById());
        } else {
            addedBy = null;
        }

        return vinylRepository.findById(id).map(vinyl -> {
            vinyl.setTitle(vinylDto.getTitle());
            vinyl.setArtist(vinylDto.getArtist());
            vinyl.setGenre(genre);
            vinyl.setReleaseYear(vinylDto.getReleaseYear());
            vinyl.setDescription(vinylDto.getDescription());
            vinyl.setCoverUrl(vinylDto.getCoverUrl());
            vinyl.setAddedBy(addedBy);

            Vinyl updatedVinyl = vinylRepository.save(vinyl);

            vinylCache.put(KEY_ID + id, updatedVinyl);

            Set<String> affectedCacheKeys = vinylKeyTracker.getVinylCacheKeys(id);
            for (String key : affectedCacheKeys) {
                vinylListCache.remove(key);
            }
            vinylKeyTracker.removeVinylCacheKeys(id);

            vinylListCache.put(KEY_ALL, vinylRepository.findAll());

            LOGGER.info("[VINYL] Обновлена пластинка с ID={} title={}", updatedVinyl.getId(),
                    updatedVinyl.getTitle());
            return updatedVinyl;
        }).orElseThrow(() -> {
            LOGGER.warn("[USER] Пользователь не найден!");
            return new RuntimeException("Винил не найден!");
        });
    }

    public void deleteVinyl(Integer id) {
        if (!vinylRepository.existsById(id)) {
            LOGGER.warn("[VINYL] Пластинки с ID={} не существует!", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Винил с ID " + id + " не найден!");
        }
        vinylRepository.deleteById(id);
        vinylCache.remove(KEY_ID + id);
        Set<String> affectedCacheKeys = vinylKeyTracker.getVinylCacheKeys(id);
        for (String key : affectedCacheKeys) {
            vinylListCache.remove(key);
        }
        vinylKeyTracker.removeVinylCacheKeys(id);
        vinylListCache.put(KEY_ALL, vinylRepository.findAll());
        LOGGER.info("[VINYL] Пластинка с ID={} удалена!", id);
    }

    public void detachUserFromVinyl(User user) {
        List<Vinyl> vinylsAddedByUser = vinylRepository.findByAddedBy(user);
        vinylsAddedByUser.forEach(vinyl -> vinyl.setAddedBy(null));
        vinylRepository.saveAll(vinylsAddedByUser);
    }

    public List<Vinyl> createVinylsBulk(List<VinylDto> vinylDtos) {
        List<Vinyl> vinyls = vinylDtos.stream()
                .map(dto -> {
                    Genre genre = genreService.getGenreById(dto.getGenreId());
                    User addedBy = dto.getAddedById() !=
                            null ? userService.getUser(dto.getAddedById()) : null;
                    return dto.toEntity(genre, addedBy);
                })
                .toList();

        List<Vinyl> savedVinyls = vinylRepository.saveAll(vinyls);

        savedVinyls.forEach(vinyl -> {
            String cacheKey = KEY_ID + vinyl.getId();
            vinylCache.put(cacheKey, vinyl);
        });

        vinylListCache.put(KEY_ALL, vinylRepository.findAll());

        LOGGER.info("[VINYL] Загружено {} новых пластинок", savedVinyls.size());
        return savedVinyls;
    }

    public List<VinylDto> getRandomVinyls(int limit) {
        List<Vinyl> vinyls = vinylRepository.findRandomVinyls(limit);
        LOGGER.info("[VINYL] Получены {} рандоных пластинок", limit);
        return vinyls.stream().map(VinylDto::new).toList();
    }

    public List<VinylDto> getRandomVinylsByYear(int year, int limit) {
        List<Vinyl> vinyls = vinylRepository.findRandomVinylsByYear(year, limit);
        LOGGER.info("[VINYL] Получены {} рандоных пластинок {} года", limit, year);
        return vinyls.stream().map(VinylDto::new).toList();
    }

    public Page<VinylDto> getVinylsPage(Pageable pageable) {
        Page<Vinyl> vinylPage = vinylRepository.findAll(pageable);
        LOGGER.info("[VINYL] Получена страница винилов: {}/{}", pageable.getPageNumber() + 1,
                vinylPage.getTotalPages());
        return vinylPage.map(VinylDto::new);
    }
}
