package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.Role;
import com.example.vinyltrackerapi.api.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    private RoleRepository roleRepository;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        roleService = new RoleService(roleRepository);
    }

    @Test
    void loadRoles_shouldCacheRoles() {
        Role admin = new Role(1, "ADMIN");
        Role user = new Role(2, "USER");

        when(roleRepository.findAll()).thenReturn(List.of(admin, user));

        roleService.loadRoles();

        Role result1 = roleService.getRoleById(1);
        Role result2 = roleService.getRoleById(2);

        assertThat(result1).isEqualTo(admin);
        assertThat(result2).isEqualTo(user);
    }

    @Test
    void getRoleById_shouldThrowIfRoleNotFound() {
        when(roleRepository.findAll()).thenReturn(List.of()); // пусто

        roleService.loadRoles();

        assertThatThrownBy(() -> roleService.getRoleById(999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("не найдена");
    }
}
