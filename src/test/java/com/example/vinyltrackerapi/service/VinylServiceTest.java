package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.*;
import com.example.vinyltrackerapi.api.repositories.GenreRepository;
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
    @Mock private GenreRepository genreRepository;
    @Mock private UserService userService;
    @Mock private GenreService genreService;
    @Mock private CacheService<Vinyl> vinylCache;
    @Mock private CacheService<List<Vinyl>> vinylListCache;
    @Mock private CacheKeyTracker vinylKeyTracker;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        vinylService = new VinylService(
                vinylRepository,genreRepository ,userService,
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
        Integer genreId = 1;

        Vinyl vinyl1 = new Vinyl();
        vinyl1.setTitle("test");

        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(vinylRepository.findAll(Mockito.<Specification<Vinyl>>any()))
                .thenReturn(List.of(vinyl1));

        List<VinylDto> result = vinylService.searchVinyls(title, artist, year, genreId);

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

    @Test
    void bulkCreateVinyls_shouldCreateMultipleVinyls() {
        Genre genre = new Genre(1, "Rock");
        User user = new User();
        user.setId(5);

        VinylDto dto1 = new VinylDto(15, "Title1", "Artist1", 1, 2000, "Desc1", "url1", 5);
        VinylDto dto2 = new VinylDto(16, "Title2", "Artist2", 1, 2001, "Desc2", "url2", 5);

        when(genreService.getGenreById(1)).thenReturn(genre);
        when(userService.getUser(5)).thenReturn(user);
        when(vinylRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Vinyl> result = vinylService.createVinylsBulk(List.of(dto1, dto2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Title1");
        assertThat(result.get(1).getTitle()).isEqualTo("Title2");
    }

    @Test
    void bulkCreateVinyls_shouldHandleEmptyList() {
        List<Vinyl> result = vinylService.createVinylsBulk(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    void getAllVinyls_shouldReturnCached() {
        List<Vinyl> vinyls = List.of(new Vinyl());
        when(vinylListCache.contains("all-vinyls")).thenReturn(true);
        when(vinylListCache.get("all-vinyls")).thenReturn(vinyls);

        List<Vinyl> result = vinylService.getAllVinyls();
        assertEquals(1, result.size());
    }

    @Test
    void getVinylsByUploaderUsername_shouldLogWarningIfEmpty() {
        when(vinylRepository.findVinylsByUploaderUsername("nope")).thenReturn(Collections.emptyList());
        List<Vinyl> result = vinylService.getVinylsByUploaderUsername("nope");
        assertThat(result).isEmpty();
    }

    @Test
    void detachUserFromVinyl_shouldSetAddedByNull() {
        User user = new User();
        Vinyl vinyl = new Vinyl();
        vinyl.setAddedBy(user);
        List<Vinyl> vinyls = List.of(vinyl);

        when(vinylRepository.findByAddedBy(user)).thenReturn(vinyls);

        vinylService.detachUserFromVinyl(user);

        assertThat(vinyl.getAddedBy()).isNull();
        verify(vinylRepository).saveAll(vinyls);
    }
}
