package com.prodman.test.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse {
    private String id;
    private String title;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;
}