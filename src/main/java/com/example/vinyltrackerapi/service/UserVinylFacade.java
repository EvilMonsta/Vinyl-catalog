package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserVinylFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserVinylFacade.class);
    private final UserService userService;
    private final VinylService vinylService;
    private final VinylStatusService vinylStatusService;
    private final UserVinylService userVinylService;

    public UserVinylFacade(UserService userService, VinylService vinylService,
                           UserVinylService userVinylService,
                           VinylStatusService vinylStatusService) {
        this.userService = userService;
        this.vinylService = vinylService;
        this.userVinylService = userVinylService;
        this.vinylStatusService = vinylStatusService;
    }

    public UserVinylDto addVinylToUser(Integer userId, Integer vinylId, Integer statusId) {
        User user = userService.getUser(userId);
        Vinyl vinyl = vinylService.getVinyl(vinylId);
        VinylStatus status = vinylStatusService.getById(statusId)
                .orElseThrow(() -> {
                    LOGGER.warn("[USER-VINYL] Статус с ID={} не найден!", statusId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Статус не найден!");
                });
        return userVinylService.addVinylToUser(user, vinyl, status);
    }

    public Optional<UserVinyl> findUserVinyl(Integer userId, Integer vinylId) {
        return userVinylService.findUserVinyl(userId, vinylId);
    }

    public List<UserVinyl> getUserVinyls(Integer userId) {
        return userVinylService.getUserVinyls(userId);
    }

    public List<UserVinyl> getUsersByVinyl(Integer vinylId) {
        return userVinylService.getUsersByVinyl(vinylId);
    }

    public List<UserVinylDto> getAllUserVinyls() {
        return userVinylService.getAllUserVinyls();
    }

    public void removeVinylFromUser(Integer userId, Integer vinylId) {
        userVinylService.removeVinylFromUser(userId, vinylId);
    }

    public void deleteUser(Integer userId) {
        User user = userService.getUser(userId);
        List<Vinyl> vinyls = vinylService.getVinylsByUploaderUsername(user.getUsername());
        vinyls.forEach(v -> v.setAddedBy(null));
        vinylService.detachUserFromVinyl(user);
        userVinylService.removeAllByUser(userId);
        userService.deleteUser(userId);
    }

    public UserVinyl updateVinylStatus(Integer userId, Integer vinylId, Integer statusId) {
        User user = userService.getUser(userId);
        Vinyl vinyl = vinylService.getVinyl(vinylId);
        VinylStatus status = vinylStatusService.getById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Статус не найден!"));
        return userVinylService.updateVinylStatus(user, vinyl, status);
    }

    public void deleteVinyl(Integer vinylId) {
        userVinylService.removeAllByVinyl(vinylId);
        vinylService.deleteVinyl(vinylId);
    }
}
