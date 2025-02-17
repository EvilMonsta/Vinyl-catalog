package com.example.vinyltrackerapi.api.models;

import com.example.vinyltrackerapi.api.enums.VinylStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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