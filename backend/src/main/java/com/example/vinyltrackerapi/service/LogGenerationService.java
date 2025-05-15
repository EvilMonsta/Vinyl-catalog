package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import com.example.vinyltrackerapi.api.utils.LogFileProcessor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogGenerationService {
    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();
    private final LogFileProcessor logFileProcessor;

    public String startTask(String from, String to) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fromDate = LocalDate.parse(from, formatter);
            LocalDate toDate = LocalDate.parse(to, formatter);

            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("Неверный диапазон дат: from > to");
            }

            String id = UUID.randomUUID().toString();
            LogTask task = new LogTask();
            task.setId(id);
            task.setStatus(LogTaskStatus.PENDING);
            tasks.put(id, task);

            logFileProcessor.processRange(from, to, id, tasks);

            return id;
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверные даты или формат: " + e.getMessage());
        }
    }

    public LogTask getStatus(String id) {
        return tasks.get(id);
    }
}