package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.enums.VinylStatus;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.UserVinylId;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.UserVinylRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserVinylService {
    private final UserVinylRepository userVinylRepository;
    private final UserService userService;
    private final VinylService vinylService;
    private final CacheService<List<UserVinyl>> userVinylCache;
    private final CacheService<List<UserVinyl>> vinylUserCache;
    private final String keyUserVinyls = "user-vinyls-";
    private final String keyVinylUsers = "vinyl-users-";
    private final String vinylNotFound = "Винил не найден!";
    private final String userNotFound = "Пользователь не найден!";

    public UserVinylService(UserVinylRepository userVinylRepository,
                            @Lazy UserService userService, @Lazy VinylService vinylService,
                            CacheService<List<UserVinyl>> userVinylCache,
                            CacheService<List<UserVinyl>> vinylUserCache) {
        this.userVinylRepository = userVinylRepository;
        this.userService = userService;
        this.vinylService = vinylService;
        this.userVinylCache = userVinylCache;
        this.vinylUserCache = vinylUserCache;
    }

    public UserVinylDto addVinylToUser(Integer userId, Integer vinylId, VinylStatus status) {
        final User user = userService.getUser(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        userNotFound));
        final Vinyl vinyl = vinylService.getVinyl(vinylId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, vinylNotFound));

        UserVinyl userVinyl = new UserVinyl();
        userVinyl.setUser(user);
        userVinyl.setVinyl(vinyl);
        userVinyl.setStatus(status);
        UserVinyl savedUserVinyl = userVinylRepository.save(userVinyl);

        return new UserVinylDto(savedUserVinyl);
    }

    public Optional<UserVinyl> findUserVinyl(Integer userId, Integer vinylId) {
        return userVinylRepository.findById(new UserVinylId(userId, vinylId));
    }

    public void removeVinylFromUser(Integer userId, Integer vinylId) {
        userVinylRepository.deleteById(new UserVinylId(userId, vinylId));

        updateCacheAfterChange(userId, vinylId);
    }

    public List<UserVinyl> getUserVinyls(Integer userId) {
        String cacheKey = keyUserVinyls + userId;
        if (userVinylCache.contains(cacheKey)) {
            return userVinylCache.get(cacheKey);
        }

        User user = userService.getUser(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с ID " + userId + " не найден!"));

        List<UserVinyl> userVinyls = userVinylRepository.findByUser(user);
        userVinylCache.put(cacheKey, userVinyls);
        return userVinyls;
    }

    public List<UserVinyl> getUsersByVinyl(Integer vinylId) {
        String cacheKey = keyVinylUsers + vinylId;
        if (vinylUserCache.contains(cacheKey)) {
            return vinylUserCache.get(cacheKey);
        }

        List<UserVinyl> users = userVinylRepository.findByVinyl(
                vinylService.getVinyl(vinylId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, vinylNotFound))
        );

        vinylUserCache.put(cacheKey, users);
        return users;
    }

    public List<UserVinylDto> getAllUserVinyls() {
        return userVinylRepository.findAll().stream()
                .map(UserVinylDto::new)
                .toList();
    }

    public UserVinyl updateVinylStatus(Integer userId, Integer vinylId, VinylStatus newStatus) {
        UserVinyl userVinyl = findUserVinyl(userId, vinylId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена!"));

        userVinyl.setStatus(newStatus);
        UserVinyl updated = userVinylRepository.save(userVinyl);

        updateCacheAfterChange(userId, vinylId);

        return updated;
    }

    private void updateCacheAfterChange(Integer userId, Integer vinylId) {
        List<UserVinyl> updatedUserVinyls = userVinylRepository.findByUser(
                userService.getUser(userId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFound))
        );

        List<UserVinyl> updatedVinylUsers = userVinylRepository.findByVinyl(
                vinylService.getVinyl(vinylId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, vinylNotFound))
        );

        userVinylCache.put(keyUserVinyls + userId, updatedUserVinyls);
        vinylUserCache.put(keyVinylUsers + vinylId, updatedVinylUsers);
    }

    public void handleUserDeletion(Integer userId) {
        List<UserVinyl> userVinyls = getUserVinyls(userId);
        userVinyls.forEach(userVinyl ->
                userVinylRepository.deleteById(new UserVinylId(userId, userVinyl.getVinyl().getId())));

        userVinylCache.remove(keyUserVinyls + userId);
        System.out.println("[CACHE] Удалён пользователь из всех связей: userId = " + userId);
    }

    public void handleVinylDeletion(Integer vinylId) {
        List<UserVinyl> vinylUsers = getUsersByVinyl(vinylId);
        vinylUsers.forEach(userVinyl ->
                userVinylRepository.deleteById(new UserVinylId(userVinyl.getUser().getId(), vinylId)));

        vinylUserCache.remove(keyVinylUsers + vinylId);
        System.out.println("[CACHE] Удалён винил из всех связей: vinylId = " + vinylId);
    }
}