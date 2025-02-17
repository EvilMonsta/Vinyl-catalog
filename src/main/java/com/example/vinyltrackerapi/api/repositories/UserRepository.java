package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
