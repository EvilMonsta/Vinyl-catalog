package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import com.example.vinyltrackerapi.api.utils.LogFileProcessor;
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

    public String startTask(String date) {
        String id = UUID.randomUUID().toString();
        LogTask task = new LogTask();
        task.setId(id);
        task.setStatus(LogTaskStatus.PENDING);
        tasks.put(id, task);

        logFileProcessor.process(date, id, tasks); // Теперь всё корректно и async

        return id;
    }

    public LogTask getStatus(String id) {
        return tasks.get(id);
    }
}