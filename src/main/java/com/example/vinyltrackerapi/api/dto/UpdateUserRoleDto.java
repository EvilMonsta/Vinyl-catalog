package com.example.vinyltrackerapi.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleDto {
    @NotNull(message = "Роль обязательна")
    private Integer roleId;
}
