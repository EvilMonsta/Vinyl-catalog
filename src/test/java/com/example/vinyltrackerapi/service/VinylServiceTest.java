package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.*;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VinylServiceTest {

    @InjectMocks
    private VinylService vinylService;

    @Mock private VinylRepository vinylRepository;
    @Mock private UserService userService;
    @Mock private UserVinylService userVinylService;
    @Mock private GenreService genreService;
    @Mock private CacheService<Vinyl> vinylCache;
    @Mock private CacheService<List<Vinyl>> vinylListCache;
    @Mock private CacheKeyTracker vinylKeyTracker;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        vinylService = new VinylService(
                vinylRepository, userService,
                genreService, vinylCache, vinylListCache, vinylKeyTracker
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void updateVinyl_shouldUpdateSuccessfully() {
        Integer vinylId = 1;
        Vinyl oldVinyl = new Vinyl();
        oldVinyl.setId(vinylId);

        VinylDto dto = new VinylDto();
        dto.setTitle("Updated");
        dto.setArtist("Artist");
        dto.setReleaseYear(2020);
        dto.setDescription("Updated desc");
        dto.setCoverUrl("url");
        dto.setGenreId(2);
        dto.setAddedById(3);

        Genre genre = new Genre();
        User addedBy = new User();

        when(vinylRepository.findById(vinylId)).thenReturn(Optional.of(oldVinyl));
        when(genreService.getGenreById(dto.getGenreId())).thenReturn(genre);
        when(userService.getUser(dto.getAddedById())).thenReturn(addedBy);
        when(vinylRepository.save(any(Vinyl.class))).thenAnswer(inv -> inv.getArgument(0));

        Vinyl updated = vinylService.updateVinyl(vinylId, dto);

        assertThat(updated.getTitle()).isEqualTo(dto.getTitle());
        assertThat(updated.getGenre()).isEqualTo(genre);
        assertThat(updated.getAddedBy()).isEqualTo(addedBy);
    }

    @Test
    void updateVinyl_shouldThrowIfNotFound() {
        Integer vinylId = 99;
        VinylDto dto = new VinylDto();
        dto.setGenreId(1);

        when(vinylRepository.findById(vinylId)).thenReturn(Optional.empty());
        when(genreService.getGenreById(dto.getGenreId())).thenReturn(new Genre());

        assertThatThrownBy(() -> vinylService.updateVinyl(vinylId, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Винил не найден!");
    }

    @Test
    void deleteVinyl_shouldDeleteSuccessfully() {
        Integer id = 1;

        when(vinylRepository.existsById(id)).thenReturn(true);
        doNothing().when(vinylRepository).deleteById(id);

        vinylService.deleteVinyl(id);

        verify(vinylRepository).deleteById(id);
    }

    @Test
    void deleteVinyl_shouldThrowIfNotExists() {
        Integer id = 100;

        when(vinylRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> vinylService.deleteVinyl(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void searchVinyls_shouldReturnMatchingList() {
        String title = "test";
        String artist = "artist";
        Integer year = 2020;
        String genre = "rock";

        Vinyl vinyl1 = new Vinyl();
        vinyl1.setTitle("test");

        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(vinylRepository.findAll(Mockito.<Specification<Vinyl>>any()))
                .thenReturn(List.of(vinyl1));

        List<VinylDto> result = vinylService.searchVinyls(title, artist, year, genre);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .allSatisfy(dto -> assertThat(dto.getTitle()).isEqualTo("test"));
    }

    @Test
    void testGetVinylFoundInCache() {
        Vinyl vinyl = new Vinyl();
        vinyl.setId(1);
        when(vinylCache.contains("vinyl-1")).thenReturn(true);
        when(vinylCache.get("vinyl-1")).thenReturn(vinyl);

        Vinyl result = vinylService.getVinyl(1);
        assertEquals(1, result.getId());
        verify(vinylCache, times(1)).get("vinyl-1");
        verifyNoInteractions(vinylRepository);
    }

    @Test
    void testGetVinylNotFound() {
        when(vinylCache.contains("vinyl-99")).thenReturn(false);
        when(vinylRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> vinylService.getVinyl(99));
    }

    @Test
    void testCreateVinyl() {
        Genre genre = new Genre(1, "Rock");
        User user = new User();
        user.setId(5);

        VinylDto dto = new VinylDto();
        dto.setTitle("New Vinyl");
        dto.setArtist("Artist");
        dto.setGenreId(1);
        dto.setReleaseYear(2024);
        dto.setAddedById(5);

        when(genreService.getGenreById(1)).thenReturn(genre);
        when(userService.getUser(5)).thenReturn(user);
        when(vinylRepository.save(any(Vinyl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vinyl result = vinylService.createVinyl(dto);
        assertEquals("New Vinyl", result.getTitle());
        assertEquals("Artist", result.getArtist());
        assertEquals(2024, result.getReleaseYear());
        assertEquals(genre, result.getGenre());
        assertEquals(user, result.getAddedBy());
    }

    // добавим ещё больше тестов по update, delete, search при необходимости
}