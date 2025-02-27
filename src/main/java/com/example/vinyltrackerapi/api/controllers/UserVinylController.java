package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.enums.VinylStatus;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.UserRepository;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import com.example.vinyltrackerapi.service.UserVinylService;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-vinyls")
public class UserVinylController {
    private final UserVinylService userVinylService;
    private final UserRepository userRepository;
    private final VinylRepository vinylRepository;

    public UserVinylController(UserVinylService userVinylService,
                               UserRepository userRepository,
                               VinylRepository vinylRepository) {
        this.userVinylService = userVinylService;
        this.userRepository = userRepository;
        this.vinylRepository = vinylRepository;
    }

    @PostMapping("/add")
    public UserVinyl addUserVinyl(@RequestParam Integer userId,
                                  @RequestParam Integer vinylId,
                                  @RequestParam VinylStatus status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Vinyl vinyl = vinylRepository.findById(vinylId).orElseThrow(() ->
                new RuntimeException("Vinyl not found"));
        return userVinylService.addVinylToUser(user, vinyl, status);
    }

    @GetMapping("/find")
    public Optional<UserVinyl> findUserVinyl(@RequestParam Integer userId, @RequestParam Integer vinylId) {
        return userVinylService.findUserVinyl(userId, vinylId);
    }

    @DeleteMapping("/remove")
    public void removeUserVinyl(@RequestParam Integer userId, @RequestParam Integer vinylId) {
        userVinylService.removeVinylFromUser(userId, vinylId);
    }

    @GetMapping("/{userId}")
    public List<UserVinyl> getUserVinyls(@PathVariable Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return userVinylService.getUserVinyls(user);
    }

    @PutMapping("/update-status")
    public UserVinyl updateVinylStatus(@RequestParam Integer userId,
                                       @RequestParam Integer vinylId,
                                       @RequestParam VinylStatus newStatus) {
        return userVinylService.updateVinylStatus(userId, vinylId, newStatus);
    }
}
