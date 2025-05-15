package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    List<Genre> findByNameContainingIgnoreCase(String name);
}