package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.enums.Genre;
import com.example.vinyltrackerapi.api.models.User;
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
    private final UserService userService;

    public VinylService(VinylRepository vinylRepository, UserService userService) {
        this.vinylRepository = vinylRepository;
        this.userService = userService;
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

    public Vinyl createVinyl(VinylDto vinylDto) {
        Vinyl vinyl = vinylDto.toEntity();

        if (vinylDto.getAddedById() != null) {
            User addedBy = userService.getUser(vinylDto.getAddedById())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Пользователь не найден!"));
            vinyl.setAddedBy(addedBy);
        }

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

    public void detachUserFromVinyl(User user) {
        List<Vinyl> vinylsAddedByUser = vinylRepository.findByAddedBy(user);
        vinylsAddedByUser.forEach(vinyl -> vinyl.setAddedBy(null));
        vinylRepository.saveAll(vinylsAddedByUser);
    }
}
