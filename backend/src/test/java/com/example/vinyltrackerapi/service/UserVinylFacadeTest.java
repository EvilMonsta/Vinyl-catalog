package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.models.Role;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserVinylFacadeTest {

    @InjectMocks private UserVinylFacade facade;
    @Mock private UserService userService;
    @Mock private VinylService vinylService;
    @Mock private UserVinylService userVinylService;
    @Mock private VinylStatusService vinylStatusService;

    private final Principal principal = () -> "test@mail.com";
    private final User user = new User();
    private final Vinyl vinyl = new Vinyl();
    private final VinylStatus status = new VinylStatus();
    private final UserVinyl userVinyl = new UserVinyl();

    @BeforeEach
    void init() {
        Role role = new Role();
        role.setId(1);
        user.setId(1);
        user.setRole(role);
        user.setUsername("uploader");
        vinyl.setId(2);
        status.setId(3);
    }

    @Test
    void addVinylToCurrentUser_shouldSucceed() {
        when(userService.getUserByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.of(status));
        when(userVinylService.addVinylToUser(user, vinyl, status)).thenReturn(new UserVinylDto());

        UserVinylDto result = facade.addVinylToCurrentUser(2, 3, principal);
        assertThat(result).isNotNull();
    }

    @Test
    void addVinylToCurrentUser_shouldThrowOnBadStatusIfLower() {
        assertThatThrownBy(() -> facade.addVinylToCurrentUser(2, 0, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Недопустимый статус");
    }

    @Test
    void addVinylToCurrentUser_shouldThrowOnBadStatusIfUpper() {
        assertThatThrownBy(() -> facade.addVinylToCurrentUser(2, 4, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Недопустимый статус");
    }

    @Test
    void removeVinylFromCurrentUser_shouldCallRemove() {
        when(userService.getUserByEmail("test@mail.com")).thenReturn(Optional.of(user));
        facade.removeVinylFromCurrentUser(2, principal);
        verify(userVinylService).removeVinylFromUser(1, 2);
    }

    @Test
    void getCurrentUserVinyls_shouldReturnList() {
        when(userService.getUserByEmail("test@mail.com")).thenReturn(Optional.of(user));

        userVinyl.setUser(user);
        userVinyl.setVinyl(new Vinyl());
        userVinyl.setStatus(new VinylStatus());

        when(userVinylService.getUserVinyls(1)).thenReturn(List.of(userVinyl));

        List<UserVinylDto> result = facade.getCurrentUserVinyls(principal);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllUserVinyls_shouldReturnList() {
        userVinyl.setUser(user);
        userVinyl.setVinyl(new Vinyl());
        userVinyl.setStatus(new VinylStatus());

        UserVinylDto dto = new UserVinylDto(userVinyl);
        when(userVinylService.getAllUserVinyls()).thenReturn(List.of(dto));

        List<UserVinylDto> result = facade.getAllUserVinyls();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(UserVinylDto.class);
        verify(userVinylService).getAllUserVinyls();
    }

    @Test
    void findVinylForCurrentUser_shouldDelegate() {
        when(userService.getUserByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(userVinylService.findUserVinyl(1, 2)).thenReturn(Optional.of(userVinyl));
        Optional<UserVinyl> result = facade.findVinylForCurrentUser(2, principal);
        assertThat(result).isPresent();
    }

    @Test
    void updateCurrentUserVinylStatus_shouldUpdate() {
        when(userService.getUserByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.of(status));
        when(userVinylService.updateVinylStatus(user, vinyl, status)).thenReturn(userVinyl);
        UserVinyl result = facade.updateCurrentUserVinylStatus(2, 3, principal);
        assertThat(result).isNotNull();
    }

    @Test
    void updateCurrentUserVinylStatus_shouldThrowIfStatusNotFound() {
        when(userService.getUserByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> facade.updateCurrentUserVinylStatus(2, 3, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Статус не найден");
    }

    @Test
    void addVinylToUser_shouldSucceed() {
        when(userService.getUser(1)).thenReturn(user);
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.of(status));
        when(userVinylService.addVinylToUser(user, vinyl, status)).thenReturn(new UserVinylDto());
        UserVinylDto result = facade.addVinylToUser(1, 2, 3);
        assertThat(result).isNotNull();
    }

    @Test
    void getUserVinyls_shouldReturnList() {
        when(userVinylService.getUserVinyls(1)).thenReturn(List.of(userVinyl));
        List<UserVinyl> result = facade.getUserVinyls(1);
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteUser_shouldCascadeRemove() {
        Vinyl v = new Vinyl(); v.setId(10); v.setAddedBy(user);
        when(userService.getUser(1)).thenReturn(user);
        when(vinylService.getVinylsByUploaderUsername("uploader")).thenReturn(List.of(v));
        facade.deleteUser(1);
        verify(vinylService).detachUserFromVinyl(user);
        verify(userVinylService).removeAllByUser(1);
        verify(userService).deleteUser(1);
    }

    @Test
    void updateVinylStatus_shouldWork() {
        when(userService.getUser(1)).thenReturn(user);
        when(vinylService.getVinyl(2)).thenReturn(vinyl);
        when(vinylStatusService.getById(3)).thenReturn(Optional.of(status));
        when(userVinylService.updateVinylStatus(user, vinyl, status)).thenReturn(userVinyl);
        UserVinyl result = facade.updateVinylStatus(1, 2, 3);
        assertThat(result).isNotNull();
    }

    @Test
    void deleteVinyl_shouldCallService() {
        facade.deleteVinyl(2);
        verify(userVinylService).removeAllByVinyl(2);
        verify(vinylService).deleteVinyl(2);
    }

    @Test
    void removeVinylFromUser_shouldCallService() {
        facade.removeVinylFromUser(1, 2);
        verify(userVinylService).removeVinylFromUser(1, 2);
    }

    @Test
    void getUsersByVinyl_shouldReturnList() {
        List<UserVinyl> list = List.of(new UserVinyl());
        when(userVinylService.getUsersByVinyl(1)).thenReturn(list);

        List<UserVinyl> result = facade.getUsersByVinyl(1);

        assertThat(result).isEqualTo(list);
        verify(userVinylService).getUsersByVinyl(1);
    }

    @Test
    void findUserVinyl_shouldReturnOptional() {
        UserVinyl uv = new UserVinyl();
        when(userVinylService.findUserVinyl(1, 2)).thenReturn(Optional.of(uv));

        Optional<UserVinyl> result = facade.findUserVinyl(1, 2);

        assertThat(result).contains(uv);
        verify(userVinylService).findUserVinyl(1, 2);
    }

}

