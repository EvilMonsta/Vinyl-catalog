package com.example.vinyltrackerapi.api.dto;

import com.example.vinyltrackerapi.api.models.Genre;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.models.Vinyl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VinylDto {
    private Integer id;
    @NotBlank(message = "Название пластинки не может быть пустым")
    private String title;

    @NotBlank(message = "Имя исполнителя не может быть пустым")
    private String artist;

    @NotNull(message = "Жанр обязателен")
    private Integer genreId;

    @NotNull(message = "Год релиза обязателен")
    @Min(value = 1800, message = "Год должен быть больше 1800")
    @Max(value = 2100, message = "Год не может быть в будущем дальше 2100")
    private Integer releaseYear;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @Size(max = 500, message = "Ссылка на обложку не должна превышать 500 символов")
    private String coverUrl;

    @NotNull(message = "Требуется указать, кто загрузил пластинку")
    private Integer addedById;

    public VinylDto(Vinyl vinyl) {
        this.id = vinyl.getId();
        this.title = vinyl.getTitle();
        this.artist = vinyl.getArtist();
        this.genreId = vinyl.getGenre() != null ? vinyl.getGenre().getId() : null;
        this.releaseYear = vinyl.getReleaseYear();
        this.description = vinyl.getDescription();
        this.coverUrl = vinyl.getCoverUrl();
        this.addedById = vinyl.getAddedBy() != null ? vinyl.getAddedBy().getId() : null;
    }

    public Vinyl toEntity(Genre genre, User addedBy) {
        Vinyl vinyl = new Vinyl();
        vinyl.setId(id);
        vinyl.setTitle(title);
        vinyl.setArtist(artist);
        vinyl.setGenre(genre);
        vinyl.setReleaseYear(releaseYear);
        vinyl.setDescription(description);
        vinyl.setCoverUrl(coverUrl);
        vinyl.setAddedBy(addedBy);
        return vinyl;
    }
}