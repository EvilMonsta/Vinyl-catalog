package com.example.vinyltrackerapi.api.dto;

import com.example.vinyltrackerapi.api.enums.VinylStatus;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.Vinyl;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVinylDto {
    private UserDto user;
    private VinylDto vinyl;
    private VinylStatus status;

    public UserVinylDto(UserVinyl userVinyl) {
        this.user = new UserDto(userVinyl.getUser());
        this.vinyl = new VinylDto(userVinyl.getVinyl());
        this.status = userVinyl.getStatus();
    }

    public UserVinyl toEntity(User user, Vinyl vinyl) {
        return new UserVinyl(user, vinyl, this.status);
    }
}