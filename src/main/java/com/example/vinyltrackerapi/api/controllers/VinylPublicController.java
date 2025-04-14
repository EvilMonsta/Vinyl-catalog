package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.service.VinylService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vinyls")
@RequiredArgsConstructor
@Tag(name = "Публичный доступ к пластинкам", description = "Просмотр и поиск винилов")
public class VinylPublicController {
    private final VinylService vinylService;

    @Operation(summary = "Получить все пластинки")
    @GetMapping
    public List<VinylDto> getAllVinyls() {
        return vinylService.getAllVinyls().stream()
                .map(VinylDto::new)
                .toList();
    }

    @Operation(summary = "Получить пластинку по ID")
    @GetMapping("/{id}")
    public ResponseEntity<VinylDto> getVinylById(@Parameter(description = "ID пластинки")
                                                 @PathVariable Integer id) {
        Vinyl vinyl = vinylService.getVinyl(id);
        return ResponseEntity.ok(new VinylDto(vinyl));
    }

    @Operation(summary = "Поиск пластинок по параметрам")
    @GetMapping("/search")
    public List<VinylDto> searchVinyls(
            @Parameter(description = "Название") @RequestParam(required = false) String title,
            @Parameter(description = "Артист") @RequestParam(required = false) String artist,
            @Parameter(description = "Год выпуска") @RequestParam(required = false) Integer releaseYear,
            @Parameter(description = "Жанр") @RequestParam(required = false) String genre) {
        return vinylService.searchVinyls(title, artist, releaseYear, genre);
    }

    @Operation(summary = "Гибкий поиск пластинок по текстовому запросу")
    @GetMapping("/search/global")
    public List<VinylDto> searchVinylsByText(
            @Parameter(description = "Произвольный поисковый запрос (название, артист, жанр или год)")
            @RequestParam String query) {
        return vinylService.searchVinylsGlobal(query);
    }

    @Operation(summary = "Получить пластинки по username загрузившего пользователя (Path)")
    @GetMapping("/uploaded-by/{username}")
    public List<VinylDto> getVinylsByUploader(@Parameter(description = "Username")
                                              @PathVariable String username) {
        return vinylService.getVinylsByUploaderUsername(username)
                .stream()
                .map(VinylDto::new)
                .toList();
    }

    @Operation(summary = "Получить пластинки по username загрузившего пользователя (Request)")
    @GetMapping("/uploaded-by")
    public List<VinylDto> findVinylsByUploader(@RequestParam String username) {
        return vinylService.getVinylsByUploaderUsername(username)
                .stream()
                .map(VinylDto::new)
                .toList();
    }
}