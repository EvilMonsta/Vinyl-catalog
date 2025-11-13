package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.Genre;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByNameIgnoreCase(String name);
    List<Genre> findByNameContainingIgnoreCase(String name);
}