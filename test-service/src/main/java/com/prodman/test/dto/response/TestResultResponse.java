package com.prodman.test.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultResponse {
    private String id;
    private String testId;
    private String testTitle;
    private String employeeId;
    private String employeeName;
    private Double score;
    private Boolean passed;
    private LocalDateTime completedAt;
    private Map<String, List<Integer>> answers;
}