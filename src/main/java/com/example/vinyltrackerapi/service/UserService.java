package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.repositories.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VinylService vinylService;
    private final UserVinylService userVinylService;
    private final CacheService<User> userCache;
    private final CacheService<List<User>> userListCache;
    private final CacheService<List<User>> userByUsernameCache;
    private final String keyAll = "all-users";
    private final String keyId = "user-";
    private final String keyName = "user-username-";

    public UserService(UserRepository userRepository, @Lazy VinylService vinylService,
                       CacheService<User> userCache,
                       @Lazy UserVinylService userVinylService,
                       CacheService<List<User>> userListCache,
                       CacheService<List<User>> userByUsernameCache) {
        this.userRepository = userRepository;
        this.vinylService = vinylService;
        this.userVinylService = userVinylService;
        this.userCache = userCache;
        this.userListCache = userListCache;
        this.userByUsernameCache = userByUsernameCache;
    }

    public List<User> getAllUsers() {
        String cacheKey = keyAll;
        if (userListCache.contains(cacheKey)) {
            return userListCache.get(cacheKey);
        }
        List<User> users = userRepository.findAll();
        userListCache.put(cacheKey, users);
        return users;
    }

    public Optional<User> getUser(Integer id) {
        String cacheKey = keyId + id;
        if (userCache.contains(cacheKey)) {
            return Optional.of(userCache.get(cacheKey));
        }
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> userCache.put(cacheKey, u));
        return user;
    }

    public List<User> getUserByUsername(String username) {
        String cacheKey = keyName + username;
        if (userByUsernameCache.contains(cacheKey)) {
            return userByUsernameCache.get(cacheKey);
        }
        List<User> users = userRepository.findByUsername(username);
        userByUsernameCache.put(cacheKey, users);
        return users;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(UserDto userDto) {
        User user = userDto.toEntity();
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с таким email уже существует!");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с таким username уже существует!");
        }
        user.setPassword(hashPassword(userDto.getPassword()));
        User savedUser = userRepository.save(user);

        userCache.put(keyId + savedUser.getId(), savedUser);
        userListCache.put(keyAll, userRepository.findAll());
        userByUsernameCache.put(keyName + savedUser.getUsername(), List.of(savedUser));

        return savedUser;
    }

    public User updateUser(Integer id, User newUserData) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(newUserData.getUsername());
            user.setEmail(newUserData.getEmail());
            if (newUserData.getPassword() != null && !newUserData.getPassword().isBlank()) {
                user.setPassword(hashPassword(newUserData.getPassword()));
            }
            user.setRole(newUserData.getRole());
            User updatedUser = userRepository.save(user);

            userCache.put(keyId + id, updatedUser);
            userListCache.put(keyAll, userRepository.findAll());
            userByUsernameCache.put(keyName + updatedUser.getUsername(), List.of(updatedUser));

            return updatedUser;
        }).orElseThrow(() -> new RuntimeException("Пользователь не найден!"));
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с ID " + id + " не найден!"));

        userVinylService.handleUserDeletion(id);
        vinylService.detachUserFromVinyl(user);
        userRepository.deleteById(id);

        userCache.remove(keyId + id);
        userListCache.put(keyAll, userRepository.findAll());
        userByUsernameCache.remove(keyName + user.getUsername());
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("eror", e);
        }
    }
}
