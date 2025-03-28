package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.repositories.GenreRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GenreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenreService.class);
    private final GenreRepository genreRepository;
    private final Map<Integer, Genre> genreCache = new HashMap<>();

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @PostConstruct
    public void loadGenres() {
        LOGGER.info("[GENRE] Все жанры загружены в кэш");
        genreRepository.findAll().forEach(g -> genreCache.put(g.getId(), g));
    }

    public Genre getGenreById(Integer id) {
        Genre genre = genreCache.get(id);
        if (genre == null) {
            LOGGER.warn("[GENRE] жанр с ID={} не найден!", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Жанр не найден: " + id);
        }
        return genre;
    }

    public List<Genre> getAllGenres() {
        LOGGER.info("[GENRE] Получены все жанры!");
        return new ArrayList<>(genreCache.values());
    }
}