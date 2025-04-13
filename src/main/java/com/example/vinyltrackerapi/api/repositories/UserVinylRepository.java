package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.UserVinyl;
import com.example.vinyltrackerapi.api.models.UserVinylId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVinylRepository extends JpaRepository<UserVinyl, UserVinylId> {
    List<UserVinyl> findByUserId(Integer userId);

    List<UserVinyl> findByVinylId(Integer vinylId);

    void deleteAllByUserId(Integer userId);

    void deleteAllByVinylId(Integer vinylId);

    @Query("SELECT uv FROM UserVinyl uv " +
            "JOIN FETCH uv.vinyl v " +
            "JOIN FETCH uv.status s " +
            "WHERE uv.user.id = :userId")
    List<UserVinyl> findAllWithVinylAndStatusByUserId(@Param("userId") Integer userId);

}
