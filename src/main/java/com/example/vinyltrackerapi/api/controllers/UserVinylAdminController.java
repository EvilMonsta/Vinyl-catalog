package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UserVinylDto;
import com.example.vinyltrackerapi.service.UserVinylFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user-vinyls")
@RequiredArgsConstructor
@Tag(name = "Связи пользователей и винилов (админ)", description = "Для администраторов")
@PreAuthorize("hasRole('ADMIN')")
public class UserVinylAdminController {
    private final UserVinylFacade userVinylFacade;

    @GetMapping
    public List<UserVinylDto> getAllUserVinyls() {
        return userVinylFacade.getAllUserVinyls();
    }

    @GetMapping("/getUsers/{vinylId}")
    public List<UserVinylDto> getUsersByVinyl(@PathVariable Integer vinylId) {
        return userVinylFacade.getUsersByVinyl(vinylId).stream().map(UserVinylDto::new).toList();
    }
}