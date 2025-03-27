package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.VinylStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinylStatusRepository extends JpaRepository<VinylStatus, Integer> {}