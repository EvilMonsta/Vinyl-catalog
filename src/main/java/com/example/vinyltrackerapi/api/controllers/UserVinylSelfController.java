package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.service.UserVinylFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/user-vinyls")
@RequiredArgsConstructor
@Tag(name = "Мои пластинки", description = "Управление своей коллекцией")
@PreAuthorize("hasAnyRole('USER', 'VIP_USER', 'ADMIN')")
public class UserVinylSelfController {
    private final UserVinylFacade userVinylFacade;

    @Operation(summary = "Добавить пластинку текущему пользователю")
    @PostMapping("/add")
    public ResponseEntity<UserVinylDto> addVinyl(@RequestParam Integer vinylId,
                                                 @RequestParam Integer statusId,
                                                 Principal principal) {
        return ResponseEntity.ok(userVinylFacade.addVinylToCurrentUser(vinylId, statusId, principal));
    }

    @Operation(summary = "Удалить пластинку у текущего пользователя")
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeVinyl(@RequestParam Integer vinylId, Principal principal) {
        userVinylFacade.removeVinylFromCurrentUser(vinylId, principal);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить все связи текущего пользователя")
    @GetMapping
    public List<UserVinylDto> getMyVinyls(Principal principal) {
        return userVinylFacade.getCurrentUserVinyls(principal);
    }

    @Operation(summary = "Получить конкретную связь у текущего пользователя")
    @GetMapping("/find")
    public ResponseEntity<UserVinylDto> findVinyl(@RequestParam Integer vinylId, Principal principal) {
        return userVinylFacade.findVinylForCurrentUser(vinylId, principal)
                .map(uv -> ResponseEntity.ok(new UserVinylDto(uv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Обновить статус пластинки у текущего пользователя")
    @PutMapping("/update-status")
    public ResponseEntity<UserVinylDto> updateStatus(@RequestParam Integer vinylId,
                                                     @RequestParam Integer newStatusId,
                                                     Principal principal) {
        return ResponseEntity.ok(new UserVinylDto(
                userVinylFacade.updateCurrentUserVinylStatus(vinylId, newStatusId, principal)));
    }
}