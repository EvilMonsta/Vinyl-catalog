package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.enums.Genre;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.service.VinylService;
import java.util.List;
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
public class VinylController {
    private final VinylService vinylService;

    public VinylController(VinylService vinylService) {
        this.vinylService = vinylService;
    }

    @GetMapping
    public List<Vinyl> getAllVinyls() {
        return vinylService.getAllVinyls();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vinyl> getVinylById(@PathVariable Integer id) {
        return vinylService.getVinyl(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Vinyl> searchVinyls(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) Integer releaseYear
    ) {
        return vinylService.searchVinyls(title, artist, genre, releaseYear);
    }

    @PostMapping("/create")
    public ResponseEntity<Vinyl> createVinyl(@RequestBody Vinyl vinyl) {
        return ResponseEntity.ok(vinylService.createVinyl(vinyl));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Vinyl> updateVinyl(@PathVariable Integer id, @RequestBody Vinyl vinyl) {
        return ResponseEntity.ok(vinylService.updateVinyl(id, vinyl));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVinyl(@PathVariable Integer id) {
        vinylService.deleteVinyl(id);
        return ResponseEntity.noContent().build();
    }
}
