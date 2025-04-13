package com.example.vinyltrackerapi.api.utils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class LogCleanup {
    private static final String LOG_DIR = "logs";
    private static final String GENERATED_PREFIX = "generated-log-";

    @PostConstruct
    public void deleteAllGeneratedLogsOnStartup() {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) return;

            try (Stream<Path> files = Files.list(logDir)) {
                files.filter(path -> path.getFileName().toString().startsWith(GENERATED_PREFIX))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                                System.out.println("[CLEANUP] Удалён файл: " + path);
                            } catch (IOException e) {
                                System.err.println("[CLEANUP] Ошибка удаления файла:" +
                                        " " + path + " — " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("[CLEANUP] Ошибка при очистке логов: " + e.getMessage());
        }
    }
}