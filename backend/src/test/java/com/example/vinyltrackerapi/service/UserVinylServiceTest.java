package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.models.Role;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.UserVinylId;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import com.example.vinyltrackerapi.api.repositories.UserVinylRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserVinylServiceTest {

    @Mock
    private UserVinylRepository userVinylRepository;
    @Mock
    private CacheService<List<UserVinyl>> userVinylCache;
    @Mock
    private CacheService<List<UserVinyl>> vinylUserCache;

    private AutoCloseable closeable;

    @InjectMocks
    private UserVinylService userVinylService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        userVinylService = new UserVinylService(userVinylRepository, userVinylCache, vinylUserCache);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getAllUserVinyls_shouldReturnAllMappedToDto() {
        Role role = new Role();
        role.setId(99);

        User user = new User();
        user.setId(1);
        user.setRole(role);

        Vinyl vinyl = new Vinyl();
        vinyl.setId(2);

        VinylStatus status = new VinylStatus();
        status.setId(3);

        UserVinyl entity = new UserVinyl(user, vinyl, status);

        when(userVinylRepository.findAll()).thenReturn(List.of(entity));

        List<UserVinylDto> result = userVinylService.getAllUserVinyls();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(UserVinylDto.class);
        verify(userVinylRepository).findAll();
    }


    @Test
    void addVinylToUser_shouldSaveAndReturnDto() {
        User user = new User();
        user.setId(1);
        Role role = new Role();
        role.setId(10);
        user.setRole(role);

        Vinyl vinyl = new Vinyl();
        vinyl.setId(2);
        VinylStatus status = new VinylStatus();
        status.setId(3);

        UserVinyl userVinyl = new UserVinyl(user, vinyl, status);
        when(userVinylRepository.save(any(UserVinyl.class))).thenReturn(userVinyl);

        UserVinylDto dto = userVinylService.addVinylToUser(user, vinyl, status);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserId()).isEqualTo(1);
        assertThat(dto.getVinylId()).isEqualTo(2);
        assertThat(dto.getStatusId()).isEqualTo(3);
    }

    @Test
    void findUserVinyl_shouldReturnResult() {
        Integer userId = 1, vinylId = 2;
        UserVinyl uv = new UserVinyl();
        when(userVinylRepository.findById(new UserVinylId(userId, vinylId)))
                .thenReturn(Optional.of(uv));

        Optional<UserVinyl> result = userVinylService.findUserVinyl(userId, vinylId);

        assertThat(result).isPresent();
    }

    @Test
    void removeVinylFromUser_shouldCallRepositoryAndUpdateCache() {
        userVinylService.removeVinylFromUser(1, 2);
        verify(userVinylRepository).deleteById(new UserVinylId(1, 2));
    }

    @Test
    void getUserVinyls_shouldFetchAndCacheResult() {
        UserVinyl mockVinyl = new UserVinyl();
        List<UserVinyl> dbResult = List.of(mockVinyl);

        when(userVinylRepository.findAllWithVinylAndStatusByUserId(1)).thenReturn(dbResult);

        List<UserVinyl> result = userVinylService.getUserVinyls(1);

        assertThat(result).isEqualTo(dbResult);
        verify(userVinylRepository).findAllWithVinylAndStatusByUserId(1);
        verify(userVinylCache).put("user-vinyls-1", dbResult);
    }

    @Test
    void getUsersByVinyl_shouldReturnFromCacheIfPresent() {
        List<UserVinyl> cached = List.of(new UserVinyl());
        when(vinylUserCache.contains("vinyl-users-1")).thenReturn(true);
        when(vinylUserCache.get("vinyl-users-1")).thenReturn(cached);

        List<UserVinyl> result = userVinylService.getUsersByVinyl(1);

        assertThat(result).isEqualTo(cached);
        verify(vinylUserCache).get("vinyl-users-1");
        verifyNoInteractions(userVinylRepository);
    }

    @Test
    void getUsersByVinyl_shouldFetchAndCacheIfNotPresent() {
        List<UserVinyl> fromDb = List.of(new UserVinyl());
        when(vinylUserCache.contains("vinyl-users-2")).thenReturn(false);
        when(userVinylRepository.findByVinylId(2)).thenReturn(fromDb);

        List<UserVinyl> result = userVinylService.getUsersByVinyl(2);

        assertThat(result).isEqualTo(fromDb);
        verify(userVinylRepository).findByVinylId(2);
        verify(vinylUserCache).put("vinyl-users-2", fromDb);
    }

    @Test
    void getUsersByVinyl_shouldReturnCachedIfPresent() {
        List<UserVinyl> cached = List.of(new UserVinyl());
        when(vinylUserCache.contains("vinyl-users-2")).thenReturn(true);
        when(vinylUserCache.get("vinyl-users-2")).thenReturn(cached);

        List<UserVinyl> result = userVinylService.getUsersByVinyl(2);
        assertThat(result).isEqualTo(cached);
    }

    @Test
    void updateVinylStatus_shouldUpdateStatusAndReturnUpdated() {
        User user = new User(); user.setId(1);
        Vinyl vinyl = new Vinyl(); vinyl.setId(2);
        VinylStatus status = new VinylStatus(); status.setId(3);
        UserVinyl uv = new UserVinyl(user, vinyl, new VinylStatus());

        when(userVinylRepository.findById(new UserVinylId(1, 2)))
                .thenReturn(Optional.of(uv));
        when(userVinylRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserVinyl result = userVinylService.updateVinylStatus(user, vinyl, status);
        assertThat(result.getStatus()).isEqualTo(status);
    }

    @Test
    void removeAllByUser_shouldCallRepoAndCacheRemove() {
        userVinylService.removeAllByUser(1);
        verify(userVinylRepository).deleteAllByUserId(1);
        verify(userVinylCache).remove("user-vinyls-1");
    }

    @Test
    void removeAllByVinyl_shouldCallRepoAndCacheRemove() {
        userVinylService.removeAllByVinyl(2);
        verify(userVinylRepository).deleteAllByVinylId(2);
        verify(vinylUserCache).remove("vinyl-users-2");
    }
}
