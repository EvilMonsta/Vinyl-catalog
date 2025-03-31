package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {}