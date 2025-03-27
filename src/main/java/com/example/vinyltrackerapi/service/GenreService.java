package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.repositories.GenreRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GenreService {
    private final GenreRepository genreRepository;
    private final Map<Integer, Genre> genreCache = new HashMap<>();

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @PostConstruct
    public void loadGenres() {
        genreRepository.findAll().forEach(g -> genreCache.put(g.getId(), g));
    }

    public Genre getGenreById(Integer id) {
        Genre genre = genreCache.get(id);
        if (genre == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Жанр не найден: " + id);
        }
        return genre;
    }

    public List<Genre> getAllGenres() {
        return new ArrayList<>(genreCache.values());
    }
}