package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.VinylStatus;
import com.example.vinyltrackerapi.api.repositories.VinylStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VinylStatusServiceTest {

    private VinylStatusRepository vinylStatusRepository;
    private VinylStatusService vinylStatusService;

    @BeforeEach
    void setUp() {
        vinylStatusRepository = mock(VinylStatusRepository.class);
        vinylStatusService = new VinylStatusService(vinylStatusRepository);
    }

    @Test
    void init_shouldLoadStatusesIntoCache() {
        VinylStatus status1 = new VinylStatus(1, "Owned");
        VinylStatus status2 = new VinylStatus(2, "Wishlist");

        when(vinylStatusRepository.findAll()).thenReturn(List.of(status1, status2));

        vinylStatusService.init();

        Optional<VinylStatus> fromCache1 = vinylStatusService.getById(1);
        Optional<VinylStatus> fromCache2 = vinylStatusService.getById(2);

        assertThat(fromCache1).isPresent().contains(status1);
        assertThat(fromCache2).isPresent().contains(status2);
    }

    @Test
    void getById_shouldReturnStatusIfPresent() {
        VinylStatus status = new VinylStatus(3, "Listened");

        when(vinylStatusRepository.findAll()).thenReturn(List.of(status));

        vinylStatusService.init();

        Optional<VinylStatus> result = vinylStatusService.getById(3);
        assertThat(result).isPresent().contains(status);
    }

    @Test
    void getById_shouldReturnEmptyIfNotFound() {
        when(vinylStatusRepository.findAll()).thenReturn(List.of());

        vinylStatusService.init();

        Optional<VinylStatus> result = vinylStatusService.getById(99);
        assertThat(result).isNotPresent();
    }

    @Test
    void refreshCache_shouldClearAndReloadCache() {
        VinylStatus statusOld = new VinylStatus(1, "Old");
        VinylStatus statusNew = new VinylStatus(2, "New");

        when(vinylStatusRepository.findAll()).thenReturn(List.of(statusOld));
        vinylStatusService.init();

        when(vinylStatusRepository.findAll()).thenReturn(List.of(statusNew));
        vinylStatusService.refreshCache();

        assertThat(vinylStatusService.getById(1)).isEmpty();
        assertThat(vinylStatusService.getById(2)).contains(statusNew);
    }
}
