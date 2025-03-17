package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.enums.Genre;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.service.VinylService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vinyls")
@RequiredArgsConstructor
public class VinylController {
    private final VinylService vinylService;

    @GetMapping
    public List<VinylDto> getAllVinyls() {
        return vinylService.getAllVinyls().stream()
                .map(VinylDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VinylDto> getVinylById(@PathVariable Integer id) {
        return vinylService.getVinyl(id)
                .map(vinyl -> ResponseEntity.ok(new VinylDto(vinyl)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<VinylDto> searchVinyls(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) String genre) {
        return vinylService.searchVinyls(title, artist, releaseYear, genre);
    }

    @PostMapping("/create")
    public ResponseEntity<VinylDto> createVinyl(@RequestBody VinylDto vinylDto) {
        return ResponseEntity.ok(new VinylDto(vinylService.createVinyl(vinylDto)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<VinylDto> updateVinyl(@PathVariable Integer id, @RequestBody VinylDto vinylDto) {
        Vinyl updatedVinyl = vinylService.updateVinyl(id, vinylDto.toEntity());
        return ResponseEntity.ok(new VinylDto(updatedVinyl));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVinyl(@PathVariable Integer id) {
        vinylService.deleteVinyl(id);
        return ResponseEntity.noContent().build();
    }
}