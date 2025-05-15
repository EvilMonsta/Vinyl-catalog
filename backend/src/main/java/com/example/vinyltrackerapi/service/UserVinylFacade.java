package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import java.security.Principal;
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
    private static final String STATUS_ER = "Статус не найден!";

    private User getCurrentUser(Principal principal) {
        return userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Пользователь не найден"));
    }

    public UserVinylDto addVinylToCurrentUser(Integer vinylId, Integer statusId, Principal principal) {
        if (statusId < 1 || statusId > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый статус");
        }
        User user = getCurrentUser(principal);
        Vinyl vinyl = vinylService.getVinyl(vinylId);
        VinylStatus status = vinylStatusService.getById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ER));
        return userVinylService.addVinylToUser(user, vinyl, status);
    }

    public void removeVinylFromCurrentUser(Integer vinylId, Principal principal) {
        User user = getCurrentUser(principal);
        userVinylService.removeVinylFromUser(user.getId(), vinylId);
    }

    public List<UserVinylDto> getCurrentUserVinyls(Principal principal) {
        User user = getCurrentUser(principal);
        return userVinylService.getUserVinyls(user.getId()).stream()
                .map(UserVinylDto::new)
                .toList();
    }

    public Optional<UserVinyl> findVinylForCurrentUser(Integer vinylId, Principal principal) {
        User user = getCurrentUser(principal);
        return userVinylService.findUserVinyl(user.getId(), vinylId);
    }

    public UserVinyl updateCurrentUserVinylStatus(Integer vinylId, Integer statusId, Principal principal) {
        User user = getCurrentUser(principal);
        Vinyl vinyl = vinylService.getVinyl(vinylId);
        VinylStatus status = vinylStatusService.getById(statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ER));
        return userVinylService.updateVinylStatus(user, vinyl, status);
    }

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
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ER);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, STATUS_ER));
        return userVinylService.updateVinylStatus(user, vinyl, status);
    }

    public void deleteVinyl(Integer vinylId) {
        userVinylService.removeAllByVinyl(vinylId);
        vinylService.deleteVinyl(vinylId);
    }
}
