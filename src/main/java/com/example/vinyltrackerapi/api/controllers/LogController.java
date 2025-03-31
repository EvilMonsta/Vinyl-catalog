package com.example.vinyltrackerapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@Tag(name = "Логирование", description = "Эндпоинты для получения лог-файлов")
@Slf4j
public class LogController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "Получить лог-файл по дате",
            description = "Возвращает .log файл, сформированный в указанный день (формат: yyyy-MM-dd)")
    @GetMapping("/{date}")
    public ResponseEntity<?> getLogsByDate(
            @Parameter(description = "Дата в формате yyyy-MM-dd") @PathVariable String date) {
        try {
            LocalDateTime localDate = LocalDateTime.parse(date, DATE_FORMATTER);
            String fileName = "logs/vinyltracker-" + localDate + ".log";
            Path path = Paths.get(fileName);

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(fileName));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);

        } catch (Exception e) {
            log.error("[LOG] Ошибка получения логов: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка формата или чтения логов. " +
                    "Ожидаемый формат: yyyy-MM-dd");
        }
    }
}
