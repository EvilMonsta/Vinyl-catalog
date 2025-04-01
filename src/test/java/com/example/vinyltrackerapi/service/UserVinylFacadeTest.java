package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserVinylFacadeTest {

    @InjectMocks
    private UserVinylFacade facade;

    @Mock private UserService userService;
    @Mock private VinylService vinylService;
    @Mock private UserVinylService userVinylService;
    @Mock private VinylStatusService vinylStatusService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        facade = new UserVinylFacade(userService, vinylService, userVinylService, vinylStatusService);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void addVinylToUser_shouldReturnDto() {
        User user = new User(); user.setId(1);
        Vinyl vinyl = new Vinyl(); vinyl.setId(2);
        VinylStatus status = new VinylStatus(); status.setId(3);
        UserVinylDto dto = new UserVinylDto();

        when(userService.getUser(1)).thenReturn(user);
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.of(status));
        when(userVinylService.addVinylToUser(user, vinyl, status)).thenReturn(dto);

        UserVinylDto result = facade.addVinylToUser(1, 2, 3);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void addVinylToUser_shouldThrowWhenStatusNotFound() {
        when(userService.getUser(1)).thenReturn(new User());
        when(vinylService.getVinyl(2)).thenReturn(new Vinyl());
        when(vinylStatusService.getById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.addVinylToUser(1, 2, 3))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Статус не найден");
    }

    @Test
    void getAllUserVinyls_shouldReturnList() {
        List<UserVinylDto> dtos = List.of(new UserVinylDto(), new UserVinylDto());
        when(userVinylService.getAllUserVinyls()).thenReturn(dtos);

        List<UserVinylDto> result = facade.getAllUserVinyls();
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteVinyl_shouldCallServices() {
        facade.deleteVinyl(10);
        verify(userVinylService).removeAllByVinyl(10);
        verify(vinylService).deleteVinyl(10);
    }

    @Test
    void updateVinylStatus_shouldUpdate() {
        User user = new User(); user.setId(1);
        Vinyl vinyl = new Vinyl(); vinyl.setId(2);
        VinylStatus status = new VinylStatus(); status.setId(3);
        UserVinyl uv = new UserVinyl();

        when(userService.getUser(1)).thenReturn(user);
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.of(status));
        when(userVinylService.updateVinylStatus(user, vinyl, status)).thenReturn(uv);

        UserVinyl result = facade.updateVinylStatus(1, 2, 3);
        assertThat(result).isEqualTo(uv);
    }

    @Test
    void findUserVinyl_shouldDelegateToService() {
        UserVinyl uv = new UserVinyl();
        when(userVinylService.findUserVinyl(1, 2)).thenReturn(Optional.of(uv));

        Optional<UserVinyl> result = facade.findUserVinyl(1, 2);
        assertThat(result).isPresent().contains(uv);
    }

    @Test
    void removeVinylFromUser_shouldCallService() {
        facade.removeVinylFromUser(1, 2);
        verify(userVinylService).removeVinylFromUser(1, 2);
    }

    @Test
    void getUserVinyls_shouldReturnList() {
        when(userVinylService.getUserVinyls(1)).thenReturn(List.of(new UserVinyl()));
        assertThat(facade.getUserVinyls(1)).hasSize(1);
    }

    @Test
    void getUsersByVinyl_shouldReturnList() {
        when(userVinylService.getUsersByVinyl(2)).thenReturn(List.of(new UserVinyl()));
        assertThat(facade.getUsersByVinyl(2)).hasSize(1);
    }
}
