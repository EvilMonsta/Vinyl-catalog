package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.Role;
import com.example.vinyltrackerapi.api.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final Map<Integer, Role> roleCache = new HashMap<>();

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void loadRoles() {
        roleRepository.findAll().forEach(role -> roleCache.put(role.getId(), role));
    }

    public Role getRoleById(Integer id) {
        Role role = roleCache.get(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Роль с ID " + id + " не найдена");
        }
        return role;
    }

    public Collection<Role> getAllRoles() {
        return roleCache.values();
    }
}