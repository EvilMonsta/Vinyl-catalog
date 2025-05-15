package com.example.vinyltrackerapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class VinylTrackerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VinylTrackerApiApplication.class, args);
        log.info("Приложение запущено!");
    }
}
