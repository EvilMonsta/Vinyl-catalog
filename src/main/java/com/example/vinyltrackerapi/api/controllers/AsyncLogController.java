package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import com.example.vinyltrackerapi.service.LogGenerationService;
import java.io.FileInputStream;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/logs/async")
@RequiredArgsConstructor
public class AsyncLogController {
    private final LogGenerationService service;

    @PostMapping("/{date}")
    public ResponseEntity<String> startGeneration(@PathVariable String date) {
        String id = service.startTask(date);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<LogTask> getStatus(@PathVariable String id) {
        LogTask task = service.getStatus(id);
        if (task == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(task);
    }

    @GetMapping("/result/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String id) {
        LogTask task = service.getStatus(id);
        if (task == null || task.getStatus() != LogTaskStatus.SUCCESS) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(task.getFilePath()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; " +
                            "filename=" + Paths.get(task.getFilePath()).getFileName())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}