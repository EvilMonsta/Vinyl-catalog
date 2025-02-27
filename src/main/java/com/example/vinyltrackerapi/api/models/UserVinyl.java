package com.example.vinyltrackerapi.api.models;

import com.example.vinyltrackerapi.api.enums.VinylStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vinyls")
@IdClass(UserVinylId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVinyl {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VinylStatus status;
}