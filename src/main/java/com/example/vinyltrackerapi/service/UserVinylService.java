package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.UserVinylId;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import com.example.vinyltrackerapi.api.repositories.UserVinylRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserVinylService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserVinylService.class);
    private final UserVinylRepository userVinylRepository;
    private final UserService userService;
    private final VinylService vinylService;
    private final VinylStatusService vinylStatusService;
    private final CacheService<List<UserVinyl>> userVinylCache;
    private final CacheService<List<UserVinyl>> vinylUserCache;
    private static final String KEY_USER_VINYLS = "user-vinyls-";
    private static final String KEY_VINYL_USERS = "vinyl-users-";

    public UserVinylService(UserVinylRepository userVinylRepository,
                            @Lazy UserService userService, @Lazy VinylService vinylService,
                            VinylStatusService vinylStatusService,
                            CacheService<List<UserVinyl>> userVinylCache,
                            CacheService<List<UserVinyl>> vinylUserCache) {
        this.userVinylRepository = userVinylRepository;
        this.userService = userService;
        this.vinylService = vinylService;
        this.vinylStatusService = vinylStatusService;
        this.userVinylCache = userVinylCache;
        this.vinylUserCache = vinylUserCache;
    }

    public UserVinylDto addVinylToUser(Integer userId, Integer vinylId, Integer statusId) {
        User user = userService.getUser(userId);

        Vinyl vinyl = vinylService.getVinyl(vinylId);
        VinylStatus status = vinylStatusService.getById(statusId)
                .orElseThrow(() -> {
                    LOGGER.warn("[USER-VINYL] Статус с ID={} не найден!", statusId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Статус не найден!");
                });

        UserVinyl userVinyl = new UserVinyl(user, vinyl, status);
        UserVinyl saved = userVinylRepository.save(userVinyl);

        updateCacheAfterChange(userId, vinylId);
        LOGGER.info("[USER-VINYL] Пользователь ID={}, пластинка ID={}, статус ID={} связаны", userId, vinylId,
                statusId);
        return new UserVinylDto(saved);
    }

    public Optional<UserVinyl> findUserVinyl(Integer userId, Integer vinylId) {
        LOGGER.info("[USER-VINYL] Найдена связь пользователя с ID={} и пластинки с ID={}", userId, vinylId);
        return userVinylRepository.findById(new UserVinylId(userId, vinylId));
    }

    public void removeVinylFromUser(Integer userId, Integer vinylId) {
        userVinylRepository.deleteById(new UserVinylId(userId, vinylId));
        updateCacheAfterChange(userId, vinylId);
        LOGGER.info("[USER-VINYL] Удалена связь пользователя с ID={} и пластинки с ID={}", userId, vinylId);
    }

    public List<UserVinyl> getUserVinyls(Integer userId) {
        String cacheKey = KEY_USER_VINYLS + userId;
        if (userVinylCache.contains(cacheKey)) {
            return userVinylCache.get(cacheKey);
        }

        User user = userService.getUser(userId);

        List<UserVinyl> userVinyls = userVinylRepository.findByUser(user);
        userVinylCache.put(cacheKey, userVinyls);
        LOGGER.info("[USER-VINYL] Получены пластинки пользователя с ID={}", userId);
        return userVinyls;
    }

    public List<UserVinyl> getUsersByVinyl(Integer vinylId) {
        String cacheKey = KEY_VINYL_USERS + vinylId;
        if (vinylUserCache.contains(cacheKey)) {
            return vinylUserCache.get(cacheKey);
        }

        Vinyl vinyl = vinylService.getVinyl(vinylId);

        List<UserVinyl> users = userVinylRepository.findByVinyl(vinyl);
        vinylUserCache.put(cacheKey, users);
        LOGGER.info("[USER-VINYL] Получены пользователи, добавившие пластинку с ID={}", vinylId);
        return users;
    }

    public List<UserVinylDto> getAllUserVinyls() {
        LOGGER.info("[USER-VINYL] Получены все связи");
        return userVinylRepository.findAll().stream()
                .map(UserVinylDto::new)
                .toList();
    }

    public UserVinyl updateVinylStatus(Integer userId, Integer vinylId, Integer statusId) {
        UserVinyl userVinyl = findUserVinyl(userId, vinylId)
                .orElseThrow(() -> {
                    LOGGER.warn("[USER-VINYL] Связь пользователя с ID={} и пластини с ID={} не найдена",
                            userId, vinylId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена!");
                });

        VinylStatus newStatus = vinylStatusService.getById(statusId)
                .orElseThrow(() -> {
                    LOGGER.warn("[USER-VINYL] Статус с ID={} не найден", statusId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Статус не найден!");
                });

        userVinyl.setStatus(newStatus);
        UserVinyl updated = userVinylRepository.save(userVinyl);
        updateCacheAfterChange(userId, vinylId);
        LOGGER.info("[USER-VINYL] Обновлен статус пользователя с ID={} и пластинки с ID={} на статус с ID={}",
                userId, vinylId, statusId);
        return updated;
    }

    private void updateCacheAfterChange(Integer userId, Integer vinylId) {
        List<UserVinyl> updatedUserVinyls = userVinylRepository.findByUser(
                userService.getUser(userId)
        );

        List<UserVinyl> updatedVinylUsers = userVinylRepository.findByVinyl(
                vinylService.getVinyl(vinylId));

        userVinylCache.put(KEY_USER_VINYLS + userId, updatedUserVinyls);
        vinylUserCache.put(KEY_VINYL_USERS + vinylId, updatedVinylUsers);
    }

    public void handleUserDeletion(Integer userId) {
        List<UserVinyl> userVinyls = getUserVinyls(userId);
        userVinyls.forEach(userVinyl ->
                userVinylRepository.deleteById(new UserVinylId(userId, userVinyl.getVinyl().getId())));

        userVinylCache.remove(KEY_USER_VINYLS + userId);
        LOGGER.info("[CACHE] Удалён пользователь из всех связей: {}", userId);
    }

    public void handleVinylDeletion(Integer vinylId) {
        List<UserVinyl> vinylUsers = getUsersByVinyl(vinylId);
        vinylUsers.forEach(userVinyl ->
                userVinylRepository.deleteById(new UserVinylId(userVinyl.getUser().getId(), vinylId)));

        vinylUserCache.remove(KEY_VINYL_USERS + vinylId);
        LOGGER.info("[CACHE] Удалён винил из всех связей: {}", vinylId);
    }
}