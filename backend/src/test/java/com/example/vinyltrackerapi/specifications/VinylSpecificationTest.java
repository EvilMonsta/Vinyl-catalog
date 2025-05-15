package com.example.vinyltrackerapi.specifications;

import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.specifications.VinylSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VinylSpecificationTest {

    @Mock private Root<Vinyl> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder criteriaBuilder;
    @Mock private Predicate predicate;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void hasTitle_shouldReturnPredicate_whenTitleProvided() {
        when(criteriaBuilder.lower(any())).thenReturn(null);
        when(criteriaBuilder.like(null, "%test%")).thenReturn(predicate);

        Specification<Vinyl> spec = VinylSpecification.hasTitle("Test");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    void hasTitle_shouldReturnNull_whenTitleIsNull() {
        Specification<Vinyl> spec = VinylSpecification.hasTitle(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void hasArtist_shouldReturnPredicate_whenArtistProvided() {
        when(criteriaBuilder.lower(any())).thenReturn(null);
        when(criteriaBuilder.like(null, "%beatles%")).thenReturn(predicate);

        Specification<Vinyl> spec = VinylSpecification.hasArtist("Beatles");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    void hasArtist_shouldReturnNull_whenArtistIsNull() {
        Specification<Vinyl> spec = VinylSpecification.hasArtist(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void hasReleaseYear_shouldReturnPredicate_whenYearProvided() {
        when(criteriaBuilder.equal(any(), eq(1999))).thenReturn(predicate);

        Specification<Vinyl> spec = VinylSpecification.hasReleaseYear(1999);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    void hasReleaseYear_shouldReturnNull_whenYearIsNull() {
        Specification<Vinyl> spec = VinylSpecification.hasReleaseYear(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }

    @Test
    void hasGenre_shouldReturnPredicate_whenGenreProvided() {
        when(criteriaBuilder.equal(any(), eq("rock"))).thenReturn(predicate);

        Specification<Vinyl> spec = VinylSpecification.hasGenre("rock");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    void hasGenre_shouldReturnNull_whenGenreIsNull() {
        Specification<Vinyl> spec = VinylSpecification.hasGenre(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNull();
    }
}
