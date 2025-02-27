package com.example.vinyltrackerapi.api.models;

import java.io.Serializable;
import java.util.Objects;

public class UserVinylId implements Serializable {
    private Integer user;
    private Integer vinyl;

    public UserVinylId() {}

    public UserVinylId(Integer user, Integer vinyl) {
        this.user = user;
        this.vinyl = vinyl;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getVinyl() {
        return vinyl;
    }

    public void setVinyl(Integer vinyl) {
        this.vinyl = vinyl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserVinylId that)) return false; // Новый стиль
        return Objects.equals(user, that.user) &&
                Objects.equals(vinyl, that.vinyl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, vinyl);
    }
}
