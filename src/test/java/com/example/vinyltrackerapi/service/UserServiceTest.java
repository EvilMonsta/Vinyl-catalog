package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.models.Role;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserRepository userRepository;
    @Mock private RoleService roleService;
    @Mock private CacheService<User> userCache;
    @Mock private CacheService<List<User>> userListCache;
    @Mock private CacheService<List<User>> userByUsernameCache;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, userCache, roleService, userListCache, userByUsernameCache);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void getUser_shouldReturnCachedUser() {
        User user = new User();
        user.setId(1);
        when(userCache.contains("user-1")).thenReturn(true);
        when(userCache.get("user-1")).thenReturn(user);

        User result = userService.getUser(1);
        assertThat(result.getId()).isEqualTo(1);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUser_shouldThrowIfNotFound() {
        when(userCache.contains("user-99")).thenReturn(false);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void createUser_shouldCreateSuccessfully() {
        UserDto dto = new UserDto();
        dto.setUsername("testuser");
        dto.setEmail("test@mail.com");
        dto.setPassword("123");
        dto.setRoleId(1);

        Role role = new Role();
        when(roleService.getRoleById(1)).thenReturn(role);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1);
            return u;
        });
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        User result = userService.createUser(dto);
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void createUser_shouldFailIfEmailExists() {
        UserDto dto = new UserDto();
        dto.setEmail("exists@mail.com");
        dto.setUsername("newuser");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        Integer userId = 1;
        UserDto dto = new UserDto();
        dto.setUsername("updated");
        dto.setEmail("new@mail.com");
        dto.setPassword("pass");
        dto.setRoleId(2);

        User existing = new User();
        existing.setId(userId);

        Role role = new Role();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(roleService.getRoleById(2)).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        User result = userService.updateUser(userId, dto);
        assertThat(result.getUsername()).isEqualTo("updated");
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("delme");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        userService.deleteUser(1);

        verify(userRepository).deleteById(1);
        verify(userCache).remove("user-1");
        verify(userListCache).put(eq("all-users"), any());
        verify(userByUsernameCache).remove("user-username-delme");
    }

    @Test
    void deleteUser_shouldThrowIfNotExists() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllUsers_shouldReturnFromCache() {
        List<User> cachedUsers = List.of(new User());
        when(userListCache.contains("all-users")).thenReturn(true);
        when(userListCache.get("all-users")).thenReturn(cachedUsers);

        List<User> result = userService.getAllUsers();

        assertThat(result).isSameAs(cachedUsers);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllUsers_shouldFetchAndCacheIfNotInCache() {
        List<User> dbUsers = List.of(new User());
        when(userListCache.contains("all-users")).thenReturn(false);
        when(userRepository.findAll()).thenReturn(dbUsers);

        List<User> result = userService.getAllUsers();

        assertThat(result).isEqualTo(dbUsers);
        verify(userListCache).put("all-users", dbUsers);
    }

    @Test
    void getUserByUsername_shouldReturnFromCache() {
        List<User> cached = List.of(new User());
        when(userByUsernameCache.contains("user-username-john")).thenReturn(true);
        when(userByUsernameCache.get("user-username-john")).thenReturn(cached);

        List<User> result = userService.getUserByUsername("john");

        assertThat(result).isSameAs(cached);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserByUsername_shouldQueryAndCacheIfMiss() {
        List<User> fromDb = List.of(new User());
        when(userByUsernameCache.contains("user-username-john")).thenReturn(false);
        when(userRepository.findByUsername("john")).thenReturn(fromDb);

        List<User> result = userService.getUserByUsername("john");

        assertThat(result).isEqualTo(fromDb);
        verify(userByUsernameCache).put("user-username-john", fromDb);
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        User user = new User();
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("test@mail.com");

        assertThat(result).containsSame(user);
    }

    @Test
    void getUserByEmail_shouldReturnEmpty() {
        when(userRepository.findByEmail("no@mail.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail("no@mail.com");

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_shouldThrowIfUserNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        UserDto dto = new UserDto();
        dto.setUsername("x");
        dto.setEmail("x@mail.com");
        dto.setPassword("123");
        dto.setRoleId(1);

        assertThatThrownBy(() -> userService.updateUser(999, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }
}
