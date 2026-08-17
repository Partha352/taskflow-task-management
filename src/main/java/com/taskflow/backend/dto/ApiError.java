package com.taskflow.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(LocalDateTime timestamp, int status, String message, String path, Map<String, String> fieldErrors) {
}
