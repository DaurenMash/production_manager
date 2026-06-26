package com.prodman.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gateway_logs")
public class LogEntry {
    @Id
    private String id;
    private LocalDateTime timestamp;
    private String method;
    private String path;
    private String queryParams;
    private String clientIp;
    private int statusCode;
    private long responseTime;
}