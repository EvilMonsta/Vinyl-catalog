package com.example.vinyltrackerapi.api.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vinyl_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VinylStatus {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;
}