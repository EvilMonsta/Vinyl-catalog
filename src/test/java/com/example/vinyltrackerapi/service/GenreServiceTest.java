package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.repositories.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreServiceTest {

    private GenreRepository genreRepository;
    private GenreService genreService;

    @BeforeEach
    void setUp() {
        genreRepository = mock(GenreRepository.class);
        genreService = new GenreService(genreRepository);
    }

    @Test
    void loadGenres_shouldCacheGenres() {
        Genre genre1 = new Genre(1, "Rock");
        Genre genre2 = new Genre(2, "Jazz");

        when(genreRepository.findAll()).thenReturn(List.of(genre1, genre2));

        genreService.loadGenres();

        Genre result1 = genreService.getGenreById(1);
        Genre result2 = genreService.getGenreById(2);

        assertThat(result1).isEqualTo(genre1);
        assertThat(result2).isEqualTo(genre2);
    }

    @Test
    void getGenreById_shouldThrowIfNotFound() {
        when(genreRepository.findAll()).thenReturn(List.of());

        genreService.loadGenres();

        assertThatThrownBy(() -> genreService.getGenreById(42))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Жанр не найден");
    }
}
