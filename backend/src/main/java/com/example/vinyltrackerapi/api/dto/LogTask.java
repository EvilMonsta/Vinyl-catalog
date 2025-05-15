package com.example.vinyltrackerapi.api.dto;

import com.example.vinyltrackerapi.api.enums.LogTaskStatus;
import lombok.Data;

@Data
public class LogTask {
    private String id;
    private LogTaskStatus status;
    private String filePath;
    private String error;
}
