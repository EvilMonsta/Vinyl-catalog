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
    private Integer userId;
    private Integer vinylId;
    private VinylStatus status;

    public UserVinylDto(UserVinyl userVinyl) {
        this.userId = userVinyl.getUser().getId();
        this.vinylId = userVinyl.getVinyl().getId();
        this.status = userVinyl.getStatus();
    }

    public UserVinyl toEntity(User user, Vinyl vinyl) {
        return new UserVinyl(user, vinyl, this.status);
    }
}