package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.service.VisitCounterService;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visits")
public class VisitStatsController {
    private final VisitCounterService visitCounterService;

    public VisitStatsController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping
    public Map<String, Integer> getAllVisitStats() {
        return visitCounterService.getAllCounts();
    }

    @GetMapping("/{uri}")
    public int getVisitCount(@PathVariable String uri) {
        String fullPath = String.join("/", "", uri);
        return visitCounterService.getCount(fullPath);
    }

    @DeleteMapping
    public void resetAll() {
        visitCounterService.reset();
    }
}