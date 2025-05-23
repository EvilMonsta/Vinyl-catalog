package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.Vinyl;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VinylRepository extends JpaRepository<Vinyl, Integer>, JpaSpecificationExecutor<Vinyl> {
    @Query(value = " SELECT v.* FROM vinyls v " +
            "JOIN users u ON v.added_by_id = u.id " +
            "WHERE u.username = :username", nativeQuery = true)
    List<Vinyl> findVinylsByUploaderUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM vinyls ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Vinyl> findRandomVinyls(@org.springframework.data.repository.query.Param("limit") int limit);

    @Query(value = "SELECT * FROM vinyls WHERE release_year = :year ORDER BY RANDOM() LIMIT :limit",
            nativeQuery = true)
    List<Vinyl> findRandomVinylsByYear(@org.springframework.data.repository.query.Param("year") int year,
                                       @org.springframework.data.repository.query.Param("limit") int limit);

    List<Vinyl> findByTitleContainingIgnoreCase(String title);

    List<Vinyl> findByArtistContainingIgnoreCase(String artist);

    List<Vinyl> findByGenre(Genre genre);

    List<Vinyl> findByReleaseYear(Integer releaseYear);

    List<Vinyl> findByAddedBy(User addedBy);
}
