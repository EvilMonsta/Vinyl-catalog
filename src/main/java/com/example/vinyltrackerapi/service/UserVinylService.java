package com.example.vinyltrackerapi.service;

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

    public UserVinylService(UserVinylRepository userVinylRepository) {
        this.userVinylRepository = userVinylRepository;
    }

    public UserVinyl addVinylToUser(User user, Vinyl vinyl, VinylStatus status) {
        UserVinyl userVinyl = new UserVinyl(user, vinyl, status);
        return userVinylRepository.save(userVinyl);
    }

    public Optional<UserVinyl> findUserVinyl(Integer userId, Integer vinylId) {
        return userVinylRepository.findById(new UserVinylId(userId, vinylId));
    }

    public void removeVinylFromUser(Integer userId, Integer vinylId) {
        userVinylRepository.deleteById(new UserVinylId(userId, vinylId));
    }

    public List<UserVinyl> getUserVinyls(User user) {
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