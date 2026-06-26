package com.prodman.gateway.service;

import com.prodman.gateway.model.LogEntry;
import com.prodman.gateway.repository.LogRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoggingService {

    private final LogRepository logRepository;

    public LoggingService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void saveLog(LogEntry logEntry) {
        logRepository.save(logEntry).subscribe();
    }
}