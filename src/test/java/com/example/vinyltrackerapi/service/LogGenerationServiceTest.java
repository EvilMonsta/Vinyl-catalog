package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LogGenerationServiceTest {
    private LogGenerationService service;

    @BeforeEach
    void setUp() {
        service = new LogGenerationService();
    }

    @Test
    void startTask_shouldReturnValidIdAndEventuallySetStatus() throws InterruptedException {
        String id = service.startTask("2025-04-10");
        Thread.sleep(500);

        LogTask task = service.getStatus(id);
        assertThat(task.getStatus()).isIn(LogTaskStatus.PENDING, LogTaskStatus.SUCCESS, LogTaskStatus.FAILED);
    }

    @Test
    void generateLogFile_shouldSetSuccessIfFileExists() throws Exception {
        String testDate = "2025-04-10";
        String id = service.startTask(testDate);

        Path file = Path.of("logs/vinyltracker-" + testDate + ".log");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "Test log line");

        Thread.sleep(1000);

        LogTask task = service.getStatus(id);
        assertThat(task.getStatus()).isEqualTo(LogTaskStatus.SUCCESS);
        assertThat(task.getFilePath()).isEqualTo(file.toString());

        Files.deleteIfExists(file);
    }

    @Test
    void generateLogFile_shouldSetFailedIfFileNotExists() throws InterruptedException {
        String id = service.startTask("2099-12-31");

        Thread.sleep(1000);

        LogTask task = service.getStatus(id);
        assertThat(task.getStatus()).isEqualTo(LogTaskStatus.FAILED);
        assertThat(task.getError()).contains("Файл логов не найден");
    }
}