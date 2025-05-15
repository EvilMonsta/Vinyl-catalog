package com.example.vinyltrackerapi.api.specifications;

import com.example.vinyltrackerapi.api.models.Vinyl;
import org.springframework.data.jpa.domain.Specification;

public class VinylSpecification {
    private VinylSpecification() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

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

    public static Specification<Vinyl> hasTitleLike(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Vinyl> hasArtistLike(String artist) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("artist")), "%" + artist.toLowerCase() + "%");
    }

    public static Specification<Vinyl> hasGenreId(Integer genreId) {
        return (root, query, cb) -> cb.equal(root.get("genre").get("id"), genreId);
    }
}