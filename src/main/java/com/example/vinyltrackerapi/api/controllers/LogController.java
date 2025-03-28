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

@Slf4j
@RestController
@RequestMapping("/logs")
@Tag(name = "Логирование", description = "Эндпоинты для получения лог-файлов")
public class LogController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    @Operation(summary = "Получить лог-файл по дате и времени",
            description = "Возвращает .log файл," +
                    " сформированный в конкретный момент времени (формат: yyyy-MM-dd_HH-mm)")
    @GetMapping("/{datetime}")
    public ResponseEntity<?> getLogsByDateTime(@Parameter(description = "Дата и время в формате " +
            "yyyy-MM-dd_HH-mm")
                                               @PathVariable String datetime) {
        try {
            LocalDateTime targetDateTime = LocalDateTime.parse(datetime, DATE_FORMATTER);
            String fileName = "logs/vinyltracker-" + DATE_FORMATTER.format(targetDateTime) + ".log";
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
                    "Ожидаемый формат: yyyy-MM-dd_HH-mm");
        }
    }
}