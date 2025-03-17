package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.api.enums.VinylStatus;
import com.example.vinyltrackerapi.service.UserVinylService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
public class UserVinylController {
    private final UserVinylService userVinylService;

    @PostMapping("/add")
    public ResponseEntity<UserVinylDto> addUserVinyl(@RequestParam Integer userId,
                                                     @RequestParam Integer vinylId,
                                                     @RequestParam VinylStatus status) {
        return ResponseEntity.ok(userVinylService.addVinylToUser(userId, vinylId, status));
    }

    @GetMapping
    public List<UserVinylDto> getAllUserVinyls() {
        return userVinylService.getAllUserVinyls();
    }

    @GetMapping("/find")
    public ResponseEntity<UserVinylDto> findUserVinyl(@RequestParam Integer userId,
                                                      @RequestParam Integer vinylId) {
        return userVinylService.findUserVinyl(userId, vinylId)
                .map(uv -> ResponseEntity.ok(new UserVinylDto(uv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeUserVinyl(@RequestParam Integer userId, @RequestParam Integer vinylId) {
        userVinylService.removeVinylFromUser(userId, vinylId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getVinyls/{userId}")
    public List<UserVinylDto> getUserVinyls(@PathVariable Integer userId) {
        return userVinylService.getUserVinyls(userId).stream()
                .map(UserVinylDto::new)
                .toList();
    }

    @GetMapping("/getUsers/{vinylId}")
    public List<UserVinylDto> getUsersByVinyl(@PathVariable Integer vinylId) {
        return userVinylService.getUsersByVinyl(vinylId).stream()
                .map(UserVinylDto::new)
                .toList();
    }

    @PutMapping("/update-status")
    public ResponseEntity<UserVinylDto> updateVinylStatus(@RequestParam Integer userId,
                                                          @RequestParam Integer vinylId,
                                                          @RequestParam VinylStatus newStatus) {
        return ResponseEntity.ok(new UserVinylDto(userVinylService.updateVinylStatus(userId,
                vinylId, newStatus)));
    }
}