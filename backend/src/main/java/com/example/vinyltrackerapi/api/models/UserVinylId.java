package com.example.vinyltrackerapi.api.models;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVinylId implements Serializable {
    private Integer user;
    private Integer vinyl;
}