package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogGenerationService {
    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();

    @Async
    public void generateLogFile(String date, String taskId) {
        LogTask task = tasks.get(taskId);
        try {
            Path logPath = Paths.get("logs/vinyltracker-" + date + ".log");

            if (!Files.exists(logPath)) {
                throw new FileNotFoundException("Файл логов не найден");
            }

            task.setStatus(LogTaskStatus.SUCCESS);
            task.setFilePath(logPath.toString());
        } catch (Exception e) {
            task.setStatus(LogTaskStatus.FAILED);
            task.setError(e.getMessage());
            log.error("[ASYNC] Ошибка генерации логов: {}", e.getMessage());
        }
    }

    public String startTask(String date) {
        String id = UUID.randomUUID().toString();
        LogTask task = new LogTask();
        task.setId(id);
        task.setStatus(LogTaskStatus.PENDING);
        tasks.put(id, task);
        generateLogFile(date, id);
        return id;
    }

    public LogTask getStatus(String id) {
        return tasks.get(id);
    }
}