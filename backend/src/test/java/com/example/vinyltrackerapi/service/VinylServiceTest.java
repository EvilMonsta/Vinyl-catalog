package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.GenreRepository;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private final Vinyl vinyl = new Vinyl();
    private final Genre genre = new Genre();
    private final User user = new User();

    private AutoCloseable closeable;

    @BeforeEach
    void init() {
        vinyl.setId(1);
        vinyl.setTitle("Test");
        genre.setId(3);
        user.setId(5);
        closeable = MockitoAnnotations.openMocks(this);
        vinylService = new VinylService(vinylRepository, genreRepository, userService, genreService, vinylCache, vinylListCache,vinylKeyTracker);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getVinylsByUploaderUsername_shouldReturnList() {
        List<Vinyl> vinyls = List.of(vinyl);
        when(vinylRepository.findVinylsByUploaderUsername("user")).thenReturn(vinyls);

        List<Vinyl> result = vinylService.getVinylsByUploaderUsername("user");

        assertThat(result).containsExactly(vinyl);
        verify(vinylRepository).findVinylsByUploaderUsername("user");
    }

    @Test
    void getVinylsByUploaderUsername_shouldLogWhenEmpty() {
        when(vinylRepository.findVinylsByUploaderUsername("empty")).thenReturn(List.of());

        List<Vinyl> result = vinylService.getVinylsByUploaderUsername("empty");

        assertThat(result).isEmpty();
        verify(vinylRepository).findVinylsByUploaderUsername("empty");
    }
    @Test
    void getAllVinyls_shouldReturnFromCache() {
        List<Vinyl> cached = List.of(vinyl);
        when(vinylListCache.contains("all-vinyls")).thenReturn(true);
        when(vinylListCache.get("all-vinyls")).thenReturn(cached);

        List<Vinyl> result = vinylService.getAllVinyls();

        assertThat(result).containsExactly(vinyl);
        verifyNoInteractions(vinylRepository);
    }

    @Test
    void getAllVinyls_shouldFetchAndCacheIfMissing() {
        List<Vinyl> all = List.of(vinyl);
        when(vinylListCache.contains("all-vinyls")).thenReturn(false);
        when(vinylRepository.findAll()).thenReturn(all);

        List<Vinyl> result = vinylService.getAllVinyls();

        assertThat(result).isEqualTo(all);
        verify(vinylListCache).put("all-vinyls", all);
    }
    @Test
    void getVinyl_shouldReturnFromCache() {
        when(vinylCache.contains("vinyl-1")).thenReturn(true);
        when(vinylCache.get("vinyl-1")).thenReturn(vinyl);

        Vinyl result = vinylService.getVinyl(1);
        assertThat(result).isEqualTo(vinyl);
    }

    @Test
    void getVinyl_shouldFetchAndCacheIfMissing() {
        when(vinylCache.contains("vinyl-1")).thenReturn(false);
        when(vinylRepository.findById(1)).thenReturn(Optional.of(vinyl));

        Vinyl result = vinylService.getVinyl(1);

        assertThat(result).isEqualTo(vinyl);
        verify(vinylCache).put("vinyl-1", vinyl);
    }

    @Test
    void getVinyl_shouldThrowIfNotFound() {
        when(vinylCache.contains("vinyl-1")).thenReturn(false);
        when(vinylRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vinylService.getVinyl(1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void searchVinylsGlobal_shouldReturnFromCacheIfExists() {
        String cacheKey = "search-vinyl-text-rock";
        when(vinylListCache.contains(cacheKey)).thenReturn(true);
        when(vinylListCache.get(cacheKey)).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinylsGlobal("rock");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchVinylsGlobal_shouldReturnEmptyListIfQueryIsNullOrBlank() {
        assertThat(vinylService.searchVinylsGlobal(null)).isEmpty();
        assertThat(vinylService.searchVinylsGlobal("   ")).isEmpty();
    }

    @Test
    void searchVinylsGlobal_shouldSearchWhenNotCached() {
        String query = "metal";
        when(genreRepository.findByNameContainingIgnoreCase("metal")).thenReturn(List.of());
        when(vinylRepository.findAll(any(Specification.class))).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinylsGlobal(query);

        assertThat(result).hasSize(1);
        verify(vinylKeyTracker).addVinylCacheKey(eq(vinyl.getId()), contains("metal"));
        verify(vinylListCache).put(contains("metal"), any());
    }

    @Test
    void searchVinylsGlobal_shouldApplyYearIfNumber() {
        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(genreRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of());
        when(vinylRepository.findAll(any(Specification.class))).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinylsGlobal("1990");

        assertThat(result).hasSize(1);
        verify(vinylListCache).put(contains("1990"), any());
    }

    @Test
    void searchVinylsGlobal_shouldApplyGenresIfMatched() {
        Genre g = new Genre(); g.setId(42);
        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(genreRepository.findByNameContainingIgnoreCase("rock")).thenReturn(List.of(g));
        when(vinylRepository.findAll(any(Specification.class))).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinylsGlobal("rock");

        assertThat(result).hasSize(1);
        verify(vinylKeyTracker).addVinylCacheKey(anyInt(), contains("rock"));
    }


    @Test
    void searchVinyls_shouldReturnFromCacheIfExists() {
        String key = "search-vinyl-1-2-3-4";
        when(vinylListCache.contains(key)).thenReturn(true);
        when(vinylListCache.get(key)).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinyls("1", "2", 3, 4);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchVinyls_shouldBuildSpecAndReturnIfNotCached() {
        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(vinylRepository.findAll(any(Specification.class))).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinyls("A", "B", 2000, 1);

        assertThat(result).hasSize(1);
        verify(vinylKeyTracker).addVinylCacheKey(eq(vinyl.getId()), contains("search-vinyl-"));
        verify(vinylListCache).put(anyString(), any());
    }

    @Test
    void searchVinyls_shouldBuildSpecFromAllParams() {
        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(vinylRepository.findAll(any(Specification.class))).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinyls("Title", "Artist", 1985, 7);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchVinyls_shouldIgnoreNullParams() {
        when(vinylListCache.contains(anyString())).thenReturn(false);
        when(vinylRepository.findAll(any(Specification.class))).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.searchVinyls(null, null, null, null);

        assertThat(result).hasSize(1);
    }


    @Test
    void createVinyl_shouldCreateAndCache() {
        VinylDto dto = mock(VinylDto.class);
        when(dto.getGenreId()).thenReturn(3);
        when(dto.getAddedById()).thenReturn(5);
        when(dto.toEntity(any(), any())).thenReturn(vinyl);
        when(genreService.getGenreById(3)).thenReturn(genre);
        when(userService.getUser(5)).thenReturn(user);
        when(vinylRepository.save(any())).thenReturn(vinyl);
        when(vinylRepository.findAll()).thenReturn(List.of(vinyl));

        Vinyl result = vinylService.createVinyl(dto);

        assertThat(result).isEqualTo(vinyl);
        verify(vinylCache).put("vinyl-1", vinyl);
        verify(vinylListCache).put("all-vinyls", List.of(vinyl));
    }

    @Test
    void updateVinyl_shouldUpdateAndInvalidateCache() {
        VinylDto dto = mock(VinylDto.class);
        when(dto.getTitle()).thenReturn("NewTitle");
        when(dto.getArtist()).thenReturn("NewArtist");
        when(dto.getDescription()).thenReturn("Desc");
        when(dto.getReleaseYear()).thenReturn(1990);
        when(dto.getCoverUrl()).thenReturn("url");
        when(dto.getGenreId()).thenReturn(3);
        when(dto.getAddedById()).thenReturn(5);
        when(vinylRepository.findById(1)).thenReturn(Optional.of(vinyl));
        when(genreService.getGenreById(3)).thenReturn(genre);
        when(userService.getUser(5)).thenReturn(user);
        when(vinylRepository.save(any())).thenReturn(vinyl);
        when(vinylKeyTracker.getVinylCacheKeys(1)).thenReturn(Set.of("some-key"));
        when(vinylRepository.findAll()).thenReturn(List.of());

        Vinyl result = vinylService.updateVinyl(1, dto);

        assertThat(result).isEqualTo(vinyl);
        verify(vinylCache).put("vinyl-1", vinyl);
        verify(vinylListCache).remove("some-key");
        verify(vinylKeyTracker).removeVinylCacheKeys(1);
        verify(vinylListCache).put("all-vinyls", List.of());
    }

    @Test
    void createVinyl_shouldHandleAddedByPresent() {
        VinylDto dto = mock(VinylDto.class);
        when(dto.getGenreId()).thenReturn(3);
        when(dto.getAddedById()).thenReturn(5);
        when(dto.toEntity(any(), any())).thenReturn(vinyl);

        when(genreService.getGenreById(3)).thenReturn(genre);
        when(userService.getUser(5)).thenReturn(user);
        when(vinylRepository.save(any())).thenReturn(vinyl);
        when(vinylRepository.findAll()).thenReturn(List.of());

        Vinyl result = vinylService.createVinyl(dto);

        assertThat(result).isEqualTo(vinyl);
        verify(userService).getUser(5);
    }

    @Test
    void createVinyl_shouldHandleAddedByNull() {
        VinylDto dto = mock(VinylDto.class);
        when(dto.getGenreId()).thenReturn(3);
        when(dto.getAddedById()).thenReturn(null);
        when(dto.toEntity(any(), eq(null))).thenReturn(vinyl);

        when(genreService.getGenreById(3)).thenReturn(genre);
        when(vinylRepository.save(any())).thenReturn(vinyl);
        when(vinylRepository.findAll()).thenReturn(List.of());

        Vinyl result = vinylService.createVinyl(dto);

        assertThat(result).isEqualTo(vinyl);
        verify(userService, never()).getUser(any());
    }

    @Test
    void updateVinyl_shouldThrowIfNotFound() {
        when(vinylRepository.findById(1)).thenReturn(Optional.empty());
        VinylDto dto = new VinylDto();

        assertThatThrownBy(() -> vinylService.updateVinyl(1, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Винил не найден");
    }

    @Test
    void deleteVinyl_shouldDeleteAndCleanCache() {
        when(vinylRepository.existsById(1)).thenReturn(true);
        when(vinylRepository.findAll()).thenReturn(List.of());
        when(vinylKeyTracker.getVinylCacheKeys(1)).thenReturn(Set.of("key1", "key2"));

        vinylService.deleteVinyl(1);

        verify(vinylRepository).deleteById(1);
        verify(vinylCache).remove("vinyl-1");
        verify(vinylListCache).remove("key1");
        verify(vinylListCache).remove("key2");
        verify(vinylKeyTracker).removeVinylCacheKeys(1);
        verify(vinylListCache).put("all-vinyls", List.of());
    }

    @Test
    void deleteVinyl_shouldThrowIfNotFound() {
        when(vinylRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> vinylService.deleteVinyl(1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void detachUserFromVinyl_shouldRemoveUserReference() {
        Vinyl v1 = new Vinyl(); v1.setAddedBy(user);
        Vinyl v2 = new Vinyl(); v2.setAddedBy(user);
        when(vinylRepository.findByAddedBy(user)).thenReturn(List.of(v1, v2));

        vinylService.detachUserFromVinyl(user);

        assertThat(v1.getAddedBy()).isNull();
        assertThat(v2.getAddedBy()).isNull();
        verify(vinylRepository).saveAll(List.of(v1, v2));
    }

    @Test
    void createVinylsBulk_shouldSaveAllAndCacheEach() {
        VinylDto dto = mock(VinylDto.class);
        when(dto.getGenreId()).thenReturn(3);
        when(dto.getAddedById()).thenReturn(5);
        when(dto.toEntity(any(), any())).thenReturn(vinyl);

        when(genreService.getGenreById(3)).thenReturn(genre);
        when(userService.getUser(5)).thenReturn(user);
        when(vinylRepository.saveAll(any())).thenReturn(List.of(vinyl));
        when(vinylRepository.findAll()).thenReturn(List.of(vinyl));

        List<Vinyl> result = vinylService.createVinylsBulk(List.of(dto));

        assertThat(result).containsExactly(vinyl);
        verify(vinylCache).put("vinyl-1", vinyl);
        verify(vinylListCache).put("all-vinyls", List.of(vinyl));
    }

    @Test
    void getRandomVinyls_shouldReturnList() {
        when(vinylRepository.findRandomVinyls(3)).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.getRandomVinyls(3);

        assertThat(result).hasSize(1);
    }

    @Test
    void getRandomVinylsByYear_shouldReturnList() {
        when(vinylRepository.findRandomVinylsByYear(1990, 2)).thenReturn(List.of(vinyl));

        List<VinylDto> result = vinylService.getRandomVinylsByYear(1990, 2);

        assertThat(result).hasSize(1);
    }

    @Test
    void getVinylsPage_shouldReturnMappedPage() {
        Page<Vinyl> page = mock(Page.class);
        Pageable pageable = mock(Pageable.class);

        when(vinylRepository.findAll(pageable)).thenReturn(page);
        when(page.map(any())).thenReturn(mock(Page.class));

        Page<VinylDto> result = vinylService.getVinylsPage(pageable);

        assertThat(result).isNotNull();
    }

}