package com.prodman.test.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTestRequest {
    private String testId;
    private String employeeId;
    private String employeeName;
    private Map<String, List<Integer>> answers; // questionId -> selectedOptions
}