package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "Профиль пользователя", description = "Управление собственным аккаунтом")
@PreAuthorize("hasAnyRole('USER', 'VIP_USER', 'ADMIN')")
public class UserSelfController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить информацию о себе")
    public ResponseEntity<UserDto> getMyProfile(Principal principal) {
        User user = userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return ResponseEntity.ok(new UserDto(user));
    }

    @PutMapping
    @Operation(summary = "Обновить свой профиль")
    public ResponseEntity<UserDto> updateMyProfile(@Valid @RequestBody UserDto userDto, Principal principal) {
        User user = userService.getUserByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        userDto.setId(user.getId());
        return ResponseEntity.ok(new UserDto(userService.updateUser(user.getId(), userDto)));
    }
}
