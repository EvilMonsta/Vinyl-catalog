package com.example.vinyltrackerapi.api.dto;

import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.models.VinylStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVinylDto {
    @Valid
    @NotNull(message = "Пользователь не может быть null")
    private UserDto user;
    @Valid
    @NotNull(message = "Пластинка не может быть null")
    private VinylDto vinyl;
    @NotNull(message = "ID статуса винила обязателен")
    private Integer statusId;

    private DictionaryEntryDto status;

    public UserVinylDto(UserVinyl userVinyl) {
        this.user = new UserDto(userVinyl.getUser());
        this.vinyl = new VinylDto(userVinyl.getVinyl());
        this.statusId = userVinyl.getStatus().getId();
        this.status = new DictionaryEntryDto(userVinyl.getStatus().getId(), userVinyl.getStatus().getName());
    }

    public UserVinyl toEntity(User user, Vinyl vinyl, VinylStatus status) {
        return new UserVinyl(user, vinyl, status);
    }

    public int getUserId() {
        return user.getId();
    }

    public int getVinylId() {
        return vinyl.getId();
    }
}