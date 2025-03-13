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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserVinylService {
    private final UserVinylRepository userVinylRepository;
    private final UserService userService;
    private final VinylService vinylService;

    public UserVinylService(UserVinylRepository userVinylRepository,
                            UserService userService, VinylService vinylService) {
        this.userVinylRepository = userVinylRepository;
        this.userService = userService;
        this.vinylService = vinylService;
    }

    public UserVinylDto addVinylToUser(Integer userId, Integer vinylId, VinylStatus status) {
        final User user = userService.getUser(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь не найден!"));
        final Vinyl vinyl = vinylService.getVinyl(vinylId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Винил не найден!"));

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
    }

    public List<UserVinyl> getUserVinyls(Integer userId) {
        User user = userService.getUser(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с ID " + userId + " не найден!"));

        return userVinylRepository.findByUser(user);
    }

    public List<UserVinylDto> getAllUserVinyls() {
        return userVinylRepository.findAll().stream()
                .map(UserVinylDto::new)
                .toList();
    }

    public UserVinyl updateVinylStatus(Integer userId, Integer vinylId, VinylStatus newStatus) {
        Optional<UserVinyl> userVinylOpt = findUserVinyl(userId, vinylId);
        if (userVinylOpt.isPresent()) {
            UserVinyl userVinyl = userVinylOpt.get();
            userVinyl.setStatus(newStatus);
            return userVinylRepository.save(userVinyl);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена!");
    }
}