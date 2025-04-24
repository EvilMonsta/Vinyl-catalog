package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UpdateUserRoleDto;
import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.service.UserService;
import com.example.vinyltrackerapi.service.UserVinylFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи (админ)", description = "Управление пользователями — только для администратора")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserService userService;
    private final UserVinylFacade userVinylFacade;

    @Operation(summary = "Получить всех пользователей")
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserDto::new)
                .toList();
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@Parameter(description = "ID пользователя")
                                               @PathVariable Integer id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(new UserDto(user));
    }

    @Operation(summary = "Получить пользователей по username")
    @GetMapping("/search")
    public List<UserDto> getUserByUsername(@Parameter(description = "Имя пользователя")
                                           @RequestParam String username) {
        return userService.getUserByUsername(username).stream()
                .map(UserDto::new)
                .toList();
    }

    @Operation(summary = "Получить пользователя по email")
    @GetMapping("/email")
    public ResponseEntity<UserDto> getUserByEmail(@Parameter(description = "Email пользователя")
                                                  @RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(new UserDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Создать нового пользователя")
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@Parameter(description = "Данные нового пользователя")
                                              @Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(new UserDto(userService.createUser(userDto)));
    }

    @Operation(summary = "Обновить пользователя по ID")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(@Parameter(description = "ID пользователя")
                                              @PathVariable Integer id,
                                              @RequestBody @Valid UserDto userDto) {
        User updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }

    @Operation(summary = "Удалить пользователя по ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ID пользователя")
                                           @PathVariable Integer id) {
        userVinylFacade.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить роль пользователя по ID")
    @PutMapping("/update-role/{id}")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Integer id,
                                                  @RequestBody @Valid UpdateUserRoleDto dto) {
        User updatedUser = userService.updateUserRole(id, dto.getRoleId());
        return ResponseEntity.ok(new UserDto(updatedUser));
    }
}
