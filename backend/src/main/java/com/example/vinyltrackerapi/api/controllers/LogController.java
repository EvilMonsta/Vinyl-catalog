package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import com.example.vinyltrackerapi.service.LogGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@Tag(name = "Логирование", description = "Эндпоинты для получения лог-файлов")
@Slf4j
public class LogController {
    private final LogGenerationService service;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "Получить лог-файл по дате",
            description = "Возвращает .log файл, сформированный в указанный день (формат: yyyy-MM-dd)")
    @GetMapping("/{date}")
    public ResponseEntity<InputStreamResource> getLogsByDate(
            @Parameter(description = "Дата в формате yyyy-MM-dd")
            @PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            String fileName = "logs/vinyltracker-" + DATE_FORMATTER.format(localDate) + ".log";
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
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Запустить асинхронную генерацию логов")
    @PostMapping("/{from}/{to}")
    public ResponseEntity<String> startGeneration(
            @PathVariable String from,
            @PathVariable String to) {
        try {
            String id = service.startTask(from, to);
            return ResponseEntity.ok(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }

    @Operation(summary = "Получить статус задачи генерации логов")
    @GetMapping("/status/{id}")
    public ResponseEntity<LogTask> getStatus(@PathVariable String id) {
        LogTask task = service.getStatus(id);
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Скачать сгенерированный лог")
    @GetMapping("/result/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String id) {
        LogTask task = service.getStatus(id);

        if (task == null || task.getStatus() != LogTaskStatus.SUCCESS) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Path path = Paths.get(task.getFilePath());
            InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + path.getFileName())
                    .contentLength(Files.size(path))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
