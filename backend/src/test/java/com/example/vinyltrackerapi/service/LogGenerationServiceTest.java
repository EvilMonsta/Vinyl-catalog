package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.LogTask;
import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import com.example.vinyltrackerapi.api.utils.LogFileProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogGenerationServiceTest {

    private LogFileProcessor logFileProcessor;
    private LogGenerationService logGenerationService;

    @BeforeEach
    void setUp() {
        logFileProcessor = mock(LogFileProcessor.class);
        logGenerationService = new LogGenerationService(logFileProcessor);
    }

    @Test
    void startTask_shouldCreateAndReturnTaskId_whenDatesAreValid() {
        String from = "2024-01-01";
        String to = "2024-01-31";

        String taskId = logGenerationService.startTask(from, to);

        assertThat(taskId).isNotNull();
        LogTask task = logGenerationService.getStatus(taskId);
        assertThat(task).isNotNull();
        assertThat(task.getStatus()).isEqualTo(LogTaskStatus.PENDING);

        verify(logFileProcessor).processRange(eq(from), eq(to), eq(taskId), anyMap());
    }

    @Test
    void startTask_shouldThrowException_whenFromAfterTo() {
        String from = "2024-02-01";
        String to = "2024-01-01";

        assertThatThrownBy(() -> logGenerationService.startTask(from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный диапазон дат");
    }

    @Test
    void startTask_shouldThrowException_whenInvalidFormat() {
        String from = "01-01-2024";
        String to = "2024-01-31";

        assertThatThrownBy(() -> logGenerationService.startTask(from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверные даты или формат");
    }

    @Test
    void getStatus_shouldReturnNull_whenTaskNotExists() {
        String unknownId = UUID.randomUUID().toString();
        assertThat(logGenerationService.getStatus(unknownId)).isNull();
    }
}
