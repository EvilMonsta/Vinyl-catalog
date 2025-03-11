package com.example.vinyltrackerapi.api.dto;

import com.example.vinyltrackerapi.api.enums.Genre;
import com.example.vinyltrackerapi.api.models.Vinyl;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VinylDto {
    private Integer id;
    private String title;
    private String artist;
    private Genre genre;
    private Integer releaseYear;
    private String description;
    private String coverUrl;
    private Integer addedById;

    public VinylDto(Vinyl vinyl) {
        this.id = vinyl.getId();
        this.title = vinyl.getTitle();
        this.artist = vinyl.getArtist();
        this.genre = vinyl.getGenre();
        this.releaseYear = vinyl.getReleaseYear();
        this.description = vinyl.getDescription();
        this.coverUrl = vinyl.getCoverUrl();
        this.addedById = vinyl.getAddedBy() != null ? vinyl.getAddedBy().getId() : null;
    }

    public Vinyl toEntity() {
        Vinyl vinyl = new Vinyl();
        vinyl.setTitle(this.title);
        vinyl.setArtist(this.artist);
        vinyl.setGenre(this.genre);
        vinyl.setReleaseYear(this.releaseYear);
        vinyl.setDescription(this.description);
        vinyl.setCoverUrl(this.coverUrl);
        return vinyl;
    }
}