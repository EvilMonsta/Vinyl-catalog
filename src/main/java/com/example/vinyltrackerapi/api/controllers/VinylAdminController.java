package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.VinylDto;
import com.example.vinyltrackerapi.api.models.Vinyl;
import com.example.vinyltrackerapi.service.UserVinylFacade;
import com.example.vinyltrackerapi.service.VinylService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/vinyls")
@RequiredArgsConstructor
@Tag(name = "Администрирование пластинок", description = "Создание, редактирование и удаление винилов")
@PreAuthorize("hasRole('ADMIN')")
public class VinylAdminController {
    private final VinylService vinylService;
    private final UserVinylFacade userVinylFacade;

    @Operation(summary = "Создать пластинку")
    @PostMapping("/create")
    public ResponseEntity<VinylDto> createVinyl(@RequestBody @Valid VinylDto vinylDto) {
        return ResponseEntity.ok(new VinylDto(vinylService.createVinyl(vinylDto)));
    }

    @Operation(summary = "Обновить пластинку по ID")
    @PutMapping("/update/{id}")
    public ResponseEntity<VinylDto> updateVinyl(@Parameter(description = "ID пластинки")
                                                @PathVariable Integer id,
                                                @RequestBody @Valid VinylDto vinylDto) {
        Vinyl updatedVinyl = vinylService.updateVinyl(id, vinylDto);
        return ResponseEntity.ok(new VinylDto(updatedVinyl));
    }

    @Operation(summary = "Удалить пластинку по ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVinyl(@PathVariable Integer id) {
        userVinylFacade.deleteVinyl(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @Operation(summary = "Загрузить список новых пластинок")
    public ResponseEntity<List<VinylDto>> createBulk(@RequestBody List<VinylDto> vinylDtos) {
        List<Vinyl> saved = vinylService.createVinylsBulk(vinylDtos);
        return ResponseEntity.ok(saved.stream().map(VinylDto::new).toList());
    }
}