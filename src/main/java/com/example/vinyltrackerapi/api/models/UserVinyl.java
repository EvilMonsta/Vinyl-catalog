package com.example.vinyltrackerapi.api.models;

import jakarta.persistence.*;
import lombok.*;

import com.example.vinyltrackerapi.api.enums.VinylStatus;

@Entity
@Table(name = "user_vinyls", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vinyl_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVinyl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VinylStatus status;
}