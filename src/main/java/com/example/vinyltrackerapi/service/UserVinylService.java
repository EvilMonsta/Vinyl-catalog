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
import org.springframework.stereotype.Service;

@Service
public class UserVinylService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserVinylService.class);
    private final UserVinylRepository userVinylRepository;
    private final CacheService<List<UserVinyl>> userVinylCache;
    private final CacheService<List<UserVinyl>> vinylUserCache;
    private static final String KEY_USER_VINYLS = "user-vinyls-";
    private static final String KEY_VINYL_USERS = "vinyl-users-";

    public UserVinylService(UserVinylRepository userVinylRepository,
                            CacheService<List<UserVinyl>> userVinylCache,
                            CacheService<List<UserVinyl>> vinylUserCache) {
        this.userVinylRepository = userVinylRepository;
        this.userVinylCache = userVinylCache;
        this.vinylUserCache = vinylUserCache;
    }

    public UserVinylDto addVinylToUser(User user, Vinyl vinyl, VinylStatus status) {
        UserVinyl userVinyl = new UserVinyl(user, vinyl, status);
        UserVinyl saved = userVinylRepository.save(userVinyl);
        updateCacheAfterChange(user.getId(), vinyl.getId());
        LOGGER.info("[USER-VINYL] Пользователь ID={}, пластинка ID={}, статус ID={} связаны",
                user.getId(), vinyl.getId(), status.getId());
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

        List<UserVinyl> userVinyls = userVinylRepository.findAllWithVinylAndStatusByUserId(userId);

        userVinylCache.put(cacheKey, userVinyls);

        LOGGER.info("[USER-VINYL] Получены пластинки пользователя с ID={}", userId);
        return userVinyls;
    }

    public List<UserVinyl> getUsersByVinyl(Integer vinylId) {
        String cacheKey = KEY_VINYL_USERS + vinylId;
        if (vinylUserCache.contains(cacheKey)) {
            return vinylUserCache.get(cacheKey);
        }
        List<UserVinyl> users = userVinylRepository.findByVinylId(vinylId);
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

    public UserVinyl updateVinylStatus(User user, Vinyl vinyl, VinylStatus status) {
        UserVinyl userVinyl = findUserVinyl(user.getId(), vinyl.getId())
                .orElseThrow(() -> new IllegalStateException("Запись не найдена!"));
        userVinyl.setStatus(status);
        UserVinyl updated = userVinylRepository.save(userVinyl);
        updateCacheAfterChange(user.getId(), vinyl.getId());
        LOGGER.info("[USER-VINYL] Обновлен статус пользователя с ID={} и пластинки с ID={} на статус с ID={}",
                user.getId(), vinyl.getId(), status.getId());
        return updated;
    }

    private void updateCacheAfterChange(Integer userId, Integer vinylId) {
        List<UserVinyl> updatedUserVinyls = userVinylRepository.findByUserId(userId);
        List<UserVinyl> updatedVinylUsers = userVinylRepository.findByVinylId(vinylId);
        userVinylCache.put(KEY_USER_VINYLS + userId, updatedUserVinyls);
        vinylUserCache.put(KEY_VINYL_USERS + vinylId, updatedVinylUsers);
    }

    public void removeAllByUser(Integer userId) {
        userVinylRepository.deleteAllByUserId(userId);
        userVinylCache.remove(KEY_USER_VINYLS + userId);
        LOGGER.info("[CACHE] Удалены все связи пользователя: {}", userId);
    }

    public void removeAllByVinyl(Integer vinylId) {
        userVinylRepository.deleteAllByVinylId(vinylId);
        vinylUserCache.remove(KEY_VINYL_USERS + vinylId);
        LOGGER.info("[CACHE] Удалены все связи винила: {}", vinylId);
    }
}
