package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.service.UserVinylService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Связь Пользователь-Пластинка", description = "Пользователь добавляет" +
        " пластинки себе в коллекцию с определённым статусом")
public class UserVinylController {
    private final UserVinylService userVinylService;

    @Operation(summary = "Добавить пластинку пользователю")
    @PostMapping("/add")
    public ResponseEntity<UserVinylDto> addUserVinyl(@RequestParam Integer userId,
                                                     @RequestParam Integer vinylId,
                                                     @RequestParam Integer statusId) {
        return ResponseEntity.ok(userVinylService.addVinylToUser(userId, vinylId, statusId));
    }

    @Operation(summary = "Получить все связи")
    @GetMapping
    public List<UserVinylDto> getAllUserVinyls() {
        return userVinylService.getAllUserVinyls();
    }

    @Operation(summary = "Получить конкретную связь")
    @GetMapping("/find")
    public ResponseEntity<UserVinylDto> findUserVinyl(@RequestParam Integer userId,
                                                      @RequestParam Integer vinylId) {
        return userVinylService.findUserVinyl(userId, vinylId)
                .map(uv -> ResponseEntity.ok(new UserVinylDto(uv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Удалить пластинку у пользователя")
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeUserVinyl(@RequestParam Integer userId, @RequestParam Integer vinylId) {
        userVinylService.removeVinylFromUser(userId, vinylId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить пластинки пользователя")
    @GetMapping("/getVinyls/{userId}")
    public List<UserVinylDto> getUserVinyls(@Parameter(description = "ID пользователя")
                                                @PathVariable Integer userId) {
        return userVinylService.getUserVinyls(userId).stream()
                .map(UserVinylDto::new)
                .toList();
    }

    @Operation(summary = "Получить пользователей, добавивших данную пластинку")
    @GetMapping("/getUsers/{vinylId}")
    public List<UserVinylDto> getUsersByVinyl(@Parameter(description = "ID пластинки")
                                                  @PathVariable Integer vinylId) {
        return userVinylService.getUsersByVinyl(vinylId).stream()
                .map(UserVinylDto::new)
                .toList();
    }

    @Operation(summary = "Обновить статус пластинки у пользователя")
    @PutMapping("/update-status")
    public ResponseEntity<UserVinylDto> updateVinylStatus(@RequestParam Integer userId,
                                                          @RequestParam Integer vinylId,
                                                          @RequestParam Integer newStatusId) {
        return ResponseEntity.ok(new UserVinylDto(userVinylService.updateVinylStatus(userId,
                vinylId, newStatusId)));
    }
}