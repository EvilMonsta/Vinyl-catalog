package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.enums.VinylStatus;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.UserVinylId;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.UserRepository;
import com.example.vinyltrackerapi.api.repositories.UserVinylRepository;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserVinylService {
    private final UserVinylRepository userVinylRepository;
    private final UserRepository userRepository;
    private final VinylRepository vinylRepository;

    public UserVinylService(UserVinylRepository userVinylRepository,
                            UserRepository userRepository, VinylRepository vinylRepository) {
        this.userVinylRepository = userVinylRepository;
        this.userRepository = userRepository;
        this.vinylRepository = vinylRepository;
    }

    public UserVinyl addVinylToUser(Integer userId, Integer vinylId, VinylStatus status) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь не найден!"));
        final Vinyl vinyl = vinylRepository.findById(vinylId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Винил не найден!"));

        UserVinylDto userVinylDto = new UserVinylDto();
        userVinylDto.setUserId(userId);
        userVinylDto.setVinylId(vinylId);
        userVinylDto.setStatus(status);

        return userVinylRepository.save(userVinylDto.toEntity(user, vinyl));
    }

    public Optional<UserVinyl> findUserVinyl(Integer userId, Integer vinylId) {
        return userVinylRepository.findById(new UserVinylId(userId, vinylId));
    }

    public void removeVinylFromUser(Integer userId, Integer vinylId) {
        userVinylRepository.deleteById(new UserVinylId(userId, vinylId));
    }

    public List<UserVinyl> getUserVinyls(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с ID " + userId + " не найден!"));

        return userVinylRepository.findByUser(user);
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