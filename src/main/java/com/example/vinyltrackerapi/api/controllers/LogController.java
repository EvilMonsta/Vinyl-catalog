package com.example.vinyltrackerapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private static final String LOG_FILE_PATH = "logs/vinyltracker.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "Получить лог-файл по дате",
            description = "Формирует и возвращает .log файл с логами " +
            "за указанную дату в формате yyyy-MM-dd")
    @GetMapping("/{date}")
    public ResponseEntity<?> getLogsByDate(@Parameter(description = "Дата в формате yyyy-MM-dd")
                                               @PathVariable String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
            Path path = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            List<String> filteredLines;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                filteredLines = reader.lines()
                        .filter(line -> line.contains(targetDate.toString()))
                        .toList();
            }

            if (filteredLines.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            File tempFile = File.createTempFile("filtered-log-", ".log");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (String line : filteredLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;" +
                            " filename=filtered-log-" + date + ".log")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при обработке логов: " + e.getMessage());
        }
    }
}
