package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.UserVinylId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVinylRepository extends JpaRepository<UserVinyl, UserVinylId> {
    List<UserVinyl> findByUser(User user);
}
