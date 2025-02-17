package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.service.VinylService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vinyls")
public class VinylController {
    private final VinylService vinylService;

    @Autowired
    public VinylController(VinylService vinylService) {
        this.vinylService = vinylService;
    }

    @GetMapping
    public List<Vinyl> getAllVinyls() {
        return vinylService.getAllVinyls();
    }

    @GetMapping("/search")
    public Optional<Vinyl> getVinylByQueryParam(@RequestParam Integer id) {
        return vinylService.getVinyl(id);
    }

    @GetMapping("/{id}")
    public Optional<Vinyl> getVinylByPathParam(@PathVariable Integer id) {
        return vinylService.getVinyl(id);
    }
}
