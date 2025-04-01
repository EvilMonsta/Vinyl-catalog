package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.models.Role;
import com.example.vinyltrackerapi.api.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;
    private final Map<Integer, Role> roleCache = new HashMap<>();

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void loadRoles() {
        LOGGER.info("[ROLE] Все роли загружены в кэш");
        roleRepository.findAll().forEach(role -> roleCache.put(role.getId(), role));
    }

    public Role getRoleById(Integer id) {
        Role role = roleCache.get(id);
        if (role == null) {
            LOGGER.warn("[ROLE] Роль с ID={} не найдена!", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Роль с ID " + id + " не найдена");
        }
        return role;
    }
}