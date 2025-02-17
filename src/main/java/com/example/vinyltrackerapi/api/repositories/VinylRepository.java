package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.Vinyl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinylRepository extends JpaRepository<Vinyl, Integer> {
}
