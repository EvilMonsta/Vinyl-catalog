package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.repositories.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final CacheService<User> userCache;
    private final CacheService<List<User>> userListCache;
    private final CacheService<List<User>> userByUsernameCache;
    private static final String KEY_ALL = "all-users";
    private static final String KEY_ID = "user-";
    private static final String KEY_NAME = "user-username-";

    public UserService(UserRepository userRepository,
                       CacheService<User> userCache,
                       RoleService roleService,
                       CacheService<List<User>> userListCache,
                       CacheService<List<User>> userByUsernameCache) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userCache = userCache;
        this.userListCache = userListCache;
        this.userByUsernameCache = userByUsernameCache;
    }

    public List<User> getAllUsers() {
        String cacheKey = KEY_ALL;
        if (userListCache.contains(cacheKey)) {
            return userListCache.get(cacheKey);
        }
        List<User> users = userRepository.findAll();
        userListCache.put(cacheKey, users);
        LOGGER.info("[USER] Получены все пользователи");
        return users;
    }

    public User getUser(Integer id) {
        String cacheKey = KEY_ID + id;

        if (userCache.contains(cacheKey)) {
            return userCache.get(cacheKey);
        }

        return userRepository.findById(id)
                .map(user -> {
                    userCache.put(cacheKey, user);
                    LOGGER.info("[USER] Пользователь найден и добавлен в кэш: ID={}", id);
                    return user;
                })
                .orElseThrow(() -> {
                    LOGGER.warn("[USER] Пользователь с ID={} не найден!", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Пользователь с ID " + id + " не найден!");
                });
    }

    public List<User> getUserByUsername(String username) {
        String cacheKey = KEY_NAME + username;
        if (userByUsernameCache.contains(cacheKey)) {
            return userByUsernameCache.get(cacheKey);
        }
        List<User> users = userRepository.findByUsername(username);
        userByUsernameCache.put(cacheKey, users);
        LOGGER.info("[USER] Получен пользователь по имени");
        return users;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(UserDto userDto) {
        User user = userDto.toEntity(roleService.getRoleById(userDto.getRoleId()));
        if (userRepository.existsByEmail(user.getEmail())) {
            LOGGER.warn("[USER] Пользователь с таким email уже существует: {}", userDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с таким email уже существует!");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            LOGGER.warn("[USER] Пользователь с таким username уже существует: {}", userDto.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с таким username уже существует!");
        }
        user.setPassword(hashPassword(userDto.getPassword()));
        User savedUser = userRepository.save(user);

        userCache.put(KEY_ID + savedUser.getId(), savedUser);
        userListCache.put(KEY_ALL, userRepository.findAll());
        userByUsernameCache.put(KEY_NAME + savedUser.getUsername(), List.of(savedUser));
        LOGGER.info("[USER] Создан пользователь ID={}", savedUser.getId());

        return savedUser;
    }

    public User updateUser(Integer id, UserDto userDto) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
                user.setPassword(hashPassword(userDto.getPassword()));
            }
            user.setRole(roleService.getRoleById(userDto.getRoleId()));
            User updatedUser = userRepository.save(user);

            userCache.put(KEY_ID + id, updatedUser);
            userListCache.put(KEY_ALL, userRepository.findAll());
            userByUsernameCache.put(KEY_NAME + updatedUser.getUsername(), List.of(updatedUser));

            LOGGER.info("[USER] Обновлен пользователь с ID={}, {}", userDto.getId(), user);

            return updatedUser;
        }).orElseThrow(() -> {
            LOGGER.warn("[USER] Пользователь не найден!");
            return new RuntimeException("Пользователь не найден!");
        });
    }

    public void deleteUser(Integer id) {
        final User user = userRepository.findById(id).orElseThrow(() -> {
            LOGGER.warn("[USER] Пользователь с таким ID={} не найден!", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с ID " + id + " не найден!"); });
        userRepository.deleteById(id);
        userCache.remove(KEY_ID + id);
        userListCache.put(KEY_ALL, userRepository.findAll());
        userByUsernameCache.remove(KEY_NAME + user.getUsername());
        LOGGER.info("[USER] Удалён пользователь с ID={}", id);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error occurred while hashing the password", e);
        }
    }

    public User updateUserRole(Integer id, Integer roleId) {
        return userRepository.findById(id).map(user -> {
            user.setRole(roleService.getRoleById(roleId));
            User updatedUser = userRepository.save(user);

            userCache.put(KEY_ID + id, updatedUser);
            userListCache.put(KEY_ALL, userRepository.findAll());
            userByUsernameCache.put(KEY_NAME + updatedUser.getUsername(), List.of(updatedUser));

            LOGGER.info("[USER] Обновлена роль пользователя ID={} на роль ID={}", id, roleId);

            return updatedUser;
        }).orElseThrow(() -> {
            LOGGER.warn("[USER] При попытке изменить роль пользователь не нашелся!");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден!");
        });
    }
}
