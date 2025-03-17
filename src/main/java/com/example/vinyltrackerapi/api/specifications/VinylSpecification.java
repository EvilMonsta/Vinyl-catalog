package com.example.vinyltrackerapi.api.specifications;

import com.example.vinyltrackerapi.api.models.Vinyl;
import org.springframework.data.jpa.domain.Specification;

public class VinylSpecification {
    public static Specification<Vinyl> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> title == null ? null :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%");
    }

    public static Specification<Vinyl> hasArtist(String artist) {
        return (root, query, criteriaBuilder) -> artist == null ? null :
                criteriaBuilder.like(criteriaBuilder.lower(root.get("artist")),
                        "%" + artist.toLowerCase() + "%");
    }

    public static Specification<Vinyl> hasReleaseYear(Integer releaseYear) {
        return (root, query, criteriaBuilder) -> releaseYear == null ? null :
                criteriaBuilder.equal(root.get("releaseYear"), releaseYear);
    }

    public static Specification<Vinyl> hasGenre(String genre) {
        return (root, query, criteriaBuilder) -> genre == null ? null :
                criteriaBuilder.equal(root.get("genre"), genre);
    }
}