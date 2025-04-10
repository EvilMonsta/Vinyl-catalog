package com.example.vinyltrackerapi.api.utils;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogFileProcessor {
    @Async
    public void process(String date, String taskId, Map<String, LogTask> tasks) {
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
}