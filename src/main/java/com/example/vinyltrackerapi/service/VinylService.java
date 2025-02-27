package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.enums.Genre;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.api.repositories.VinylRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VinylService {
    private final VinylRepository vinylRepository;

    public VinylService(VinylRepository vinylRepository) {
        this.vinylRepository = vinylRepository;
    }

    public List<Vinyl> getAllVinyls() {
        return vinylRepository.findAll();
    }

    public Optional<Vinyl> getVinyl(Integer id) {
        return vinylRepository.findById(id);
    }

    public List<Vinyl> searchVinyls(String title, String artist, Genre genre, Integer releaseYear) {
        if (title != null) {
            return vinylRepository.findByTitleContainingIgnoreCase(title);
        }
        if (artist != null) {
            return vinylRepository.findByArtistContainingIgnoreCase(artist);
        }
        if (genre != null) {
            return vinylRepository.findByGenre(genre);
        }
        if (releaseYear != null) {
            return vinylRepository.findByReleaseYear(releaseYear);
        }
        return vinylRepository.findAll();
    }

    public Vinyl createVinyl(Vinyl vinyl) {
        return vinylRepository.save(vinyl);
    }

    public Vinyl updateVinyl(Integer id, Vinyl newVinylData) {
        return vinylRepository.findById(id).map(vinyl -> {
            vinyl.setTitle(newVinylData.getTitle());
            vinyl.setArtist(newVinylData.getArtist());
            vinyl.setGenre(newVinylData.getGenre());
            vinyl.setReleaseYear(newVinylData.getReleaseYear());
            vinyl.setDescription(newVinylData.getDescription());
            vinyl.setCoverUrl(newVinylData.getCoverUrl());
            return vinylRepository.save(vinyl);
        }).orElseThrow(() -> new RuntimeException("Винил не найден!"));
    }

    public void deleteVinyl(Integer id) {
        if (!vinylRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Винил с ID " + id + " не найден!");
        }
        vinylRepository.deleteById(id);
    }
}
