package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {}